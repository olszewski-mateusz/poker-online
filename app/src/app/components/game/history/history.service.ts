import { Injectable } from '@angular/core';
import {
  ActionStepType,
  ActionType,
  Game,
  GamePhase, HandType,
  HistoryEntry,
  HistoryEntryCategory, translateHandType
} from '../../../model';

@Injectable({
  providedIn: 'root'
})
export class HistoryService {

  public buildHistoryEntries(game: Game): string[] {
    const entries: string[] = [];
    for (const entry of game.history) {
      entries.push(this.translateHistoryEntry(entry));
    }
    return entries;
  }

  private translateHistoryEntry(entry: HistoryEntry): string {
    switch (entry.category) {
      case HistoryEntryCategory.ACTION:
        return this.translateActionEntry(entry.properties);
      case HistoryEntryCategory.ACTION_STEP:
        return this.translateActionStepEntry(entry.properties);
      case HistoryEntryCategory.GAME_WINNER:
        return `${entry.properties["winnerName"]} is game winner!`
      case HistoryEntryCategory.PHASE_CHANGE:
        return this.translateGamePhaseToEntry(entry.properties);
      case HistoryEntryCategory.POT_WINNER:
        return this.translatePotWinnerEntry(entry.properties);
      default:
        return 'Unknown history entry';
    }
  }

  private translateActionEntry(properties: Record<string, string>): string {
    const playerName = properties["playerName"]
    switch (properties["actionType"]) {
      case ActionType.JOIN:
        return `${playerName} joins`;
      case ActionType.AI_JOIN:
        return `${playerName} joins as AI`;
      case ActionType.ALL_IN:
        return `${playerName} goes all in!`;
      case ActionType.BET: {
        const betAmount = properties["betAmount"]
        return `${playerName} bets ${betAmount}`;
      }
      case ActionType.CALL:
        return `${playerName} calls`;
      case ActionType.CHECK:
        return `${playerName} checks`;
      case ActionType.FOLD:
        return `${playerName} folds`;
      case ActionType.RAISE: {
        const raiseAmount = properties["raiseAmount"]
        return `${playerName} raises to ${raiseAmount}`;
      }
      case ActionType.READY: {
        const isReady = Boolean(JSON.parse(properties["ready"]));
        return `${playerName} is ${isReady ? '' : 'not'} ready`;
      }
      case ActionType.REPLACE: {
        const replaceAmount = Number(properties["replaceAmount"]);
        const isOne: boolean = replaceAmount === 1;
        return `${playerName} replaces ${replaceAmount} ${isOne ? 'card' : 'cards'}`;
      }
    }
    return 'Unknown action entry';
  }

  private translateActionStepEntry(properties: Record<string, string>) {
    switch (properties["stepType"]) {
      case ActionStepType.NEXT_TURN:
        return 'Next turn'
      case ActionStepType.START_ROUND:
        return 'Next round begins'
      default:
        return 'Unknown action step entry';
    }
  }

  private translateGamePhaseToEntry(properties: Record<string, string>): string {
    const phase = properties["newPhase"];
    switch (phase) {
      case GamePhase.NOT_STARTED:
        return 'Game is not started';
      case GamePhase.FIRST_BETTING:
        return 'Round starts';
      case GamePhase.DRAWING:
        return 'Drawing phase starts';
      case GamePhase.SECOND_BETTING:
        return 'Second betting phase starts';
      case GamePhase.SHOWDOWN:
        return 'Showdown phase starts';
      case GamePhase.FOLDOUT:
        return 'Foldout phase starts';
      case GamePhase.FINISHED:
        return 'There is only one player left with chips';
      default:
        return 'Unknown phase entry';
    }
  }

  private translatePotWinnerEntry(properties: Record<string, string>) {
    const type = properties["potType"];
    const chips = properties["potChips"];
    const winners = properties["winners"];
    const handType: HandType = properties["handType"] as HandType;
    const showdown = properties["showdown"];

    return `${winners} wins ${chips} chips from ${type.toLowerCase()} pot with ${translateHandType(handType)} hand.`;
  }
}
