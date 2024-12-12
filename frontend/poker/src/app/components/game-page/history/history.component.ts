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
import {ActionType, Command, Game} from '../../../model/game';

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
    let players: number = 0;
    let readyPlayers: number = 0;
    let betPlaced: boolean = false;
    let gameStarted: boolean = false;
    for (const command of game.history) {
      entries.push(this.translateCommandToHistoryEntry(command, betPlaced));

      if (command.actionType === ActionType.JOIN) {
        players++;
      }

      if (command.actionType === ActionType.READY) {
        if (command.amount === 1) {
          readyPlayers++;
        } else if (command.amount === 0) {
          readyPlayers--;
        }
      }

      if (readyPlayers === players && players >= this.game().configuration.minPlayersToStart) {
        entries.push(gameStarted ? 'Next round' : 'Game started');
        gameStarted = true;
        readyPlayers = 0;
        betPlaced = false;
      }

      if (!betPlaced && (command.actionType === ActionType.RAISE || command.actionType === ActionType.ALL_IN)) {
        betPlaced = true;
      }
    }
    return entries;
  }

  private translateCommandToHistoryEntry(command: Command, betPlaced: boolean): string {
    const type: string | ActionType = command.actionType;
    switch (type) {
      case ActionType.JOIN:
        return `${command.playerName} joined`;
      case ActionType.ALL_IN:
        return `${command.playerName} goes all in!`;
      case ActionType.CHECK:
        return command.playerName + (betPlaced ? ' called' : ' checked');
      case ActionType.FOLD:
        return `${command.playerName} folded`;
      case ActionType.RAISE:
        return command.playerName + (betPlaced ? ' raises to ' : ' bets ') + command.amount;
      case ActionType.READY:
        return `${command.playerName} is ${command.amount === 1 ? '' : 'not'} ready`;
      case ActionType.REPLACE:
        return `${command.playerName} replaced ${command.amount} cards`;
    }
    return 'Unknown history entry';
  }
}
