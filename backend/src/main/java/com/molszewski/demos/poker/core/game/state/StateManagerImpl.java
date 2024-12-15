package com.molszewski.demos.poker.core.game.state;

import com.molszewski.demos.poker.core.game.GameState;
import com.molszewski.demos.poker.core.game.GameConfiguration;
import com.molszewski.demos.poker.core.game.state.action.*;
import com.molszewski.demos.poker.core.game.state.exception.ActionException;
import com.molszewski.demos.poker.core.game.state.transition.NextTurn;
import com.molszewski.demos.poker.core.game.state.transition.StartRound;
import com.molszewski.demos.poker.core.game.state.transition.Transition;
import com.molszewski.demos.poker.core.game.state.validator.CorrectPhase;
import com.molszewski.demos.poker.core.game.state.validator.Validator;
import com.molszewski.demos.poker.core.player.Player;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.util.List;
import java.util.Set;

public class StateManagerImpl implements StateManager {

    @Override
    public List<Validator> getValidators(Action action) {
        return this.getActionContext(action).validators;
    }

    @Override
    public Transition getTransition(Action action) {
        return this.getActionContext(action).transition;
    }

    private ActionContext getActionContext(Action action) {
        switch (action) {
            case Join ignored -> {
                return ActionContext.builder()
                        .validators(List.of(CorrectPhase.of(Set.of(GamePhase.NOT_STARTED))))
                        .build();
            }
            case Ready ignored -> {
                return ActionContext.builder()
                        .validators(List.of(CorrectPhase.of(Set.of(GamePhase.NOT_STARTED, GamePhase.SHOWDOWN))))
                        .transition(StartRound.of())
                        .build();
            }
            case Check ignored -> {
                return ActionContext.builder()
                        .validators(List.of(CorrectPhase.of(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING))))
                        .transition(NextTurn.of())
                        .build();
            }
            case Raise ignored -> {
                return ActionContext.builder()
                        .validators(List.of(CorrectPhase.of(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING))))
                        .transition(NextTurn.of())
                        .build();
            }
            case AllIn ignored1 -> {
                return ActionContext.builder()
                        .validators(List.of(CorrectPhase.of(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING))))
                        .transition(NextTurn.of())
                        .build();
            }
            case Fold ignored -> {
                return ActionContext.builder()
                        .validators(List.of(CorrectPhase.of(Set.of(GamePhase.FIRST_BETTING, GamePhase.SECOND_BETTING))))
                        .transition(NextTurn.of())
                        .build();
            }
            case Replace ignored -> {
                return ActionContext.builder()
                        .validators(List.of(CorrectPhase.of(Set.of(GamePhase.DRAWING))))
                        .transition(NextTurn.of())
                        .build();
            }
        }
    }

    @Builder
    private record ActionContext(List<Validator> validators, @Nullable Transition transition) {
    }
}
