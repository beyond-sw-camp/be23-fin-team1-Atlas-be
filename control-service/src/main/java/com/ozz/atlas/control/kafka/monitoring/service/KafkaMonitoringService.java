package com.ozz.atlas.control.kafka.monitoring.service;

import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.control.kafka.monitoring.dtos.KafkaEventSummaryResponse;
import com.ozz.atlas.control.kafka.monitoring.dtos.KafkaSubscriptionStatusResponse;
import com.ozz.atlas.control.kafka.rule.service.KafkaEventRuleService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.i18n.LocaleContextHolder;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMonitoringService {

    private static final List<String> MONITORED_TOPICS = List.of(
            KafkaTopics.SUPPLY_SHIPMENT,
            KafkaTopics.SUPPLY_LOGISTICS_NODE,
            KafkaTopics.SUPPLY_INVENTORY,
            KafkaTopics.CONTROL_RECOMMENDATION_REQUESTED,
            KafkaTopics.CONTROL_RECOMMENDATION_GENERATED,
            KafkaTopics.CONTROL_RECOMMENDATION_FAILED,
            KafkaTopics.SUPPLY_SUPPLIER_RISK,
            KafkaTopics.CONTROL_RECOMMENDATION_DECISION
    );

    private final KafkaEventRuleService kafkaEventRuleService;
    private final AdminClient adminClient;
    private final KafkaProperties kafkaProperties;
    private final Map<String, OffsetSnapshot> offsetSnapshots = new ConcurrentHashMap<>();

    public Page<KafkaEventSummaryResponse> getEvents(Pageable pageable) {
        return kafkaEventRuleService.getRules(pageable);
    }

    public List<KafkaSubscriptionStatusResponse> getSubscriptions() {
        try {
            Set<String> existingTopics = new HashSet<>(adminClient.listTopics()
                    .names()
                    .get(3, TimeUnit.SECONDS));
            ConsumerGroupDescription consumerGroupDescription = adminClient
                    .describeConsumerGroups(List.of(kafkaProperties.getConsumer().getGroupId()))
                    .all()
                    .get(3, TimeUnit.SECONDS)
                    .get(kafkaProperties.getConsumer().getGroupId());
            Map<TopicPartition, OffsetAndMetadata> groupOffsets = adminClient
                    .listConsumerGroupOffsets(kafkaProperties.getConsumer().getGroupId())
                    .partitionsToOffsetAndMetadata()
                    .get(3, TimeUnit.SECONDS);

            List<KafkaSubscriptionStatusResponse> responses = new ArrayList<>();
            for (String topic : MONITORED_TOPICS) {
                responses.add(buildTopicStatusSafely(topic, existingTopics, consumerGroupDescription, groupOffsets));
            }
            return responses;
        } catch (Exception e) {
            log.warn("Kafka 구독 상태 조회에 실패했습니다. broker 연결 상태를 확인합니다.", e);
            return MONITORED_TOPICS.stream()
                    .map(this::buildDisconnectedStatus)
                    .toList();
        }
    }

    private KafkaSubscriptionStatusResponse buildTopicStatusSafely(
            String topic,
            Set<String> existingTopics,
            ConsumerGroupDescription consumerGroupDescription,
            Map<TopicPartition, OffsetAndMetadata> groupOffsets
    ) {
        try {
            return buildTopicStatus(topic, existingTopics, consumerGroupDescription, groupOffsets);
        } catch (Exception e) {
            log.warn("Kafka 토픽 상태 조회에 실패했습니다. topic={}", topic, e);
            return buildUnavailableStatus(topic);
        }
    }

    private KafkaSubscriptionStatusResponse buildTopicStatus(
            String topic,
            Set<String> existingTopics,
            ConsumerGroupDescription consumerGroupDescription,
            Map<TopicPartition, OffsetAndMetadata> groupOffsets
    ) throws Exception {
        if (!existingTopics.contains(topic)) {
            return buildUnavailableStatus(topic);
        }

        TopicDescription topicDescription = adminClient.describeTopics(List.of(topic))
                .allTopicNames()
                .get(3, TimeUnit.SECONDS)
                .get(topic);

        List<TopicPartition> partitions = topicDescription.partitions().stream()
                .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
                .toList();

        Map<TopicPartition, OffsetSpec> latestOffsetSpecs = partitions.stream()
                .collect(java.util.stream.Collectors.toMap(partition -> partition, ignored -> OffsetSpec.latest()));
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latestOffsets = adminClient
                .listOffsets(latestOffsetSpecs)
                .all()
                .get(3, TimeUnit.SECONDS);

        long committedOffset = partitions.stream()
                .map(groupOffsets::get)
                .filter(offset -> offset != null)
                .mapToLong(OffsetAndMetadata::offset)
                .sum();

        long endOffset = partitions.stream()
                .map(latestOffsets::get)
                .filter(offset -> offset != null)
                .mapToLong(ListOffsetsResult.ListOffsetsResultInfo::offset)
                .sum();

        long lag = Math.max(0L, endOffset - committedOffset);
        boolean assigned = consumerGroupDescription.members().stream()
                .map(MemberDescription::assignment)
                .flatMap(assignment -> assignment.topicPartitions().stream())
                .anyMatch(partition -> topic.equals(partition.topic()));
        boolean subscribed = assigned || partitions.stream().anyMatch(groupOffsets::containsKey);

        return KafkaSubscriptionStatusResponse.builder()
                .topic(topic)
                .partitionCount(partitions.size())
                .committedOffset(committedOffset)
                .endOffset(endOffset)
                .lag(lag)
                .messagesPerHour(calculateMessagesPerHour(topic, endOffset))
                .brokerConnectionStatus(localizeStatus("CONNECTED"))
                .consumerSubscriptionStatus(localizeStatus(subscribed ? "SUBSCRIBED" : "NOT_SUBSCRIBED"))
                .build();
    }

    private KafkaSubscriptionStatusResponse buildDisconnectedStatus(String topic) {
        return KafkaSubscriptionStatusResponse.builder()
                .topic(topic)
                .partitionCount(0)
                .committedOffset(0L)
                .endOffset(0L)
                .lag(0L)
                .messagesPerHour(0)
                .brokerConnectionStatus(localizeStatus("DISCONNECTED"))
                .consumerSubscriptionStatus(localizeStatus("NOT_SUBSCRIBED"))
                .build();
    }

    private KafkaSubscriptionStatusResponse buildUnavailableStatus(String topic) {
        return KafkaSubscriptionStatusResponse.builder()
                .topic(topic)
                .partitionCount(0)
                .committedOffset(0L)
                .endOffset(0L)
                .lag(0L)
                .messagesPerHour(0)
                .brokerConnectionStatus(localizeStatus("CONNECTED"))
                .consumerSubscriptionStatus(localizeStatus("UNAVAILABLE"))
                .build();
    }

    private String localizeStatus(String status) {
        Locale locale = LocaleContextHolder.getLocale();
        if (!Locale.KOREAN.getLanguage().equals(locale.getLanguage())) {
            return status;
        }

        return switch (status) {
            case "CONNECTED" -> "연결됨";
            case "DISCONNECTED" -> "미연결";
            case "SUBSCRIBED" -> "구독 중";
            case "NOT_SUBSCRIBED" -> "미구독";
            case "UNAVAILABLE" -> "사용 불가";
            default -> status;
        };
    }

    private int calculateMessagesPerHour(String topic, long endOffset) {
        Instant now = Instant.now();
        OffsetSnapshot previous = offsetSnapshots.put(topic, new OffsetSnapshot(endOffset, now));
        if (previous == null) {
            return 0;
        }

        long seconds = Math.max(1L, now.getEpochSecond() - previous.timestamp().getEpochSecond());
        long delta = Math.max(0L, endOffset - previous.offset());
        return (int) ((delta * 3600) / seconds);
    }

    private record OffsetSnapshot(long offset, Instant timestamp) {
    }
}
