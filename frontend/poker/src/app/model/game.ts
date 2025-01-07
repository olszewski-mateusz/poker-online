import {HistoryEntry} from './history';
import {Card, HandType} from './card';
import {computed, Signal} from '@angular/core';

export type Game = {
  players: Player[]
  history: HistoryEntry[]
  configuration: Configuration
  phase: GamePhase
  currentPlayerIndex?: number
  winnerIndex?: number
  myId: string
  myIndex: number
  gameId: string
  cardsInDeck: number
  discardedCards: number
  betPlacedInCurrentPhase: boolean
}

export function buildMyPlayerSignal(gameSignal: Signal<Game>): Signal<Player | undefined> {
  return computed(() => {
    const game: Game = gameSignal();
    return game.players.find(value => value.index === game.myIndex);
  })
}

export function buildMyTurnSignal(gameSignal: Signal<Game>): Signal<boolean> {
  return computed(() => {
    const game: Game = gameSignal();
    return game.currentPlayerIndex === game.myIndex;
  })
}

export type Player = {
  index: number
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
