import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  input,
  InputSignal,
  signal,
  Signal,
  viewChild,
  WritableSignal
} from '@angular/core';
import {MatList, MatListItem, MatListItemTitle} from '@angular/material/list';
import {Game} from '../../../model';
import {MatIcon} from '@angular/material/icon';
import {HistoryService} from './history.service';

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
  mobile: InputSignal<boolean> = input.required<boolean>();

  private readonly historyContainer = viewChild.required<MatList, ElementRef>(MatList, {read: ElementRef});

  private readonly firstScroll: WritableSignal<boolean> = signal<boolean>(true);

  private readonly maxEntries: Signal<number> = computed(() => {
    return this.mobile() ? 20 : 100;
  })

  private readonly historyService = inject(HistoryService);

  historyEntries: Signal<string[]> = computed(() => {
    const entries: string[] = this.historyService.buildHistoryEntries(this.game());
    if (entries.length > this.maxEntries()) {
      return entries.slice(-this.maxEntries());
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
}
