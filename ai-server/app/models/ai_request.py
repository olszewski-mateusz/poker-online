from enum import Enum
from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class Aggressivity(str, Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"


class ActionType(str, Enum):
    ALL_IN = "ALL_IN"
    BET = "BET"
    CALL = "CALL"
    CHECK = "CHECK"
    FOLD = "FOLD"
    JOIN = "JOIN"
    AI_JOIN = "AI_JOIN"
    RAISE = "RAISE"
    READY = "READY"
    REPLACE = "REPLACE"


class GamePhase(str, Enum):
    NOT_STARTED = "NOT_STARTED"
    FIRST_BETTING = "FIRST_BETTING"
    DRAWING = "DRAWING"
    SECOND_BETTING = "SECOND_BETTING"
    SHOWDOWN = "SHOWDOWN"
    FOLDOUT = "FOLDOUT"
    FINISHED = "FINISHED"


class Rank(str, Enum):
    TWO = "TWO"
    THREE = "THREE"
    FOUR = "FOUR"
    FIVE = "FIVE"
    SIX = "SIX"
    SEVEN = "SEVEN"
    EIGHT = "EIGHT"
    NINE = "NINE"
    TEN = "TEN"
    JACK = "JACK"
    QUEEN = "QUEEN"
    KING = "KING"
    ACE = "ACE"


class Suit(str, Enum):
    HEARTS = "HEARTS"
    DIAMONDS = "DIAMONDS"
    CLUBS = "CLUBS"
    SPADES = "SPADES"


class HandType(str, Enum):
    HIGH_CARD = "HIGH_CARD"
    PAIR = "PAIR"
    TWO_PAIR = "TWO_PAIR"
    THREE_OF_A_KIND = "THREE_OF_A_KIND"
    STRAIGHT = "STRAIGHT"
    FLUSH = "FLUSH"
    FULL_HOUSE = "FULL_HOUSE"
    FOUR_OF_A_KIND = "FOUR_OF_A_KIND"
    STRAIGHT_FLUSH = "STRAIGHT_FLUSH"


class Card(BaseModel):
    model_config = ConfigDict(extra="ignore")

    rank: Rank
    suit: Suit


class Player(BaseModel):
    model_config = ConfigDict(extra="ignore")

    index: int
    name: str
    chips: int
    bet: int
    ready: bool
    folded: bool
    cards: list[Card] | None = None
    handType: HandType | None = None


class MainPot(BaseModel):
    model_config = ConfigDict(extra="ignore")

    chips: int
    winnerIndexes: list[int] = Field(default_factory=list)


class SidePot(BaseModel):
    model_config = ConfigDict(extra="ignore")

    chips: int
    eligibleIndexes: list[int] = Field(default_factory=list)
    winnerIndexes: list[int] = Field(default_factory=list)


class Pot(BaseModel):
    model_config = ConfigDict(extra="ignore")

    chips: int
    mainPot: MainPot
    sidePots: list[SidePot] = Field(default_factory=list)


class GameConfiguration(BaseModel):
    model_config = ConfigDict(extra="allow")

    minPlayersToStart: int | None = None
    startChips: int | None = None
    ante: int | None = None
    minOpenBet: int | None = None


class AiConfiguration(BaseModel):
    model_config = ConfigDict(extra="ignore")

    aggressivity: Aggressivity


class ValidAction(BaseModel):
    model_config = ConfigDict(extra="ignore")

    type: ActionType
    properties: dict[str, str] = Field(default_factory=dict)


class GameResponse(BaseModel):
    model_config = ConfigDict(extra="allow")

    gameId: str
    myId: str
    myIndex: int | None = None
    phase: GamePhase | None = None
    currentPlayerIndex: int | None = None
    pot: Pot | None = None
    players: list[Player] = Field(default_factory=list)
    history: list[dict[str, Any]] = Field(default_factory=list)
    cardsInDeck: int | None = None
    discardedCards: int | None = None
    configuration: GameConfiguration | None = None
    validActions: list[ValidAction] = Field(default_factory=list)


class AiRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    aiConfiguration: AiConfiguration
    gameResponse: GameResponse
