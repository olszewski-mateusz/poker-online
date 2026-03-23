package com.molszewski.demos.poker.engine.player;

import com.molszewski.demos.poker.engine.exception.ActionValidationException;
import com.molszewski.demos.poker.engine.game.GamePhase;
import com.molszewski.demos.poker.engine.game.GameState;
import com.molszewski.demos.poker.engine.hand.Hand;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class PlayerState {
    private final List<Player> players;
    private Player currentPlayer;

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public PlayerState(List<Player> players) {
        this.players = new ArrayList<>(players);
    }

    public Player getById(String id) {
        return players.stream().filter(player -> player.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Player with id " + id + " not found"));
    }

    public List<Player> getByIds(Collection<String> ids) {
        return players.stream().filter(player -> ids.contains(player.getId())).toList();
    }

    public boolean exists(String id) {
        return players.stream().anyMatch(player -> player.getId().equals(id));
    }

    public int getIndex(String playerId) {
        if (!exists(playerId)) {
            throw new IllegalStateException("Player not found");
        }

        Player player = getById(playerId);
        return players.indexOf(player);
    }

    @Nullable
    public Integer getCurrentPlayerIndex() {
        return currentPlayer != null ? getIndex(currentPlayer.getId()) : null;
    }

    public void canPlayerMakeAction(String playerId) throws ActionValidationException {
        if (currentPlayer == null) {
            throw new ActionValidationException("No current player selected");
        }
        if (currentPlayer.getId().equals(playerId)) {
            if (currentPlayer.isReady()) {
                throw new ActionValidationException(String.format("Player %s is ready", currentPlayer.getId()));
            }
            if (currentPlayer.isFolded()) {
                throw new ActionValidationException(String.format("Player %s folded", currentPlayer.getId()));
            }
        } else {
            throw new ActionValidationException("It's not player " + playerId + "turn");
        }
    }

    public void add(String id, String displayName, int chips) {
        players.add(new Player(id, displayName, chips, false, true, null));
    }

    public int size() {
        return players.size();
    }

    public int readySize() {
        return Math.toIntExact(players.stream().filter(Player::isReady).count());
    }

    public void removeReadyForAllWithChips() {
        players.forEach(p -> {
            if (p.getChips() > 0) {
                p.setReady(false);
            }
        });
    }

    public void removeReadyForAll() {
        players.forEach(p -> {
            p.setReady(false);
        });
    }

    public void removeFoldedForAll() {
        players.forEach(p -> {
            p.setFolded(false);
        });
    }

    public void setNextCurrentPlayer(GamePhase currentPhase) {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players in game");
        }

        if (currentPlayer == null) {
            throw new IllegalStateException("Current player is null");
        }

        List<Player> playersInGame;
        if (currentPhase.equals(GamePhase.FIRST_BETTING) || currentPhase.equals(GamePhase.SECOND_BETTING)) {
            playersInGame = players.stream()
                    .filter(player -> (!player.isFolded() && player.getChips() > 0) || player == currentPlayer)
                    .toList();
        } else {
            playersInGame = players.stream()
                    .filter(player -> !player.isFolded() || player == currentPlayer)
                    .toList();
        }

        currentPlayer = playersInGame.get((playersInGame.indexOf(currentPlayer) + 1) % playersInGame.size());
    }

    public void clearCurrentPlayer() {
        currentPlayer = null;
    }

    public boolean allMatched(Predicate<Player> predicate) {
        return players.stream().allMatch(predicate);
    }

    public List<Player> filtered(Predicate<Player> predicate) {
        return players.stream().filter(predicate).toList();
    }

    public List<Player> getAll() {
        return new ArrayList<>(players);
    }

    public List<Player> getAllNotFolded() {
        return players.stream().filter(player -> !player.isFolded()).toList();
    }

    public List<Player> getWinners() {
        return players.stream()
                .map(Player::getHand)
                .filter(Objects::nonNull)
                .max(Hand::compareTo)
                .map(bestHand -> players.stream()
                        .filter(player -> player.getHand().compareTo(bestHand) == 0).toList()
                ).orElse(List.of());
    }

    public List<Player> getPotWinners(Collection<String> eligible) {
        List<Player> potPlayers = getByIds(eligible);
        return potPlayers.stream()
                .map(Player::getHand)
                .filter(Objects::nonNull)
                .max(Hand::compareTo)
                .map(bestHand -> potPlayers.stream()
                        .filter(player -> player.getHand().compareTo(bestHand) == 0).toList()
                ).orElse(List.of());
    }

    public void setFirstPlayerForBetting() {
        if (currentPlayer != null) {
            throw new IllegalStateException("Current player is already set");
        }

        Player player = players.stream()
                .filter(p -> !p.isFolded() && p.getChips() > 0)
                .findFirst()
                .orElse(null);

        if (player == null) {
            throw new IllegalStateException("Cant's set current player");
        }

        currentPlayer = player;
    }

    public void setFinishedPhaseCurrentPlayer() {
        currentPlayer = getAllNotFolded().getFirst();
    }

    public void disablePlayersWithoutChips() {
        players.stream()
                .filter(player -> player.getChips() == 0)
                .forEach(player -> player.setFolded(true));
    }

    public void reorderPlayers() {
        players.addLast(players.removeFirst());
    }

    public void markPlayersWithoutChipsReady() {
        players.stream()
                .filter(player -> !player.isFolded() && player.getChips() == 0)
                .forEach(player -> player.setReady(true));
    }

    public Player removeById(String playerId, GamePhase phase) {
        int removedIndex = getIndex(playerId);
        Player removed = getById(playerId);
        boolean wasCurrent = currentPlayer != null && currentPlayer.getId().equals(playerId);

        players.remove(removed);

        if (!wasCurrent) {
            return removed;
        }

        this.currentPlayer = findNextEligibleAfterRemoved(removedIndex, phase);
        return removed;
    }

    public void resetForLobby(int startChips) {
        players.forEach(player -> player.resetToLobby(startChips));
        clearCurrentPlayer();
    }

    @Nullable
    private Player findNextEligibleAfterRemoved(int removedIndex, GamePhase phase) {
        if (players.isEmpty()) {
            return null;
        }

        int startIndex = removedIndex % players.size();
        for (int i = 0; i < players.size(); i++) {
            int currentIndex = (startIndex + i) % players.size();
            Player candidate = players.get(currentIndex);
            if (isEligibleToActInPhase(candidate, phase)) {
                return candidate;
            }
        }

        return null;
    }

    private boolean isEligibleToActInPhase(Player player, GamePhase phase) {
        return switch (phase) {
            case FIRST_BETTING, SECOND_BETTING -> !player.isFolded() && player.getChips() > 0 && !player.isReady();
            case DRAWING -> !player.isFolded() && !player.isReady();
            default -> true;
        };
    }
}
