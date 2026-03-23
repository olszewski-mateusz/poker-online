package com.molszewski.demos.poker.engine.action;

import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.player.Player;
import lombok.Getter;

import java.util.List;

public final class RemovePlayer extends Action {
    @Getter
    private final int playerIndex;

    public RemovePlayer(String requesterPlayerId, int playerIndex) {
        super(requesterPlayerId);
        this.playerIndex = playerIndex;
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws ActionValidationException {
        if (!gameState.getPlayerState().exists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }
        if (!hasValidTargetIndex(gameState)) {
            throw new ActionValidationException("Invalid player index");
        }

        Player targetPlayer = getTargetPlayer(gameState);
        if (targetPlayer.getId().equals(getPlayerId())) {
            throw new ActionValidationException("Cannot remove yourself");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration) {
        Player targetPlayer = getTargetPlayer(gameState);
        gameState.getHistory().addAction(gameState, this);
        Player removedPlayer = gameState.getPlayerState().removeById(targetPlayer.getId(), gameState.getGamePhase());

        if (gameState.getAiState().isPlayerAi(removedPlayer.getId())) {
            gameState.getAiState().removeAi(removedPlayer.getId());
        }
    }

    public Player getTargetPlayer(GameState gameState) {
        List<Player> players = gameState.getPlayerState().getAll();
        if (playerIndex < 0 || playerIndex >= players.size()) {
            throw new IllegalStateException("Invalid player index");
        }
        return players.get(playerIndex);
    }

    private boolean hasValidTargetIndex(GameState gameState) {
        int size = gameState.getPlayerState().size();
        return playerIndex >= 0 && playerIndex < size;
    }
}
