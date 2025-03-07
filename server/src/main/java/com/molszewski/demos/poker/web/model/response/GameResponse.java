package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.player.Player;
import com.molszewski.demos.poker.web.collector.history.entry.HistoryEntry;
import com.molszewski.demos.poker.web.collector.metadata.MetadataCollector;
import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
public record GameResponse(
        String gameId,
        String myId,
        Integer myIndex,
        GamePhase phase,
        Integer currentPlayerIndex,
        Integer winnerIndex,
        List<PlayerResponse> players,
        List<HistoryEntry> history,
        int cardsInDeck,
        int discardedCards,
        boolean betPlacedInCurrentPhase,
        GameConfiguration configuration
) {
    public static GameResponse fromParams(Params params) {

        boolean isShowdown = params.game.getPhase().equals(GamePhase.SHOWDOWN);

        Integer currentPlayerIndex = Optional.ofNullable(params.game.getCurrentPlayer())
                .map(Player::getId)
                .map(params.metadataCollector::getPlayerIndex)
                .orElse(null);

        Integer winnerIndex = params.game.getGameState().getWinner()
                .map(Player::getId)
                .map(params.metadataCollector::getPlayerIndex)
                .orElse(null);

        return GameResponse.builder()
                .gameId(params.gameId)
                .myId(params.myId)
                .myIndex(params.metadataCollector.getPlayerIndex(params.myId))
                .phase(params.game.getPhase())
                .currentPlayerIndex(currentPlayerIndex)
                .winnerIndex(winnerIndex)
                .players(params.game.getPlayers().stream()
                        .map(player -> {
                            boolean hideCards = !(isShowdown || player.getId().equals(params.myId));
                            return PlayerResponse.fromPlayer(player, params.metadataCollector.getPlayerMetadata(player.getId()), hideCards);
                        })
                        .toList())
                .cardsInDeck(params.game.getCardsInDeck())
                .discardedCards(params.game.getDiscardedCards())
                .history(params.history)
                .betPlacedInCurrentPhase(params.metadataCollector.isBetPlacedInCurrentPhase())
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
