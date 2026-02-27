import {GamePhase} from './game';
import {Card, HandType} from './card';

export type HistoryEntry =
  ActionEntry
  | PhaseChangeEntry
  | WinnerEntry
  | GameWinnerEntry
  | PotWinnerEntry
  | ShowdownSummaryEntry;

export type ActionEntry = {
  entryType: ActionType
  details: {
    playerName: string
    value?: string | number | boolean
  }
}

export function isWinnerEntry(entry: HistoryEntry): entry is WinnerEntry {
  return entry.entryType === 'WINNER';
}

export function isGameWinnerEntry(entry: HistoryEntry): entry is GameWinnerEntry {
  return entry.entryType === 'GAME_WINNER';
}

export function isPotWinnerEntry(entry: HistoryEntry): entry is PotWinnerEntry {
  return entry.entryType === 'POT_WINNER';
}

export function isShowdownSummaryEntry(entry: HistoryEntry): entry is ShowdownSummaryEntry {
  return entry.entryType === 'SHOWDOWN_SUMMARY';
}

export function isPhaseChangeEntry(entry: HistoryEntry): entry is PhaseChangeEntry {
  return entry.entryType === 'PHASE_CHANGE';
}

export function isActionEntry(entry: HistoryEntry): entry is ActionEntry {
  return Object.values(ActionType).includes(entry.entryType as ActionType);
}

export enum ActionType  {
  JOIN = 'JOIN',
  ALL_IN = 'ALL_IN',
  BET = 'BET',
  CALL = 'CALL',
  CHECK = 'CHECK',
  FOLD = 'FOLD',
  RAISE = 'RAISE',
  READY = 'READY',
  REPLACE = 'REPLACE'
}

export type PhaseChangeEntry = {
  entryType: 'PHASE_CHANGE'
  details: GamePhase
}

export type WinnerEntry = {
  entryType: 'WINNER'
  details: {
    playerName: string
    handType: HandType
    keyCards: Card[]
  }
}

export type GameWinnerEntry = {
  entryType: 'GAME_WINNER'
  details: {
    playerName: string
  }
}

export type PotWinnerEntry = {
  entryType: 'POT_WINNER'
  details: {
    potIndex: number
    potAmount: number
    winnerNames: string[]
  }
}

export type ShowdownSummaryEntry = {
  entryType: 'SHOWDOWN_SUMMARY'
  details: {
    pots: {
      potIndex: number
      potAmount: number
      winnerNames: string[]
    }[]
    winners: {
      playerName: string
      handType: HandType
      keyCards: Card[]
    }[]
  }
}
