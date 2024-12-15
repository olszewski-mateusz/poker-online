package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.player.Player;
import com.molszewski.demos.poker.persistence.metadata.MetadataCollector;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
public record GameResponse(
        String gameId,
        String myId,
        GamePhase state,
        String currentPlayerId,
        List<PlayerResponse> players,
        List<HistoryEntry> history,
        int cardsInDeck,
        int discardedCards,
        GameConfiguration configuration
        ) {
    public static GameResponse fromParams(Params params) {
        return GameResponse.builder()
                .gameId(params.gameId)
                .myId(params.myId)
                .state(params.game.getGameState())
                .currentPlayerId(Optional.ofNullable(params.game.getCurrentPlayer()).map(Player::getId).orElse(null))
                .players(params.game.getPlayers().stream()
                        .map(player -> PlayerResponse.fromPlayer(player, params.metadataCollector.getPlayerMetadata(player.getId())))
                        .toList())
                .cardsInDeck(params.game.getCardsInDeck())
                .discardedCards(params.game.getDiscardedCards())
                .history(params.history)
                .configuration(params.game.getConfiguration())
                .build();
    }

    @Builder
    public record Params(
            String gameId,
            String myId,
            Game game,
            MetadataCollector metadataCollector,
            List<HistoryEntry> history
    ) {
    }
}
