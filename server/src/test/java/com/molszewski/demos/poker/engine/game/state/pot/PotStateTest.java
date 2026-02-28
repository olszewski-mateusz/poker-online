package com.molszewski.demos.poker.engine.game.state.pot;

import com.molszewski.demos.poker.engine.player.Player;
import com.molszewski.demos.poker.test.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
class PotStateTest {

    @Test
    void fullRaiseAllInStillCreatesSidePotWhenActionContinues() {
        Player p1 = new Player("1", "1", 300, false, false, null);
        Player p2 = new Player("2", "2", 120, false, false, null);
        Player p3 = new Player("3", "3", 300, false, false, null);

        PotState potState = new PotState();
        potState.addBet(p1, 50);
        potState.addAllIn(p2);
        potState.addRaise(p3, 200);
        potState.addCall(p1);

        List<Pot> pots = potState.calculatePots(List.of(p1, p2, p3));

        assertEquals(2, pots.size());
        assertEquals(PotType.MAIN, pots.get(0).type());
        assertEquals(360, pots.get(0).getAmount());
        assertEquals(Set.of("1", "2", "3"), pots.get(0).eligible());

        assertEquals(PotType.SIDE, pots.get(1).type());
        assertEquals(160, pots.get(1).getAmount());
        assertEquals(Set.of("1", "3"), pots.get(1).eligible());
    }

    @Test
    void allInCapsAreAppliedInAscendingOrder() {
        Player p1 = new Player("1", "1", 80, false, false, null);
        Player p2 = new Player("2", "2", 50, false, false, null);
        Player p3 = new Player("3", "3", 200, false, false, null);
        Player p4 = new Player("4", "4", 200, false, false, null);

        PotState potState = new PotState();
        potState.addAllIn(p1);     // inserts cap 80
        potState.addAllIn(p2);     // inserts cap 50 (out of order)
        potState.addCall(p3);      // calls to 80
        potState.addRaise(p4, 160);
        potState.addCall(p3);      // calls to 160

        List<Pot> pots = potState.calculatePots(List.of(p1, p2, p3, p4));

        assertEquals(3, pots.size());
        assertEquals(PotType.MAIN, pots.get(0).type());
        assertEquals(200, pots.get(0).getAmount());
        assertEquals(Set.of("1", "2", "3", "4"), pots.get(0).eligible());

        assertEquals(PotType.SIDE, pots.get(1).type());
        assertEquals(90, pots.get(1).getAmount());
        assertEquals(Set.of("1", "3", "4"), pots.get(1).eligible());

        assertEquals(PotType.SIDE, pots.get(2).type());
        assertEquals(160, pots.get(2).getAmount());
        assertEquals(Set.of("3", "4"), pots.get(2).eligible());
    }
}
