package com.ozz.atlas.control.kafka.monitoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaSubscriptionStatusResponse {

    private String topic;
    private int partitionCount;
    private long committedOffset;
    private long endOffset;
    private long lag;
    private Integer messagesPerHour;
    private String brokerConnectionStatus;
    private String consumerSubscriptionStatus;

}
