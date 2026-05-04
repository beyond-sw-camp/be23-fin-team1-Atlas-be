package com.ozz.atlas.control.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ozz.atlas.control.chat.service.RedisSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

@Configuration
public class ControlRedisConfig {

    @Value("${atlas.redis.notification.database:2}")
    private int notificationDatabase;

    @Value("${atlas.redis.chat.database:3}")
    private int chatDatabase;

    @Bean
    public RedisConnectionFactory notificationRedisConnectionFactory(RedisProperties redisProperties) {
        return redisConnectionFactory(redisProperties, notificationDatabase);
    }

    @Primary
    @Bean
    public RedisConnectionFactory chatRedisConnectionFactory(RedisProperties redisProperties) {
        return redisConnectionFactory(redisProperties, chatDatabase);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("chatRedisConnectionFactory") RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(listenerAdapter, new PatternTopic(RedisConstants.CHAT_ROOM_TOPIC_PREFIX + "*"));
        container.addMessageListener(listenerAdapter, new PatternTopic(RedisConstants.NOTIFY_USER_TOPIC_PREFIX + "*"));

        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisTemplate<String, Object> notificationRedisTemplate(
            @Qualifier("notificationRedisConnectionFactory") RedisConnectionFactory connectionFactory
    ) {
        return redisTemplate(connectionFactory);
    }

    @Primary
    @Bean
    public RedisTemplate<String, Object> chatRedisTemplate(
            @Qualifier("chatRedisConnectionFactory") RedisConnectionFactory connectionFactory
    ) {
        return redisTemplate(connectionFactory);
    }

    private RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties, int database) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisProperties.getHost());
        configuration.setPort(redisProperties.getPort());
        configuration.setDatabase(database);

        if (StringUtils.hasText(redisProperties.getUsername())) {
            configuration.setUsername(redisProperties.getUsername());
        }
        if (StringUtils.hasText(redisProperties.getPassword())) {
            configuration.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }

        return new LettuceConnectionFactory(configuration);
    }

    private RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);

        return redisTemplate;
    }
}
