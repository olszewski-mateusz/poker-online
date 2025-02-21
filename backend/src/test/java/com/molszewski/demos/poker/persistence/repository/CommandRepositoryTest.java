package com.molszewski.demos.poker.persistence.repository;

import com.molszewski.demos.poker.persistence.configuration.RedisConfiguration;
import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.test.IntegrationTest;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataRedisTest
@AutoConfigureJson
@Import({CommandRepository.class, RedisConfiguration.class})
@Testcontainers
@IntegrationTest
class CommandRepositoryTest {

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

    @Autowired
    private CommandRepository commandRepository;

    @Autowired
    private ReactiveRedisTemplate<String, Command> redisCommandTemplate;

    @Test
    @DisplayName("Send command test")
    void sendCommandTest() {
        Duration maxLatency = Duration.ofSeconds(5);
        String playerId = "1";
        String gameId = "sendCommandTest";
        String streamId = "stream:" + gameId;

        assertTrue(redis.isRunning());

        StepVerifier.create(commandRepository.send(gameId, Command.join(playerId, "test")))
                .expectComplete()
                .verify();

        StepVerifier.create(redisCommandTemplate.opsForStream().read(Command.class, StreamOffset.fromStart(streamId)))
                .assertNext(rec -> assertEquals(playerId, rec.getValue().getPlayerId()))
                .expectComplete()
                .verify();

        StepVerifier.create(redisCommandTemplate.getExpire(streamId))
                .assertNext(duration -> assertThat(duration).isCloseTo(Duration.ofHours(2), maxLatency))
                .verifyComplete();
    }

    @Test
    @DisplayName("Read all test")
    void readAllTest() {
        String playerId1 = "1";
        String playerId2 = "2";
        String playerId3 = "3";
        String gameId = "readAllTest";

        assertTrue(redis.isRunning());

        commandRepository.send(gameId, Command.join(playerId1, "test1")).block();
        commandRepository.send(gameId, Command.join(playerId2, "test2")).block();
        commandRepository.send(gameId, Command.join(playerId3, "test3")).block();

        StepVerifier.create(commandRepository.readAll(gameId))
                .assertNext(rec -> assertEquals(playerId1, rec.getValue().getPlayerId()))
                .assertNext(rec -> assertEquals(playerId2, rec.getValue().getPlayerId()))
                .assertNext(rec -> assertEquals(playerId3, rec.getValue().getPlayerId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Read with offset - block with no results")
    void readFromOffsetWithBlockNoResultsTest() {
        String gameId = "readFromOffsetWithBlockNoResultsTest";

        Duration minTime = Duration.ofMillis(2000);
        Duration maxTime = Duration.ofMillis(5000);

        assertTrue(redis.isRunning());

        StepVerifier.create(commandRepository.readFromOffsetWithBlock(StreamOffset.fromStart(gameId)))
                .expectNextCount(0)
                .expectComplete()
                .verifyThenAssertThat()
                .tookLessThan(maxTime)
                .tookMoreThan(minTime);
    }

    @Test
    @DisplayName("Read with offset with results")
    void readFromOffsetWithBlockResultsTest() {
        String gameId = "readFromOffsetWithBlockResultsTest";

        String playerId1 = "1";
        String playerId2 = "2";
        String playerId3 = "3";

        assertTrue(redis.isRunning());

        commandRepository.send(gameId, Command.join(playerId1, "test1")).block();

        ObjectRecord<String, Command> objectRecord = commandRepository.readAll(gameId).collectList().block().getFirst();

        commandRepository.send(gameId, Command.join(playerId2, "test2")).block();
        commandRepository.send(gameId, Command.join(playerId3, "test3")).block();

        StepVerifier.create(commandRepository.readFromOffsetWithBlock(StreamOffset.from(objectRecord)))
                .assertNext(rec -> assertEquals(playerId2, rec.getValue().getPlayerId()))
                .assertNext(rec -> assertEquals(playerId3, rec.getValue().getPlayerId()))
                .expectComplete()
                .verifyThenAssertThat();
    }

    @Test
    @DisplayName("Stream key test")
    void getStreamKeyTest() {
        String gameId = "test";
        assertEquals("stream:test", commandRepository.getStreamKey(gameId));
    }
}