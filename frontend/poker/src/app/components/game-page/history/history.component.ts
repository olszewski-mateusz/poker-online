import {
  ChangeDetectionStrategy,
  Component,
  computed, effect,
  ElementRef,
  input,
  InputSignal,
  Signal,
  viewChild
} from '@angular/core';
import {MatList, MatListItem, MatListItemLine, MatListItemTitle, MatListModule} from '@angular/material/list';
import {ActionType, Game} from '../../../model/game';

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

  private historyContainer = viewChild.required<MatList, ElementRef>(MatList, {read: ElementRef});

  constructor() {
    effect(() => {
      const historyEntries = this.historyEntries();
      this.scrollToBottom();
    });
  }

  scrollToBottom(): void {
    const historyContainer = this.historyContainer();
    console.log(historyContainer)
    if (historyContainer) {
      historyContainer.nativeElement.scroll({
        top: historyContainer.nativeElement.scrollHeight,
        left: 0,
        behavior: 'smooth'
      });
    }
  }

  historyEntries: Signal<string[]> = computed(() => {
    return this.game().history.map(command => {
      switch (command.actionType) {
        case ActionType.JOIN:
          return `Player ${command.playerName} joined.`;
        case ActionType.ALL_IN:
          break;
        case ActionType.CHECK:
          break;
        case ActionType.FOLD:
          break;
        case ActionType.RAISE:
          break;
        case ActionType.READY:
          return `Player ${command.playerName} is ${command.amount === 1 ? '' : 'not'} ready.`;
        case ActionType.REPLACE:
          break;
      }
      return 'Unknown';
    });
  })
}
