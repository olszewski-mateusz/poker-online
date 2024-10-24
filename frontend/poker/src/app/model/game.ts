import {Rank} from './rank';
import {Suit} from './suit';

export type Game = {
  players: Player[]
  history: Command[]
  configuration: Configuration
  state: GameState
  currentPlayerId?: string
  myId: string
  gameId: string
  cardsInDeck: number
  discardedCards: number
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

export type Card = {
  rank: Rank
  suit: Suit
}

export enum HandType {
  HIGH_CARD = 'HIGH_CARD',
  PAIR = 'PAIR',
  TWO_PAIR = 'TWO_PAIR',
  THREE_OF_A_KIND = 'THREE_OF_A_KIND',
  STRAIGHT = 'STRAIGHT',
  FLUSH = 'FLUSH',
  FULL_HOUSE = 'FULL_HOUSE',
  FOUR_OF_A_KIND = 'FOUR_OF_A_KIND',
  STRAIGHT_FLUSH = 'STRAIGHT_FLUSH'
}

export type Command = {
  playerName: string
  actionType: ActionType
  amount?: number
}

export type Configuration = {
  minPlayersToStart: number
  startMoney: number
  ante: number
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

export enum GameState {
  NOT_STARTED = 'NOT_STARTED',
  FIRST_BETTING = 'FIRST_BETTING',
  DRAWING = 'DRAWING',
  SECOND_BETTING = 'SECOND_BETTING',
  SHOWDOWN = 'SHOWDOWN',
}
