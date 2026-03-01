package com.molszewski.demos.poker.engine.action;

import com.molszewski.demos.poker.engine.card.Card;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.game.GameConfiguration;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.action.step.ActionSteps;
import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.exception.PlayerNotFound;
import com.molszewski.demos.poker.engine.hand.Hand;
import com.molszewski.demos.poker.engine.player.Player;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public final class Replace extends Action{

    @Getter
    private final Set<Card> cardsToReplace;

    public Replace(String playerId, Set<Card> cardsToReplace) {
        super(playerId);
        this.cardsToReplace = cardsToReplace;
    }

    @Override
    protected void validate(GameState gameState, GameConfiguration configuration) throws ActionValidationException {
        gameState.validatePhase(Set.of(GamePhase.DRAWING));
        if (!gameState.playerExists(getPlayerId())) {
            throw new PlayerNotFound(getPlayerId());
        }
        gameState.validateCurrentPlayer(getPlayerId());

        Player player = gameState.getPlayerById(this.getPlayerId());
        Set<Card> cardsInHand = new HashSet<>(player.getHand().getCards());
        if (!cardsInHand.containsAll(cardsToReplace)) {
            throw new ActionValidationException("Cards to replace are not in hand");
        }
    }

    @Override
    protected void execute(GameState gameState, GameConfiguration configuration)  {
        gameState.getHistory().addAction(this);
        Player player = gameState.getPlayerById(this.getPlayerId());
        player.setReady(true);
        Set<Card> cardsInHand = new HashSet<>(player.getHand().getCards());
        cardsInHand.removeAll(cardsToReplace);
        gameState.getDeck().addAllToDiscards(cardsToReplace);

        for (int i = 0; i < cardsToReplace.size(); i++) {
            cardsInHand.add(gameState.getDeck().pop());
        }

        player.setHand(new Hand(cardsInHand));
        ActionSteps.nextTurn(gameState);
    }
}
