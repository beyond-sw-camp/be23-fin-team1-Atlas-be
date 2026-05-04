package com.ozz.atlas.auth.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

@Configuration
public class AuthRedisConfig {

    private static final int VERIFICATION_DATABASE = 0;
    private static final int SESSION_DATABASE = 1;

    @Bean
    public RedisConnectionFactory authVerificationRedisConnectionFactory(RedisProperties redisProperties) {
        return redisConnectionFactory(redisProperties, VERIFICATION_DATABASE);
    }

    @Primary
    @Bean
    public RedisConnectionFactory authSessionRedisConnectionFactory(RedisProperties redisProperties) {
        return redisConnectionFactory(redisProperties, SESSION_DATABASE);
    }

    @Bean
    public StringRedisTemplate authVerificationStringRedisTemplate(
            @Qualifier("authVerificationRedisConnectionFactory") RedisConnectionFactory connectionFactory
    ) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Primary
    @Bean
    public StringRedisTemplate authSessionStringRedisTemplate(
            @Qualifier("authSessionRedisConnectionFactory") RedisConnectionFactory connectionFactory
    ) {
        return new StringRedisTemplate(connectionFactory);
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
}
