package com.molszewski.demos.poker.web.service;

import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.game.GameEngine;
import com.molszewski.demos.poker.persistence.entity.GameSetup;
import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.repository.CommandRepository;
import com.molszewski.demos.poker.persistence.repository.GameSetupRepository;
import com.molszewski.demos.poker.web.model.response.ActionResponse;
import com.molszewski.demos.poker.web.model.response.GameResponse;
import com.molszewski.demos.poker.web.model.response.NewGameResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final GameSetupRepository gameSetupRepository;
    private final CommandRepository commandRepository;
    private final MonitoringService monitoringService;

    public Mono<NewGameResponse> createGame() {
        return gameSetupRepository.save(GameSetup.init(generateId(), new Random()))
                .map(setup -> new NewGameResponse(setup.id(), setup.adminSecret()));
    }

    public Mono<Boolean> hasAdminAccess(String gameId, String adminSecret) {
        return gameSetupRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Game not found")))
                .map(setup -> setup.adminSecret().equals(adminSecret));
    }

    public Mono<ActionResponse> handleCommand(String gameId, Command newCommand) {
        return gameSetupRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Game not found")))
                .doOnNext(gameSetup -> monitoringService.keepAlive(gameId))
                .flatMap(gameSetup -> commandRepository.readAll(gameId)
                        .map(Record::getValue)
                        .collectList()
                        .flatMap(commands -> {
                            GameEngine engine = gameSetup.toGame();
                            engine.applyActions(commands.stream().map(Command::toAction));
                            try {
                                engine.tryApplyAction(newCommand.toAction());
                                return commandRepository.send(gameId, newCommand).thenReturn(new ActionResponse(newCommand.getPlayerId()));
                            } catch (ActionValidationException e) {
                                return Mono.error(e);
                            }
                        })
                ).doOnError(e -> log.warn(e.getMessage()));
    }

    public Flux<GameResponse> subscribeToGameChanges(String gameId, String myId) {
        log.info("subscribeToGameChanges for game {}", gameId);

        final AtomicReference<StreamOffset<String>> currentOffset = new AtomicReference<>(StreamOffset.fromStart(commandRepository.getStreamKey(gameId)));

        return gameSetupRepository.findById(gameId)
                .doOnNext(gameSetup -> monitoringService.keepAlive(gameId))
                .map(GameSetup::toGame)
                .flatMapMany(game -> {
                    return Mono.defer(() -> commandRepository.readFromOffsetWithBlock(currentOffset.get()).collectList()
                            .filter(objectRecords -> !objectRecords.isEmpty())
                            .flatMap(objectRecords -> {
                                List<Command> commands = objectRecords.stream().map(Record::getValue).toList();
                                this.processCommands(game, commands);
                                if (!objectRecords.isEmpty()) {
                                    currentOffset.set(StreamOffset.from(objectRecords.getLast()));
                                }
                                return Mono.just(GameResponse.fromParams(
                                        GameResponse.GameContext.builder()
                                                .gameId(gameId)
                                                .myId(myId)
                                                .state(game.getGameState())
                                                .configuration(game.getConfiguration())
                                                .build()
                                ));
                            })).repeat();
                });
    }

    public Mono<GameResponse> getCurrentGame(String gameId, String myId) {
        return gameSetupRepository.findById(gameId)
                .map(GameSetup::toGame)
                .flatMap(game -> commandRepository.readAll(gameId)
                        .map(Record::getValue)
                        .collectList()
                        .flatMap(commands -> {
                                    this.processCommands(game, commands);
                                    return Mono.just(GameResponse.fromParams(
                                            GameResponse.GameContext.builder()
                                                    .gameId(gameId)
                                                    .myId(myId)
                                                    .state(game.getGameState())
                                                    .configuration(game.getConfiguration())
                                                    .build()
                                    ));
                                }
                        )
                );
    }

    private void processCommands(GameEngine gameEngine, List<Command> commands) {
        gameEngine.applyActions(commands.stream().map(Command::toAction));
    }

    public String generatePlayerId() {
        return generateId();
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public Mono<Boolean> exists(String gameId) {
        return this.gameSetupRepository.exists(gameId);
    }
}
