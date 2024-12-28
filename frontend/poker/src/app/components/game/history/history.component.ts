import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  ElementRef,
  input,
  InputSignal,
  signal,
  Signal,
  viewChild,
  WritableSignal
} from '@angular/core';
import {MatList, MatListItem, MatListItemTitle} from '@angular/material/list';
import {
  ActionType,
  Game,
  GamePhase,
  HistoryEntry,
  isActionEntry,
  isPhaseChangeEntry,
  isWinnerEntry,
  translateHandType
} from '../../../model';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'poker-game-history',
  standalone: true,
  imports: [
    MatListItemTitle,
    MatListItem,
    MatList,
    MatIcon
  ],
  templateUrl: './history.component.html',
  styleUrl: './history.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HistoryComponent {
  game: InputSignal<Game> = input.required<Game>()

  private readonly historyContainer = viewChild.required<MatList, ElementRef>(MatList, {read: ElementRef});

  private readonly firstScroll: WritableSignal<boolean> = signal<boolean>(true);

  historyEntries: Signal<string[]> = computed(() => {
    const entries: string[] = this.buildHistoryEntries(this.game());
    if (entries.length > 100) {
      return entries.slice(-100);
    }
    return entries;
  })

  constructor() {
    effect(() => {
      if (this.historyEntries().length > 0) {
        this.scrollToBottom(!this.firstScroll());
      }
      this.firstScroll.set(false);
    }, {allowSignalWrites: true});
  }

  scrollToBottom(smooth: boolean): void {
    const historyContainer = this.historyContainer();
    if (historyContainer) {
      historyContainer.nativeElement.scroll({
        top: historyContainer.nativeElement.scrollHeight,
        left: 0,
        behavior: smooth ? 'smooth' : 'instant'
      });
    }
  }

  private buildHistoryEntries(game: Game): string[] {
    const entries: string[] = [];
    let betPlaced: boolean = false;
    for (const entry of game.history) {
      entries.push(this.translateHistoryEntry(entry, betPlaced));

      if (isPhaseChangeEntry(entry) && (entry.details === GamePhase.FIRST_BETTING || entry.details === GamePhase.SECOND_BETTING)) {
        betPlaced = false;
      }

      if (isActionEntry(entry) && (entry.entryType === ActionType.RAISE || entry.entryType === ActionType.ALL_IN)) {
        betPlaced = true;
      }
    }
    return entries;
  }

  private translateHistoryEntry(entry: HistoryEntry, betPlaced: boolean): string {
    if (isPhaseChangeEntry(entry)) {
      return this.translateGamePhaseToEntry(entry.details);
    } else if (isWinnerEntry(entry)) {
      return `${entry.details.playerName} wins with ${translateHandType(entry.details.handType)}`;
    } else if (isActionEntry(entry)) {
      switch (entry.entryType) {
        case ActionType.JOIN:
          return `${entry.details.playerName} joins`;
        case ActionType.ALL_IN:
          return `${entry.details.playerName} goes all in!`;
        case ActionType.CHECK:
          return entry.details.playerName + (betPlaced ? ' calls' : ' checks');
        case ActionType.FOLD:
          return `${entry.details.playerName} folds`;
        case ActionType.RAISE:
          return entry.details.playerName + (betPlaced ? ' raises to ' : ' bets ') + entry.details.value;
        case ActionType.READY:
          return `${entry.details.playerName} is ${entry.details.value === true ? '' : 'not'} ready`;
        case ActionType.REPLACE:
          return `${entry.details.playerName} replaces ${entry.details.value} cards`;
      }
    }
    return 'Unknown history entry';
  }

  private translateGamePhaseToEntry(phase: GamePhase): string {
    switch (phase) {
      case GamePhase.NOT_STARTED: return 'Game is not started';
      case GamePhase.FIRST_BETTING: return 'Round starts';
      case GamePhase.DRAWING: return 'Drawing phase starts';
      case GamePhase.SECOND_BETTING: return 'Second betting phase starts';
      case GamePhase.SHOWDOWN: return 'Showdown phase starts';
    }
  }
}
