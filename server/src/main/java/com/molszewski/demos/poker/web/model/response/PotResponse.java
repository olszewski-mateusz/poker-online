package com.molszewski.demos.poker.web.model.response;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.engine.player.PlayerState;
import com.molszewski.demos.poker.engine.pot.Pot;
import com.molszewski.demos.poker.engine.pot.PotType;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Builder
public record PotResponse(
        int chips,
        MainPot mainPot,
        List<SidePot> sidePots

) {
    @Builder
    public record MainPot(
            int chips,
            List<Integer> winnerIndexes
    ) {
    }

    @Builder
    public record SidePot(
            int chips,
            List<Integer> eligibleIndexes,
            List<Integer> winnerIndexes
    ) {
    }

    public static PotResponse fromGameState(GameState gameState) {
        PlayerState playerState = gameState.getPlayerState();
        List<Pot> pots = gameState.getPotState().calculatePots(playerState.getAll());

        Pot mainPot = pots.stream().filter(pot -> PotType.MAIN.equals(pot.type())).findFirst().orElse(null);
        MainPot mainPotResponse;
        if (mainPot == null) {
            mainPotResponse = MainPot.builder()
                    .chips(0)
                    .winnerIndexes(List.of())
                    .build();
        } else {
            mainPotResponse = MainPot.builder()
                    .chips(mainPot.getAmount())
                    .winnerIndexes(playerToIndexes(playerState, playerState.getPotWinners(mainPot.eligible())))
                    .build();
        }

        List<Pot> sidePots = pots.stream().filter(pot -> PotType.SIDE.equals(pot.type())).toList();

        List<SidePot> sidePotResponses = new ArrayList<>();

        for (Pot sidePot : sidePots) {
            SidePot sidePotResponse = SidePot.builder()
                    .chips(sidePot.getAmount())
                    .eligibleIndexes(playerIdsToIndexes(playerState, sidePot.eligible()))
                    .winnerIndexes(playerToIndexes(playerState, playerState.getPotWinners(sidePot.eligible())))
                    .build();
            sidePotResponses.add(sidePotResponse);
        }

        int fullAmount = pots.stream().map(Pot::getAmount).reduce(0, Integer::sum);
        return new PotResponse(fullAmount, mainPotResponse, sidePotResponses);
    }

    private static List<Integer> playerIdsToIndexes(PlayerState playerState, Collection<String> ids) {
        return ids.stream().map(playerState::getIndex).toList();
    }

    private static List<Integer> playerToIndexes(PlayerState playerState, Collection<Player> players) {
        return players.stream().map(Player::getId).map(playerState::getIndex).toList();
    }
}
