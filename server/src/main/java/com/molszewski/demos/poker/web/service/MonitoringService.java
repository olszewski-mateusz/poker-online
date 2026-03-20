package com.molszewski.demos.poker.web.service;

import com.molszewski.demos.poker.engine.ai.AiConfiguration;
import com.molszewski.demos.poker.engine.ai.AiState;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.persistence.entity.GameSetup;
import com.molszewski.demos.poker.persistence.entity.command.Command;
import com.molszewski.demos.poker.persistence.repository.CommandRepository;
import com.molszewski.demos.poker.persistence.repository.GameSetupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitoringService {
    private final GameSetupRepository gameSetupRepository;
    private final CommandRepository commandRepository;
    private final AiService aiService;

    private final ConcurrentHashMap<String, GameSubscription> subscriptions = new ConcurrentHashMap<>();

    public void keepAlive(String gameId) {
        log.info("Keep alive for game: {}", gameId);
        subscriptions.compute(gameId, (key, gameSubscription) ->
                gameSubscription == null
                        ? new GameSubscription(System.currentTimeMillis(), subscribeToGame(key).subscribe())
                        : new GameSubscription(System.currentTimeMillis(), gameSubscription.disposable())
        );
    }

    private Flux<Void> subscribeToGame(String gameId) {
        final AtomicReference<StreamOffset<String>> currentOffset = new AtomicReference<>(StreamOffset.fromStart(commandRepository.getStreamKey(gameId)));
        final AtomicLong lastActionTime = new AtomicLong(System.currentTimeMillis());
        log.info("Subscribing internally to game {}", gameId);
        return gameSetupRepository.findById(gameId)
                .map(GameSetup::toGame)
                .flatMapMany(game -> {
                    return Mono.defer(() -> commandRepository.readFromOffsetWithBlock(currentOffset.get()).collectList()
                            .flatMap(objectRecords -> {
                                if (objectRecords.isEmpty()) {
                                    if (System.currentTimeMillis() - lastActionTime.get() > TimeUnit.SECONDS.toMillis(90)) {
                                        log.info("Game {} is idle", gameId);
                                        // todo: add some timeout actions
                                        lastActionTime.set(System.currentTimeMillis());
                                    }
                                    return Mono.empty().then();
                                }
                                lastActionTime.set(System.currentTimeMillis());
                                currentOffset.set(StreamOffset.from(objectRecords.getLast()));

                                List<Command> commands = objectRecords.stream().map(Record::getValue).toList();
                                log.info("Game {} new commands: {}", gameId, commands);
                                game.applyActions(commands.stream().map(Command::toAction));

                                Player player = game.getGameState().getPlayerState().getCurrentPlayer();
                                AiState aiState = game.getGameState().getAiState();
                                if (player != null && aiState.isPlayerAi(player.getId())) {
                                    AiConfiguration configuration = aiState.getAi(player);
                                    return aiService.sendRequestToAi(gameId, player.getId(), configuration, game)
                                            .doOnError(throwable ->  log.error("Error sending request to AI", throwable))
                                            .onErrorReturn(Void.TYPE.cast("void"));
                                }

                                return Mono.empty();
                            })).repeat();
                }).doOnError(throwable -> log.warn("Error thrown during internal subscription: {}", throwable.getMessage()));
    }

    @Scheduled(cron = "0 * * * * *")
    public void monitor() {
        log.info("Monitoring triggered");
        this.subscriptions.forEach((key, value) -> {

            if (System.currentTimeMillis() - value.accessTime() > TimeUnit.MINUTES.toMillis(3)) {
                if (!value.disposable().isDisposed()) {
                    value.disposable().dispose();
                    log.info("Disposing inactive game subscription: {}", key);
                } else {
                    log.info("Removing already disposed inactive game subscription: {}", key);
                }
                this.subscriptions.remove(key);
            } else {
                if (value.disposable().isDisposed()) {
                    log.info("Game subscription {} is disposed. Restarting", key);
                    subscriptions.computeIfPresent(key, (k, gameSubscription) ->  new GameSubscription(System.currentTimeMillis(), subscribeToGame(key).subscribe()));
                }
            }
        });
    }

    private record GameSubscription(
            long accessTime,
            Disposable disposable
    ) {}
}
