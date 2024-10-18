package com.molszewski.demos.poker.core.game.state.transition;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.GameState;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.player.Player;

import java.util.List;

public final class NextTurn extends Transition {

    NextTurn() {
    }

    @Override
    protected void applyLogic(StateManager stateManager, Board board, GameConfiguration configuration) {
        nextTurn(stateManager, board);
    }

    private void nextTurn(StateManager stateManager, Board board) {
        nextPlayer(stateManager, board);
        if (board.getPlayers().stream().allMatch(Player::isReady)) {

            switch (stateManager.getState()) {
                case FIRST_BETTING -> {
                    stateManager.setState(GameState.CARD_EXCHANGE);
                    board.getPlayers().forEach(player -> player.setReady(false));
                }
                case CARD_EXCHANGE -> {
                    stateManager.setState(GameState.SECOND_BETTING);
                    board.getPlayers().forEach(player -> player.setReady(false));
                }
                case SECOND_BETTING -> {
                    stateManager.setState(GameState.FINISHED);
                    board.getPlayers().forEach(player -> player.setReady(false));
                }
                default -> throw new IllegalStateException("Unexpected state: " + stateManager.getState());
            }}
    }


    private void nextPlayer(StateManager stateManager, Board board) {
        List<Player> players = board.getPlayers();
        Player currentPlayer = stateManager.getCurrentPlayer();

        if (players.isEmpty()) {
            throw new IllegalStateException("No players in game");
        }

        if (currentPlayer == null) {
            throw new IllegalStateException("Current player is null");
        }

        List<Player> playersInGame = players.stream()
                .filter(player -> !player.isFolded() || player == currentPlayer)
                .toList();
        
        stateManager.setCurrentPlayer(playersInGame.get((playersInGame.indexOf(currentPlayer) + 1) % playersInGame.size()));
    }
}
