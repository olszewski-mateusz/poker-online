export type HistoryEntry = {
  category: HistoryEntryCategory;
  properties: Record<string, string>;
}

export enum HistoryEntryCategory {
  ACTION = 'ACTION',
  ACTION_STEP = 'ACTION_STEP',
  GAME_WINNER = 'GAME_WINNER',
  PHASE_CHANGE = 'PHASE_CHANGE',
  POT_WINNER = 'POT_WINNER'
}

export enum ActionStepType {
  NEXT_TURN = 'NEXT_TURN',
  START_ROUND = 'START_ROUND'
}

export enum ActionEntryType {
  JOIN = 'join',
  ALL_IN = 'allIn',
  BET = 'bet',
  CALL = 'call',
  CHECK = 'check',
  FOLD = 'fold',
  RAISE = 'raise',
  READY = 'ready',
  REPLACE = 'replace'
}
