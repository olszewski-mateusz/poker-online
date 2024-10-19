package com.molszewski.demos.poker.web.service;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.persistence.entity.GameSetup;
import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.repository.CommandRepository;
import com.molszewski.demos.poker.persistence.repository.GameSetupRepository;
import com.molszewski.demos.poker.web.model.response.CommandResponse;
import com.molszewski.demos.poker.web.model.response.GameResponse;
import com.molszewski.demos.poker.web.model.response.NewGameResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Mono<CommandResponse> handleCommand(String gameId, Command newCommand) {
        return gameSetupRepository.findById(gameId)
                .flatMap(gameSetup -> commandRepository.readAllNonBlocking(gameId)
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
                            return commandRepository.send(gameId, newCommand).thenReturn(new CommandResponse(newCommand.getPlayerId()));
                        }

                        return Mono.error(new IllegalStateException("Illegal request"));
                    })
                );
    }

    public Flux<GameResponse> subscribeToGameChanges(String gameId) {
        final AtomicReference<StreamOffset<String>> currentOffset = new AtomicReference<>(StreamOffset.fromStart(commandRepository.getStreamKey(gameId)));

        return gameSetupRepository.findById(gameId)
                .map(gameSetup -> gameSetup.toGame(new Random()))
                .flatMapMany(game -> Mono.defer(() -> commandRepository.readFromBlocking(currentOffset.get()).collectList().flatMap(objectRecords -> {
                            Record<String, Command> lastRecord = null;
                            for (Record<String, Command> commandRecord : objectRecords) {
                                lastRecord = commandRecord;
                                Command command = commandRecord.getValue();
                                boolean success = game.applyAction(command.toAction());
                                if (!success) {
                                    log.warn("Illegal command found in stream");
                                }
                            }
                            if (lastRecord != null) {
                                currentOffset.set(StreamOffset.from(lastRecord));
                            }
                            return Mono.just(GameResponse.fromGame(game));
                        })).repeat()
                );
    }

    public String generatePlayerId() {
        return generateId();
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
