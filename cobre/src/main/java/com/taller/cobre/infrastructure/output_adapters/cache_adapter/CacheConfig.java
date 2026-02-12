package com.taller.cobre.infrastructure.output_adapters.cache_adapter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller.cobre.domain.model.client.ClientRouting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CacheConfig {

    @Bean
    public ReactiveRedisTemplate<String, ClientRouting> clientRoutingRedisTemplate(ReactiveRedisConnectionFactory factory) {

        var serializer = new Jackson2JsonRedisSerializer<>(ClientRouting.class);
        var context = RedisSerializationContext.<String, ClientRouting>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
