package com.molszewski.demos.poker.core.game.state.transition;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.GameState;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.player.Player;

public final class StartGame extends Transition {

    StartGame() {
    }

    @Override
    protected void applyLogic(StateManager stateManager, Board board, GameConfiguration configuration) {
        long playersReady = board.getPlayers().stream().filter(Player::isReady).count();
        if (playersReady >= configuration.minPlayersToStartGame() && playersReady == board.getPlayers().size()) {
            applyStartGame(stateManager, board, configuration);
        }
    }

    private void applyStartGame(StateManager stateManager, Board board, GameConfiguration configuration) {
        stateManager.setState(GameState.FIRST_BETTING);
        stateManager.setCurrentPlayer(board.getPlayers().getFirst());
        giveCardsToPlayers(board);
        takeInitialMoney(board, configuration);
        board.getPlayers().forEach(player -> player.setReady(false));
    }

    private void giveCardsToPlayers(Board board) {
        for (Player player : board.getPlayers()) {
            player.setHand(board.getDeck().getHand());
        }
    }

    private void takeInitialMoney(Board board, GameConfiguration configuration) {
        for (Player player : board.getPlayers()) {
            player.moveMoneyToBid(configuration.minBet());
        }
    }
}
