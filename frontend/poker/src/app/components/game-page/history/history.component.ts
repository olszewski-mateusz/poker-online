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
import {MatList, MatListItem, MatListItemLine, MatListItemTitle, MatListModule} from '@angular/material/list';
import {
  ActionType,
  Game,
  GamePhase,
  HistoryEntry,
  isActionEntry,
  isPhaseChangeEntry,
  isWinnerEntry
} from '../../../model';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [
    MatListItemTitle,
    MatListItemLine,
    MatListItem,
    MatList,
    MatListModule
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
    return this.buildHistoryEntries(this.game());
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
      return `${entry.details} phase started`
    } else if (isWinnerEntry(entry)) {
      return `${entry.details.playerName} won with ${entry.details.handType}`;
    } else if (isActionEntry(entry)) {
      switch (entry.entryType) {
        case ActionType.JOIN:
          return `${entry.details.playerName} joined`;
        case ActionType.ALL_IN:
          return `${entry.details.playerName} goes all in!`;
        case ActionType.CHECK:
          return entry.details.playerName + (betPlaced ? ' called' : ' checked');
        case ActionType.FOLD:
          return `${entry.details.playerName} folded`;
        case ActionType.RAISE:
          return entry.details.playerName + (betPlaced ? ' raises to ' : ' bets ') + entry.details.value;
        case ActionType.READY:
          return `${entry.details.playerName} is ${entry.details.value === true ? '' : 'not'} ready`;
        case ActionType.REPLACE:
          return `${entry.details.playerName} replaced ${entry.details.value} cards`;
      }
    }
    return 'Unknown history entry';
  }
}
