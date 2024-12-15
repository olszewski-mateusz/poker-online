import {GamePhase} from './game';
import {HandType} from './card';

export type HistoryEntry = ActionEntry | PhaseChangeEntry | WinnerEntry;

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

export function isPhaseChangeEntry(entry: HistoryEntry): entry is PhaseChangeEntry {
  return entry.entryType === 'PHASE_CHANGE';
}

export function isActionEntry(entry: HistoryEntry): entry is ActionEntry {
  return entry.entryType !== 'PHASE_CHANGE' && entry.entryType !== 'WINNER';
}

export enum ActionType  {
  JOIN = 'JOIN',
  ALL_IN = 'ALL_IN',
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
  }
}
