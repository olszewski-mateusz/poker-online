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
