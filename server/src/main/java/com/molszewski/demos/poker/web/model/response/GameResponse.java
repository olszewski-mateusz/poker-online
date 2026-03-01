package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.player.Player;
import lombok.Builder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

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
        GameConfiguration configuration
) {
    public static GameResponse fromParams(GameContext context) {
        List<Player> players = context.state.getPlayers();

        boolean isShowdown = context.state.getGamePhase().equals(GamePhase.SHOWDOWN);

        Integer currentPlayerIndex = Optional.ofNullable(context.state.getCurrentPlayer())
                .map(Player::getId)
                .map(context.state::getPlayerIndex)
                .orElse(null);

//        List<Integer> winnerIndexes = context.state.getWinners().stream()
//                .map(Player::getId)
//                .map(context.state::getPlayerIndex)
//                .toList();

        return GameResponse.builder()
                .gameId(context.gameId)
                .myId(context.myId)
                .myIndex(context.state.getPlayerIndex(context.myId))
                .phase(context.state.getGamePhase())
                .currentPlayerIndex(currentPlayerIndex)
//                .winnerIndexes(winnerIndexes)
                .players(IntStream.range(0, players.size())
                        .mapToObj(index -> {
                            Player player = players.get(index);
                            boolean hideCards = !(isShowdown || player.getId().equals(context.myId));
                            return PlayerResponse.fromPlayer(player, index, hideCards, context.state.getPotState().getPlayerContribution(player));
                        })
                        .toList())
                .cardsInDeck(context.state.getDeck().cardsCount())
                .discardedCards(context.state.getDeck().discardsCount())
                .history(context.state.getHistory().getEntries().stream()
                        .map(entry -> HistoryEntryResponse.fromHistoryEntry(entry, context.state))
                        .filter(Objects::nonNull)
                        .toList())
                .betPlacedInCurrentPhase(context.state.getPotState().isBetPlaced())
                .configuration(context.configuration)
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
