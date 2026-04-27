package com.ozz.atlas.control.config;

import com.ozz.atlas.common.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic supplyPurchaseOrderTopic() {
        return TopicBuilder.name(KafkaTopics.SUPPLY_PURCHASE_ORDER)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic supplySubPurchaseOrderTopic() {
        return TopicBuilder.name(KafkaTopics.SUPPLY_SUB_PURCHASE_ORDER)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic supplyShipmentTopic() {
        return TopicBuilder.name(KafkaTopics.SUPPLY_SHIPMENT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic supplyDeliveryExceptionTopic() {
        return TopicBuilder.name(KafkaTopics.SUPPLY_DELIVERY_EXCEPTION)
                .partitions(3)
                .replicas(1)
                .build();
    }

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
    public NewTopic supplyLotTopic() {
        return TopicBuilder.name(KafkaTopics.SUPPLY_LOT)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic supplyReturnRequestTopic() {
        return TopicBuilder.name(KafkaTopics.SUPPLY_RETURN_REQUEST)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic supplySupplierCertificateTopic() {
        return TopicBuilder.name(KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE)
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
