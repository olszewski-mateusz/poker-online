package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.action.Join;
import com.molszewski.demos.poker.core.game.action.Ready;
import com.molszewski.demos.poker.core.game.state.GameState;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReadyTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();


    @Test
    void onePlayerReady() {
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();
        UUID playerC = UUID.randomUUID();

        Game game = new Game(random, configuration);
        game.applyAction(new Join(playerA));
        game.applyAction(new Join(playerB));
        game.applyAction(new Join(playerC));

        game.applyAction(new Ready(playerB));

        assertEquals(GameState.NOT_STARTED, game.getGameState());
        assertEquals(1, game.getPlayers().stream().filter(Player::isReady).count());
    }

    @Test
    void minPlayersReadyButNotAll() {
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();
        UUID playerC = UUID.randomUUID();
        UUID playerD = UUID.randomUUID();

        Game game = new Game(random, configuration);
        game.applyAction(new Join(playerA));
        game.applyAction(new Join(playerB));
        game.applyAction(new Join(playerC));
        game.applyAction(new Join(playerD));

        game.applyAction(new Ready(playerB));
        game.applyAction(new Ready(playerA));
        game.applyAction(new Ready(playerC));

        assertEquals(GameState.NOT_STARTED, game.getGameState());
        assertEquals(3, game.getPlayers().stream().filter(Player::isReady).count());
        assertTrue(game.getPlayers().size() >= configuration.minPlayersToStartGame());
    }

    @Test
    void allPlayerReady() {
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();
        UUID playerC = UUID.randomUUID();
        UUID playerD = UUID.randomUUID();

        Game game = new Game(random, configuration);
        game.applyAction(new Join(playerA));
        game.applyAction(new Join(playerB));
        game.applyAction(new Join(playerC));
        game.applyAction(new Join(playerD));

        game.applyAction(new Ready(playerB));
        game.applyAction(new Ready(playerA));
        game.applyAction(new Ready(playerC));
        game.applyAction(new Ready(playerD));

        assertEquals(GameState.FIRST_BETTING, game.getGameState());
        assertEquals(4, game.getPlayers().stream().filter(Player::isReady).count());
        assertEquals(5, game.getPlayers().get(0).getHand().getCards().size());
        assertEquals(5, game.getPlayers().get(1).getHand().getCards().size());
        assertEquals(5, game.getPlayers().get(2).getHand().getCards().size());
        assertEquals(5, game.getPlayers().get(3).getHand().getCards().size());
        assertEquals(playerA, game.getCurrentPlayer().getId());
    }
}