package com.molszewski.demos.poker.persistence.repository;

import com.molszewski.demos.poker.persistence.entity.command.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class CommandRepository {
    private final ReactiveRedisTemplate<String, Command> redisCommandTemplate;

    public Mono<Void> send(final String gameId, final Command command) {
        return redisCommandTemplate.opsForStream().add(Record.of(command).withStreamKey(getStreamKey(gameId))).then();
    }

    public Flux<ObjectRecord<String, Command>> readAllNonBlocking(final String gameId) {
        return redisCommandTemplate.opsForStream()
                .read(Command.class, StreamOffset.fromStart(getStreamKey(gameId)));
    }

    public Flux<ObjectRecord<String, Command>> readFromBlocking(final StreamOffset<String> stream) {
        return redisCommandTemplate.opsForStream().read(Command.class, StreamReadOptions.empty().block(Duration.ZERO), stream);
    }

    public String getStreamKey(final String gameId) {
        return String.format("stream:%s", gameId);
    }
}
