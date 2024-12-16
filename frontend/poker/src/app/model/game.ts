import {HistoryEntry} from './history';
import {Card, HandType} from './card';

export type Game = {
  players: Player[]
  history: HistoryEntry[]
  configuration: Configuration
  phase: GamePhase
  currentPlayerId?: string
  winnerId?: string
  myId: string
  gameId: string
  cardsInDeck: number
  discardedCards: number
  betPlacedInCurrentPhase: boolean
}

export type Player = {
  id: string
  name: string
  money: number
  bet: number
  ready: boolean
  folded: boolean
  cards?: Card[]
  handType?: HandType
}

export type Configuration = {
  minPlayersToStart: number
  startMoney: number
  ante: number
}

export enum GamePhase {
  NOT_STARTED = 'NOT_STARTED',
  FIRST_BETTING = 'FIRST_BETTING',
  DRAWING = 'DRAWING',
  SECOND_BETTING = 'SECOND_BETTING',
  SHOWDOWN = 'SHOWDOWN',
}

export function translateGamePhase(phase: GamePhase): string {
  switch (phase) {
    case GamePhase.NOT_STARTED: return 'Not started';
    case GamePhase.FIRST_BETTING: return 'First betting';
    case GamePhase.DRAWING: return 'Drawing';
    case GamePhase.SECOND_BETTING: return 'Second betting';
    case GamePhase.SHOWDOWN: return 'Showdown';
  }
}
