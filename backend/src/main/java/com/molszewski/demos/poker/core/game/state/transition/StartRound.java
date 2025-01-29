package com.molszewski.demos.poker.core.game.state.transition;

import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.player.Player;

import java.util.List;

public final class StartRound implements Transition {

    private StartRound() {
    }

    public static StartRound of() {
        return new StartRound();
    }

    @Override
    public void apply(GameState gameState, GameConfiguration configuration) {
        long playersReady = gameState.getPlayers().stream().filter(Player::isReady).count();
        if (playersReady >= configuration.minPlayersToStart() && playersReady == gameState.getPlayers().size()) {
            applyStartGame(gameState, configuration);
        }
    }

    private void applyStartGame(GameState state, GameConfiguration configuration) {
        if (state.getGamePhase().equals(GamePhase.SHOWDOWN)) {
            reorderPlayers(state);
            collectBet(state);
        }
        state.getPlayers().forEach(player -> player.setFolded(false));
        state.getPlayers().forEach(player -> player.setReady(false));
        disablePlayersWithoutMoney(state);
        state.setCurrentPlayer(state.getPlayers().stream().filter(player -> !player.isFolded()).findFirst().orElse(null));
        if (isGameFinished(state)) {
            state.setGamePhase(GamePhase.FINISHED);
            return;
        }
        state.setGamePhase(GamePhase.FIRST_BETTING);
        giveCardsToPlayers(state);
        collectAnte(state, configuration);
    }

    private boolean isGameFinished(GameState state) {
        return state.getPlayers().stream().filter(player -> !player.isFolded()).count() == 1;
    }

    private void collectBet(GameState gameState) {
        Player winner = gameState.getWinner().orElseThrow(() -> new IllegalStateException("No winner found in game"));
        int collectedMoney = gameState.getPlayers().stream().map(Player::clearAndGetBet).reduce(0, Integer::sum);
        winner.addMoney(collectedMoney);
    }

    private void disablePlayersWithoutMoney(GameState gameState) {
        gameState.getPlayers().stream().filter(player -> player.getMoney() == 0).
                forEach(player -> player.setFolded(true));
    }

    private void reorderPlayers(GameState gameState) {
        List<Player> players = gameState.getPlayers();
        players.addLast(players.removeFirst());
    }

    private void giveCardsToPlayers(GameState gameState) {
        for (Player player : gameState.getPlayers()) {
            if (player.getHand() != null) {
                gameState.getDeck().addAllToDiscards(player.getHand().getCards());
                player.setHand(null);
            }
            if (!player.isFolded()) {
                player.setHand(gameState.getDeck().getHand());
            }
        }
    }

    private void collectAnte(GameState gameState, GameConfiguration configuration) {
        for (Player player : gameState.getPlayers().stream().filter(player -> !player.isFolded()).toList()) {
            player.transferMoneyToBet(configuration.ante());
        }
    }
}
