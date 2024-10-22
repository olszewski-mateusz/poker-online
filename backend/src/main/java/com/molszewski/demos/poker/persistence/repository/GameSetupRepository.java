package com.molszewski.demos.poker.persistence.repository;

import com.molszewski.demos.poker.persistence.entity.GameSetup;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class GameSetupRepository {
    private final ReactiveRedisTemplate<String, GameSetup> redisGameTemplate;

    @Value("${poker.redis.expire-minutes}")
    int expireMinutes;

    public Mono<String> save(final GameSetup gameSetup) {
        return redisGameTemplate.opsForValue().set(getGameKey(gameSetup.id()), gameSetup, Duration.ofMinutes(expireMinutes))
                .thenReturn(gameSetup.id());
    }

    public Mono<GameSetup> findById(final String gameId) {
        return redisGameTemplate.hasKey(getGameKey(gameId)).flatMap(keyExists -> {
            if (Boolean.FALSE.equals(keyExists)) {
                return Mono.error(new IllegalArgumentException("Game not found"));
            }
            return redisGameTemplate.opsForValue().getAndExpire(getGameKey(gameId), Duration.ofMinutes(expireMinutes));
        });
    }

    public Mono<Boolean> exists(final String gameId) {
        return redisGameTemplate.hasKey(getGameKey(gameId));
    }

    public String getGameKey(final String gameId) {
        return String.format("game:%s", gameId);
    }
}
