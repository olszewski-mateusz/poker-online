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
