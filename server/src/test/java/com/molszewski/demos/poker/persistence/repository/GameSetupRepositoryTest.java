package com.molszewski.demos.poker.persistence.repository;

import com.molszewski.demos.poker.persistence.configuration.RedisConfiguration;
import com.molszewski.demos.poker.persistence.entity.GameSetup;
import com.molszewski.demos.poker.test.IntegrationTest;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
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
@Import({GameSetupRepository.class, RedisConfiguration.class})
@Testcontainers
@IntegrationTest
class GameSetupRepositoryTest {

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

    @Autowired
    private GameSetupRepository gameSetupRepository;

    @Autowired
    private ReactiveRedisTemplate<String, GameSetup> redisCommandTemplate;

    @Test
    @DisplayName("Save correctly")
    void saveTest() {
        String gameId = "saveTest";
        String streamId = "game:" + gameId;

        assertTrue(redis.isRunning());

        GameSetup gameSetup = GameSetup.builder().id(gameId).seed(17).build();

        StepVerifier.create(gameSetupRepository.save(gameSetup))
                .assertNext(id -> assertEquals(gameId, id))
                .verifyComplete();

        StepVerifier.create(redisCommandTemplate.hasKey(streamId))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Save and wait for expire")
    void saveAndExpiredTest() {
        String gameId = "saveAndExpiredTest";
        String streamId = "game:" + gameId;

        Duration expireDuration = (Duration) ReflectionTestUtils.getField(gameSetupRepository, "expireDuration");
        ReflectionTestUtils.setField(gameSetupRepository, "expireDuration", Duration.ofMillis(50));

        assertTrue(redis.isRunning());

        GameSetup gameSetup = GameSetup.builder().id(gameId).seed(17).build();

        StepVerifier.create(gameSetupRepository.save(gameSetup).delayElement(Duration.ofMillis(500)))
                .assertNext(id -> assertEquals(gameId, id))
                .verifyComplete();

        StepVerifier.create(redisCommandTemplate.hasKey(streamId))
                .expectNext(false)
                .verifyComplete();

        ReflectionTestUtils.setField(gameSetupRepository, "expireDuration", expireDuration);
    }

    @Test
    @DisplayName("Find by id correctly")
    void findByIdTest() {
        String gameId = "findByIdTest";

        assertTrue(redis.isRunning());

        GameSetup gameSetup = GameSetup.builder().id(gameId).seed(17).build();

        gameSetupRepository.save(gameSetup).block();

        StepVerifier.create(gameSetupRepository.findById(gameId))
                .assertNext(value -> {
                    assertEquals(gameSetup.id(), value.id());
                    assertEquals(gameSetup.seed(), value.seed());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Find by non existing id throws")
    void findByIdNotFoundTest() {
        assertTrue(redis.isRunning());

        StepVerifier.create(gameSetupRepository.findById("non-existing-id"))
                .verifyErrorSatisfies(error -> assertThat(error)
                            .isInstanceOf(IllegalArgumentException.class)
                            .hasMessageContaining("Game not found")
                );
    }

    @Test
    @DisplayName("Check existing and non-existing id")
    void existsTest() {
        String gameId = "existsTest";

        assertTrue(redis.isRunning());

        GameSetup gameSetup = GameSetup.builder().id(gameId).seed(17).build();

        StepVerifier.create(gameSetupRepository.exists(gameId))
                .expectNext(false)
                .verifyComplete();

        gameSetupRepository.save(gameSetup).block();

        StepVerifier.create(gameSetupRepository.exists(gameId))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Get game key test")
    void getGameKeyTest() {
        String gameId = "test";
        assertEquals("game:test", gameSetupRepository.getGameKey(gameId));
    }
}