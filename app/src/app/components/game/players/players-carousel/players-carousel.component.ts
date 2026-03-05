import {
  ChangeDetectionStrategy,
  Component, computed,
  effect,
  input,
  InputSignal,
  output,
  OutputEmitterRef,
  signal,
  WritableSignal
} from '@angular/core';
import {Game, GamePhase, Player} from '../../../../model';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'poker-game-players-carousel',
  standalone: true,
  templateUrl: './players-carousel.component.html',
  styleUrl: './players-carousel.component.scss',
  imports: [
    MatIcon
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlayersCarouselComponent {
  protected readonly itemHeightPx: number = 80;

  private readonly scrollCenterIndex: WritableSignal<number> = signal(0);
  private readonly highlightedPlayer: WritableSignal<number | undefined> = signal(undefined);
  private readonly dragOffsetPx: WritableSignal<number> = signal(0);
  private readonly pointerState: WritableSignal<{ id: number, y: number } | undefined> = signal(undefined);

  game: InputSignal<Game> = input.required<Game>();

  players = computed(() => this.game().players)
  activePlayer = computed(() => this.game().players.find(p => p.index === this.game().currentPlayerIndex))

  protected readonly renderedSlots: readonly number[] = [-3, -2, -1, 0, 1, 2, 3];
  protected readonly isDragging = signal(false);

  constructor() {
    effect(() => {
      const players: Player[] = this.players();
      if (players.length === 0) {
        this.scrollCenterIndex.set(0);
        this.highlightedPlayer.set(undefined);
        this.dragOffsetPx.set(0);
        return;
      }

      this.scrollCenterIndex.update(currentIndex => this.normalizeIndex(currentIndex, players.length));

      const selectedPlayer: number | undefined = this.highlightedPlayer();
      if (selectedPlayer && !players.some(p => p.index === selectedPlayer)) {
        this.highlightedPlayer.set(undefined);
      }
    }, {allowSignalWrites: true});

    effect(() => {
      const highlightedPlayerInput: Player | undefined = this.activePlayer();
      if (highlightedPlayerInput) {
        this.centerOnPlayer(highlightedPlayerInput.index);
      } else {
        this.highlightedPlayer.set(undefined);
      }
    }, {allowSignalWrites: true});
  }

  centerOnPlayer(playerId: number) {
    const players: Player[] = this.players();
    if (players.length === 0) {
      return;
    }

    const normalizedIndex: number = this.normalizeIndex(playerId, players.length);
    const selectedPlayer: Player = players[normalizedIndex];

    this.scrollCenterIndex.set(normalizedIndex);
    this.highlightedPlayer.set(selectedPlayer.index);
    this.dragOffsetPx.set(0);
  }

  protected getPlayerForOffset(offsetFromCenter: number): Player | undefined {
    const players: Player[] = this.players();
    if (players.length === 0) {
      return undefined;
    }

    const index: number = this.normalizeIndex(this.scrollCenterIndex() + offsetFromCenter, players.length);
    return players[index];
  }

  protected highlighted(offsetFromCenter: number): boolean {
    const highlightedPlayer: number | undefined = this.highlightedPlayer();
    return highlightedPlayer !== undefined && this.getPlayerForOffset(offsetFromCenter)?.index === highlightedPlayer;
  }

  protected slotTransform(offsetFromCenter: number): string {
    const translateY: number = offsetFromCenter * this.itemHeightPx + this.dragOffsetPx();
    return `translateY(${translateY}px)`;
  }

  protected onPointerDown(event: PointerEvent): void {
    if (this.players().length < 2) {
      return;
    }

    const target: HTMLElement = event.currentTarget as HTMLElement;
    target.setPointerCapture(event.pointerId);
    this.pointerState.set({id: event.pointerId, y: event.clientY});
    this.isDragging.set(true);
  }

  protected onPointerMove(event: PointerEvent): void {
    const pointerState: { id: number, y: number } | undefined = this.pointerState();
    if (pointerState?.id !== event.pointerId) {
      return;
    }

    const deltaY: number = event.clientY - pointerState.y;
    this.pointerState.set({id: pointerState.id, y: event.clientY});
    this.applyDelta(deltaY);
    event.preventDefault();
  }

  protected onPointerCancel(event: PointerEvent): void {
    const pointerState: { id: number, y: number } | undefined = this.pointerState();
    if (pointerState?.id !== event.pointerId) {
      return;
    }
    const target: HTMLElement = event.currentTarget as HTMLElement;
    if (target.hasPointerCapture(event.pointerId)) {
      target.releasePointerCapture(event.pointerId);
    }
    this.pointerState.set(undefined);
    this.isDragging.set(false);
    this.snapToNearestItem();
  }

  private applyDelta(deltaY: number): void {
    const centerSwitchThresholdPx: number = this.itemHeightPx / 2;
    let nextOffset: number = this.dragOffsetPx() + deltaY;
    while (nextOffset >= centerSwitchThresholdPx) {
      nextOffset -= this.itemHeightPx;
      this.shiftCenteredBy(-1);
    }
    while (nextOffset <= -centerSwitchThresholdPx) {
      nextOffset += this.itemHeightPx;
      this.shiftCenteredBy(1);
    }
    this.dragOffsetPx.set(nextOffset);
  }

  private shiftCenteredBy(offset: number): void {
    const playersCount: number = this.players().length;
    if (playersCount === 0) {
      return;
    }
    this.scrollCenterIndex.update(index => this.normalizeIndex(index + offset, playersCount));
  }

  private snapToNearestItem(): void {
    const steps: number = Math.round(this.dragOffsetPx() / this.itemHeightPx);
    if (steps !== 0) {
      this.shiftCenteredBy(-steps);
    }
    this.dragOffsetPx.set(0);
  }

  private normalizeIndex(index: number, length: number): number {
    return ((index % length) + length) % length;
  }

  private shortestCircularDelta(fromIndex: number, toIndex: number, length: number): number {
    const forward: number = ((toIndex - fromIndex) % length + length) % length;
    const backward: number = forward - length;
    return forward;
    return Math.abs(forward) <= Math.abs(backward) ? forward : backward;
  }

  showAllInIcon(player: Player): boolean {
    return !player.folded && player.chips === 0 &&
      (this.game().phase === GamePhase.FIRST_BETTING || this.game().phase === GamePhase.SECOND_BETTING);
  }

  showWinnerIcon(player: Player): boolean {
    return this.game().phase === GamePhase.SHOWDOWN && this.game().winnerIndexes.includes(player.index);
  }
}
