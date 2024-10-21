package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.GameState;
import com.molszewski.demos.poker.core.player.Player;
import com.molszewski.demos.poker.persistence.metadata.MetadataCollector;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
public record GameResponse(
        List<PlayerResponse> players,
        List<CommandResponse> history,
        GameConfiguration configuration,
        GameState state,
        String currentPlayerId,
        int cardsInDeck,
        int discardedCards
) {
    public static GameResponse fromGame(Game game, MetadataCollector metadataCollector, List<CommandResponse> history) {
        return GameResponse.builder()
                .state(game.getGameState())
                .configuration(game.getConfiguration())
                .history(history)
                .cardsInDeck(game.getCardsInDeck())
                .discardedCards(game.getDiscardedCards())
                .currentPlayerId(Optional.ofNullable(game.getCurrentPlayer()).map(Player::getId).orElse(null))
                .players(game.getPlayers().stream()
                        .map(player -> PlayerResponse.fromPlayer(player, metadataCollector.getPlayerMetadata(player.getId())))
                        .toList())
                .build();
    }
}
