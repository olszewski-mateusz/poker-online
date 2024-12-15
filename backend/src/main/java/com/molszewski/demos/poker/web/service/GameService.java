package com.molszewski.demos.poker.web.service;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.persistence.entity.GameSetup;
import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.metadata.MetadataCollector;
import com.molszewski.demos.poker.persistence.repository.CommandRepository;
import com.molszewski.demos.poker.persistence.repository.GameSetupRepository;
import com.molszewski.demos.poker.web.model.response.ActionResponse;
import com.molszewski.demos.poker.web.model.response.GameResponse;
import com.molszewski.demos.poker.web.model.response.HistoryEntry;
import com.molszewski.demos.poker.web.model.response.NewGameResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
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

    public Mono<NewGameResponse> createGame() {
        return gameSetupRepository.save(GameSetup.init(generateId(), new Random()))
                .map(NewGameResponse::new);
    }

    public Mono<ActionResponse> handleCommand(String gameId, Command newCommand) {
        return gameSetupRepository.findById(gameId)
                .flatMap(gameSetup -> commandRepository.readAll(gameId)
                        .map(Record::getValue)
                        .collectList()
                        .flatMap(commands -> {
                            Game game = gameSetup.toGame(new Random());
                            for (Command command : commands) {
                                boolean success = game.applyAction(command.toAction());
                                if (!success) {
                                    log.warn("Illegal command found in stream");
                                }
                            }
                            boolean success = game.applyAction(newCommand.toAction());
                            if (success) {
                                return commandRepository.send(gameId, newCommand).thenReturn(new ActionResponse(newCommand.getPlayerId()));
                            }

                            return Mono.error(new IllegalStateException("Illegal request"));
                        })
                ).doOnError(e -> log.warn(e.getMessage()));
    }

    public Flux<GameResponse> subscribeToGameChanges(String gameId, String myId) {
        final AtomicReference<StreamOffset<String>> currentOffset = new AtomicReference<>(StreamOffset.fromStart(commandRepository.getStreamKey(gameId)));

        return gameSetupRepository.findById(gameId)
                .map(gameSetup -> gameSetup.toGame(new Random()))
                .flatMapMany(game -> {
                    MetadataCollector metadataCollector = MetadataCollector.newInstance();
                    List<HistoryEntry> history = new ArrayList<>();
                    return Mono.defer(() -> commandRepository.readFromOffsetWithBlock(currentOffset.get()).collectList()
                            .filter(objectRecords -> !objectRecords.isEmpty())
                            .flatMap(objectRecords -> {
                                List<Command> commands = objectRecords.stream().map(Record::getValue).toList();
                                history.addAll(this.processCommands(game, commands, metadataCollector));
                                if (!objectRecords.isEmpty()) {
                                    currentOffset.set(StreamOffset.from(objectRecords.getLast()));
                                }
                                return Mono.just(GameResponse.fromParams(
                                        GameResponse.Params.builder()
                                                .gameId(gameId)
                                                .myId(myId)
                                                .game(game)
                                                .history(history)
                                                .metadataCollector(metadataCollector)
                                                .build()
                                ));
                            })).repeat();
                });
    }

    public Mono<GameResponse> getCurrentGame(String gameId, String myId) {
        return gameSetupRepository.findById(gameId)
                .map(gameSetup -> gameSetup.toGame(new Random()))
                .flatMap(game -> commandRepository.readAll(gameId)
                        .map(Record::getValue)
                        .collectList()
                        .flatMap(commands -> {
                                    MetadataCollector metadataCollector = MetadataCollector.newInstance();
                                    List<HistoryEntry> history = this.processCommands(game, commands, metadataCollector);
                                    return Mono.just(GameResponse.fromParams(
                                            GameResponse.Params.builder()
                                                    .gameId(gameId)
                                                    .myId(myId)
                                                    .game(game)
                                                    .history(history)
                                                    .metadataCollector(metadataCollector)
                                                    .build()
                                    ));
                                }
                        )
                );
    }

    private List<HistoryEntry> processCommands(Game game, List<Command> commands, MetadataCollector metadataCollector) {
        List<HistoryEntry> history = new ArrayList<>();
        for (Command command : commands) {
            boolean success = game.applyAction(command.toAction());
            if (!success) {
                log.warn("Illegal command found in stream");
            } else {
                metadataCollector.includeCommand(command);
                history.add(HistoryEntry.fromCommand(command, metadataCollector));
            }
        }
        return history;
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
