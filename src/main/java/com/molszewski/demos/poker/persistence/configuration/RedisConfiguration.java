package com.molszewski.demos.poker.persistence.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molszewski.demos.poker.persistence.entity.GameSetup;
import com.molszewski.demos.poker.persistence.entity.command.Command;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {
    @Bean
    public ReactiveRedisTemplate<String, GameSetup> gameSetupRedisTemplate(ObjectMapper objectMapper, ReactiveRedisConnectionFactory factory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<GameSetup> valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, GameSetup.class);
        RedisSerializationContext<String, GameSetup> context = RedisSerializationContext.<String, GameSetup>newSerializationContext(keySerializer)
                .value(valueSerializer)
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, Command> commandRedisTemplate(ObjectMapper objectMapper, ReactiveRedisConnectionFactory factory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Command> valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Command.class);
        RedisSerializationContext<String, Command> context = RedisSerializationContext.<String, Command>newSerializationContext(keySerializer)
                .value(valueSerializer)
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
