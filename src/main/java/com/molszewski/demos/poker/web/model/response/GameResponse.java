package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.GameState;
import com.molszewski.demos.poker.core.player.Player;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
public record GameResponse(
        List<PlayerResponse> players,
        GameConfiguration configuration,
        GameState state,
        String currentPlayerId,
        int cardsInDeck,
        int discardedCards
) {
    public static GameResponse fromGame(Game game) {
        return GameResponse.builder()
                .state(game.getGameState())
                .configuration(game.getConfiguration())
                .cardsInDeck(game.getCardsInDeck())
                .discardedCards(game.getDiscardedCards())
                .currentPlayerId(Optional.ofNullable(game.getCurrentPlayer()).map(Player::getId).orElse(null))
                .players(game.getPlayers().stream().map(PlayerResponse::fromPlayer).toList())
                .build();
    }
}
