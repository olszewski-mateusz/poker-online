package com.molszewski.demos.poker.core.game.state.transition;

import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.GameState;
import com.molszewski.demos.poker.core.game.state.StateManager;
import com.molszewski.demos.poker.core.player.Player;

import java.util.List;

public final class StartGame extends Transition {

    StartGame() {
    }

    @Override
    protected void applyLogic(StateManager stateManager, Board board, GameConfiguration configuration) {
        long playersReady = board.getPlayers().stream().filter(Player::isReady).count();
        if (playersReady >= configuration.minPlayersToStart() && playersReady == board.getPlayers().size()) {
            applyStartGame(stateManager, board, configuration);
        }
    }

    private void applyStartGame(StateManager stateManager, Board board, GameConfiguration configuration) {
        if (stateManager.getState().equals(GameState.SHOWDOWN)) {
            reorderPlayers(board);
            collectBet(board);
            disablePlayersWithoutMoney(board);
        }
        stateManager.setState(GameState.FIRST_BETTING);
        stateManager.setCurrentPlayer(board.getPlayers().getFirst());
        giveCardsToPlayers(board);
        collectAnte(board, configuration);
        board.getPlayers().forEach(player -> player.setReady(false));
    }

    private void collectBet(Board board) {
        Player winner = board.getWinner();
        int collectedMoney = board.getPlayers().stream().map(Player::collectBet).reduce(0, Integer::sum);
        winner.addMoney(collectedMoney);
    }

    private void disablePlayersWithoutMoney(Board board) {
        board.getPlayers().stream().filter(player -> player.getMoney() == 0).forEach(player -> player.setFolded(true));
    }

    private void reorderPlayers(Board board) {
        List<Player> players = board.getPlayers();
        Player firstPlayer = players.removeFirst();
        players.add(firstPlayer);
    }

    private void giveCardsToPlayers(Board board) {
        for (Player player : board.getPlayers()) {
            player.setHand(board.getDeck().getHand());
        }
    }

    private void collectAnte(Board board, GameConfiguration configuration) {
        for (Player player : board.getPlayers()) {
            player.moveMoneyToBet(configuration.ante());
        }
    }
}
