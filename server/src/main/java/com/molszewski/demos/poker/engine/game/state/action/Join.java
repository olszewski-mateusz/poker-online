package com.molszewski.demos.poker.engine.game.state.action;

import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.state.GamePhase;
import com.molszewski.demos.poker.engine.game.state.exception.GameStateException;
import com.molszewski.demos.poker.engine.player.Player;

public final class Join extends Action {
    private final String displayName;

    public Join(String playerId, String displayName) {
        super(playerId);
        this.displayName = displayName;
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws GameStateException {
        if (gameState.playerExists(getPlayerId())) {
            throw new GameStateException("Player already in game");
        }
        if (gameState.getPlayers().size() >= configuration.maxPlayers()) {
            throw new GameStateException("Game is full");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        gameState.getHistory().addAction(this);
        gameState.addPlayer(this.getPlayerId(), this.displayName, configuration.startChips());
        if (isActiveRound(gameState.getGamePhase())) {
            Player player = gameState.getPlayerById(this.getPlayerId());
            player.setFolded(true);
        }
    }

    private boolean isActiveRound(GamePhase gamePhase) {
        return gamePhase == GamePhase.FIRST_BETTING
                || gamePhase == GamePhase.DRAWING
                || gamePhase == GamePhase.SECOND_BETTING;
    }
}
