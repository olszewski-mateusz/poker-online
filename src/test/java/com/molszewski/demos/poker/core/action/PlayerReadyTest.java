package com.molszewski.demos.poker.core.action;

import com.molszewski.demos.poker.core.game.Game;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.player.Player;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerReadyTest {
    private final Random random = new Random(17L);
    private final GameConfiguration configuration = GameConfiguration.defaultConfiguration();


    @Test
    void onePlayerReady() {
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();
        UUID playerC = UUID.randomUUID();

        Game game = new Game(random, configuration);
        game.applyAction(new AddPlayer(playerA));
        game.applyAction(new AddPlayer(playerB));
        game.applyAction(new AddPlayer(playerC));

        game.applyAction(new PlayerReady(playerB));

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
        game.applyAction(new AddPlayer(playerA));
        game.applyAction(new AddPlayer(playerB));
        game.applyAction(new AddPlayer(playerC));
        game.applyAction(new AddPlayer(playerD));

        game.applyAction(new PlayerReady(playerB));
        game.applyAction(new PlayerReady(playerA));
        game.applyAction(new PlayerReady(playerC));

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
        game.applyAction(new AddPlayer(playerA));
        game.applyAction(new AddPlayer(playerB));
        game.applyAction(new AddPlayer(playerC));
        game.applyAction(new AddPlayer(playerD));

        game.applyAction(new PlayerReady(playerB));
        game.applyAction(new PlayerReady(playerA));
        game.applyAction(new PlayerReady(playerC));
        game.applyAction(new PlayerReady(playerD));

        assertEquals(GameState.FIRST_BETTING, game.getGameState());
        assertEquals(4, game.getPlayers().stream().filter(Player::isReady).count());
        assertEquals(5, game.getPlayers().get(0).getHand().getCards().size());
        assertEquals(5, game.getPlayers().get(1).getHand().getCards().size());
        assertEquals(5, game.getPlayers().get(2).getHand().getCards().size());
        assertEquals(5, game.getPlayers().get(3).getHand().getCards().size());
        assertEquals(playerA, game.getCurrentPlayer().getId());
    }
}