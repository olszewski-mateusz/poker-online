package com.molszewski.demos.poker.core.game.state.action;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.exception.PlayerNotFound;
import com.molszewski.demos.poker.core.hand.Hand;
import com.molszewski.demos.poker.core.player.Player;

import java.util.HashSet;
import java.util.Set;

public final class Replace extends Action{

    private final Set<Card> cardsToReplace;

    public Replace(String playerId, Set<Card> cardsToReplace) {
        super(playerId);
        this.cardsToReplace = cardsToReplace;
    }

    @Override
    public void execute(GameState gameState, GameConfiguration configuration) throws ActionException {
        Player player = gameState.getPlayerById(this.getPlayerId())
                .orElseThrow(() -> new PlayerNotFound(this.getPlayerId()));
        player.setReady(true);
        Set<Card> cardsInHand = new HashSet<>(player.getHand().getCards());
        if (!cardsInHand.containsAll(cardsToReplace)) {
            throw new ActionException("Cards to replace are not in hand");
        }

        cardsInHand.removeAll(cardsToReplace);
        gameState.getDeck().addAllToDiscards(cardsToReplace);

        for (int i = 0; i < cardsToReplace.size(); i++) {
            cardsInHand.add(gameState.getDeck().pop());
        }

        player.setHand(new Hand(cardsInHand));
    }
}
