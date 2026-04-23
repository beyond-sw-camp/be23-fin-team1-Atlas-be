package com.ozz.atlas.control.config;

import com.ozz.atlas.common.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic supplyLogisticsNodeTopic() {
        return TopicBuilder.name(KafkaTopics.SUPPLY_LOGISTICS_NODE)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic supplyInventoryTopic() {
        return TopicBuilder.name(KafkaTopics.SUPPLY_INVENTORY)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic supplySupplierRiskTopic() {
        return TopicBuilder.name(KafkaTopics.SUPPLY_SUPPLIER_RISK)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic controlRecommendationDecisionTopic() {
        return TopicBuilder.name(KafkaTopics.CONTROL_RECOMMENDATION_DECISION)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
