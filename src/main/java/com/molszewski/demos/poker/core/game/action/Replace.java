package com.molszewski.demos.poker.core.game.action;

import com.molszewski.demos.poker.core.card.Card;
import com.molszewski.demos.poker.core.game.Board;
import com.molszewski.demos.poker.core.game.GameConfiguration;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Replace extends Action{

    private final Set<Card> cardsToReplace;

    public Replace(UUID playerId, Set<Card> cardsToReplace) {
        super(playerId);
        this.cardsToReplace = cardsToReplace;
    }

    @Override
    public void execute(Board board, GameConfiguration configuration) throws ActionException {
        board.getPlayerById(getPlayerId()).setReady(true);
        Set<Card> currentPlayerCards = new HashSet<>(board.getPlayerById(getPlayerId()).getHand().getCards());
        if (!currentPlayerCards.containsAll(cardsToReplace)) {
            throw new ActionException("Cards to replace are not in hand");
        }

        currentPlayerCards.removeAll(cardsToReplace);

        for (int i = 0; i < 5 - currentPlayerCards.size(); i++) {
            currentPlayerCards.add(board.getDeck().pop());
        }
    }
}
