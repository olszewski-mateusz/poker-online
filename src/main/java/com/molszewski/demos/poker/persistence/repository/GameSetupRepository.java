package com.molszewski.demos.poker.persistence.repository;

import com.molszewski.demos.poker.persistence.entity.GameSetup;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class GameSetupRepository {
    private final ReactiveRedisTemplate<String, GameSetup> redisGameTemplate;

    public Mono<String> save(final GameSetup gameSetup) {
        return redisGameTemplate.opsForValue().set(getGameKey(gameSetup.id()), gameSetup).thenReturn(gameSetup.id());
    }

    public Mono<GameSetup> findById(final String gameId) {
        return redisGameTemplate.opsForValue().get(getGameKey(gameId));
    }

    public String getGameKey(final String gameId) {
        return String.format("game:%s", gameId);
    }
}
