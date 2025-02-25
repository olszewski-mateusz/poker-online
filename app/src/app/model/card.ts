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

export function translateHandType(type: HandType): string {
  switch (type) {
    case HandType.HIGH_CARD: return 'high card';
    case HandType.PAIR: return 'pair';
    case HandType.TWO_PAIR: return 'two pairs';
    case HandType.THREE_OF_A_KIND: return 'three of a kind';
    case HandType.STRAIGHT: return 'straight';
    case HandType.FLUSH: return 'flush';
    case HandType.FULL_HOUSE: return 'full house';
    case HandType.FOUR_OF_A_KIND: return 'four of a kind';
    case HandType.STRAIGHT_FLUSH: return 'straight flush';
  }
}

export enum Rank {
  TWO = 'TWO',
  THREE = 'THREE',
  FOUR = 'FOUR',
  FIVE = 'FIVE',
  SIX = 'SIX',
  SEVEN = 'SEVEN',
  EIGHT = 'EIGHT',
  NINE = 'NINE',
  TEN = 'TEN',
  JACK = 'JACK',
  QUEEN = 'QUEEN',
  KING = 'KING',
  ACE = 'ACE'
}

export enum Suit {
  HEARTS = 'HEARTS',
  SPADES = 'SPADES',
  DIAMONDS = 'DIAMONDS',
  CLUBS = 'CLUBS'
}

export function compareCards(a: Card, b: Card): number {
  const rankCompare: number = rankToValue(a.rank) - rankToValue(b.rank);
  if (rankCompare != 0) {
    return rankCompare;
  }
  return  suitToValue(a.suit) - suitToValue(b.suit);
}

function rankToValue(rank: Rank): number {
  switch (rank){
    case Rank.TWO: return 2;
    case Rank.THREE: return 3;
    case Rank.FOUR: return 4;
    case Rank.FIVE: return 5;
    case Rank.SIX: return 6;
    case Rank.SEVEN: return 7;
    case Rank.EIGHT: return 8;
    case Rank.NINE: return 9;
    case Rank.TEN: return 10;
    case Rank.JACK: return 11;
    case Rank.QUEEN: return 12;
    case Rank.KING: return 13;
    case Rank.ACE: return 14;
  }
}

function suitToValue(suit: Suit): number {
  switch (suit){
    case Suit.SPADES: return 1;
    case Suit.HEARTS: return 2;
    case Suit.CLUBS: return 3;
    case Suit.DIAMONDS: return 4;
  }
}
