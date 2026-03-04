package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.player.PlayerState;
import lombok.Builder;

import java.util.List;

@Builder
public record GameResponse(
        String gameId,
        String myId,
        Integer myIndex,
        GamePhase phase,
        Integer currentPlayerIndex,
        List<Integer> winnerIndexes,
        List<PlayerResponse> players,
        List<HistoryEntryResponse> history,
        int cardsInDeck,
        int discardedCards,
        boolean betPlacedInCurrentPhase,
        GameConfiguration configuration,
        List<ValidActionsResponse.ValidAction> validActions
) {
    public static GameResponse fromParams(GameContext context) {
        PlayerState playerState = context.state.getPlayerState();
        List<Player> players = playerState.getAll();

        boolean isShowdown = context.state.getGamePhase().equals(GamePhase.SHOWDOWN);

        Integer currentPlayerIndex = playerState.getCurrentPlayerIndex();

        List<Integer> winnerIndexes = playerState.getWinners().stream()
                .map(player -> playerState.getIndex(player.getId()))
                .toList();

        return GameResponse.builder()
                .gameId(context.gameId)
                .myId(context.myId)
                .myIndex(playerState.getIndex(context.myId))
                .phase(context.state.getGamePhase())
                .currentPlayerIndex(currentPlayerIndex)
                .winnerIndexes(winnerIndexes)
                .players(players.stream()
                        .map(player -> {
                            boolean hideCards = !(isShowdown || player.getId().equals(context.myId));
                            return PlayerResponse.fromPlayer(player, playerState.getIndex(player.getId()), hideCards, context.state.getPotState().getPlayerContribution(player));
                        })
                        .toList())
                .cardsInDeck(context.state.getDeck().cardsCount())
                .discardedCards(context.state.getDeck().discardsCount())
                .history(context.state.getHistory().getEntries().stream()
                        .map(HistoryEntryResponse::fromHistoryEntry)
                        .toList())
                .betPlacedInCurrentPhase(context.state.getPotState().isBetPlaced())
                .configuration(context.configuration)
                .validActions(ValidActionsResponse.fromGameState(context.myId, context.state, context.configuration).validActions())
                .build();
    }

    @Builder
    public record GameContext(
            String gameId,
            String myId,
            GameState state,
            GameConfiguration configuration
    ) {
    }
}
