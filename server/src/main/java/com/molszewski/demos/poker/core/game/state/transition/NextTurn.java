package com.molszewski.demos.poker.core.game.state.transition;

import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.state.GamePhase;
import com.molszewski.demos.poker.core.player.Player;

import java.util.List;

public final class NextTurn implements Transition {

    private NextTurn() {
    }

    public static NextTurn of() {
        return new NextTurn();
    }

    @Override
    public void apply(GameState gameState, GameConfiguration configuration) {
        nextTurn(gameState);
    }

    private void nextTurn(GameState state) {
        nextPlayer(state);
        if (state.getPlayers().stream().allMatch(player -> player.isReady() || player.isFolded())) {

            if (state.getPlayers().stream().filter(player -> !player.isFolded()).count() == 1 ||
                state.getGamePhase().equals(GamePhase.DRAWING) && state.getPlayers().stream().filter(player -> !player.isFolded() && player.getMoney() > 0).count() <= 1) {

                state.setGamePhase(GamePhase.SHOWDOWN);
                state.getPlayers().forEach(player -> player.setReady(false));
                state.setCurrentPlayer(null);
                return;
            }

            switch (state.getGamePhase()) {
                case FIRST_BETTING -> {
                    state.setGamePhase(GamePhase.DRAWING);
                    state.getPlayers().forEach(player -> player.setReady(false));
                }
                case DRAWING -> {
                    state.setGamePhase(GamePhase.SECOND_BETTING);
                    state.getPlayers().forEach(player -> {
                        if (player.getMoney() > 0) {
                            player.setReady(false);
                        }
                    });
                }
                case SECOND_BETTING -> {
                    state.setGamePhase(GamePhase.SHOWDOWN);
                    state.getPlayers().forEach(player -> player.setReady(false));
                    state.setCurrentPlayer(null);
                }
                default -> throw new IllegalStateException("Unexpected phase: " + state.getGamePhase());
            }
        }
    }


    private void nextPlayer(GameState state) {
        List<Player> players = state.getPlayers();
        Player currentPlayer = state.getCurrentPlayer();

        if (players.isEmpty()) {
            throw new IllegalStateException("No players in game");
        }

        if (currentPlayer == null) {
            throw new IllegalStateException("Current player is null");
        }

        List<Player> playersInGame;
        if (state.getGamePhase().equals(GamePhase.FIRST_BETTING) || state.getGamePhase().equals(GamePhase.SECOND_BETTING)) {
            playersInGame = players.stream()
                    .filter(player -> (!player.isFolded() && player.getMoney() > 0) || player == currentPlayer)
                    .toList();
        } else {
            playersInGame = players.stream()
                    .filter(player -> !player.isFolded() || player == currentPlayer)
                    .toList();
        }

        state.setCurrentPlayer(playersInGame.get((playersInGame.indexOf(currentPlayer) + 1) % playersInGame.size()));
    }
}
