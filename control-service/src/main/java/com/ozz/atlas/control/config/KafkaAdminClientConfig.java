package com.ozz.atlas.control.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaAdminClientConfig {

    @Bean(destroyMethod = "close")
    public AdminClient adminClient(KafkaProperties kafkaProperties, SslBundles sslBundles) {
        return AdminClient.create(kafkaProperties.buildAdminProperties(sslBundles));
    }
}
