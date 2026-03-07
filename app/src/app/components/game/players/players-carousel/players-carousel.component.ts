import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  input,
  InputSignal,
  signal,
  WritableSignal
} from '@angular/core';
import {Game, Player} from '../../../../model';
import {PlayersCarouselPropertiesComponent} from './players-carousel-properties/players-carousel-properties.component';

@Component({
  selector: 'poker-game-players-carousel',
  standalone: true,
  templateUrl: './players-carousel.component.html',
  styleUrl: './players-carousel.component.scss',
  imports: [
    PlayersCarouselPropertiesComponent
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlayersCarouselComponent {
  protected itemSideSize: number = 150;
  private readonly snapAnimationMs: number = 1000;

  private readonly scrollCenterIndex: WritableSignal<number> = signal(0);
  private readonly highlightedPlayer: WritableSignal<number | undefined> = signal(undefined);
  private readonly dragOffsetPx: WritableSignal<number> = signal(0);
  private readonly pointerState: WritableSignal<{ id: number, value: number } | undefined> = signal(undefined);
  private centerAnimationTimeoutId: ReturnType<typeof setTimeout> | undefined;
  private snapResetTimeoutId: ReturnType<typeof setTimeout> | undefined;

  game: InputSignal<Game> = input.required<Game>();
  vertical: InputSignal<boolean> = input.required<boolean>();
  highlightedOnCenter: InputSignal<boolean> = input.required<boolean>();

  players = computed(() => this.game().players)
  activePlayer = computed(() => this.game().players.find(p => p.index === this.game().currentPlayerIndex))

  protected readonly renderedSlots: readonly number[] = [-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5];
  protected readonly isDragging = signal(false);
  protected readonly animateSnap = signal(false);

  constructor() {

    effect(() => {
      const vertical = this.vertical();
      if (vertical) {
        this.itemSideSize = 80;
      } else {
        this.itemSideSize = 150;
      }
    });


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
        if (this.isDragging()) {
          return;
        }
        if (this.highlightedPlayer() === highlightedPlayerInput.index) {
          return;
        }
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

    const playerIndex: number = players.findIndex(player => player.index === playerId);
    if (playerIndex < 0) {
      return;
    }

    const currentIndex: number = this.scrollCenterIndex();
    const normalizedIndex: number = this.normalizeIndex(playerIndex, players.length);
    const moveDelta: number = this.shortestCircularDelta(currentIndex, normalizedIndex, players.length);
    const selectedPlayer: Player = players[normalizedIndex];

    this.clearTimeout(this.centerAnimationTimeoutId);
    this.centerAnimationTimeoutId = undefined;
    this.clearTimeout(this.snapResetTimeoutId);
    this.snapResetTimeoutId = undefined;
    this.animateSnap.set(false);

    // Keep visual continuity first, then animate back to centered (0px offset).
    this.dragOffsetPx.set(this.isDragging() ? 0 : moveDelta * this.itemSideSize);
    this.scrollCenterIndex.set(normalizedIndex);
    this.highlightedPlayer.set(selectedPlayer.index);

    if (!this.isDragging() && moveDelta !== 0) {
      this.centerAnimationTimeoutId = setTimeout(() => {
        this.animateSnap.set(true);
        this.dragOffsetPx.set(0);
        this.centerAnimationTimeoutId = undefined;
        this.snapResetTimeoutId = setTimeout(() => {
          this.animateSnap.set(false);
          this.snapResetTimeoutId = undefined;
        }, this.snapAnimationMs);
      }, 0);
      return;
    }

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
    const translate: number = offsetFromCenter * this.itemSideSize + this.dragOffsetPx();

    if (this.vertical()) {
      return `translateY(${translate}px)`;
    } else {
      return `translateX(${translate}px)`;
    }
  }

  protected onPointerDown(event: PointerEvent): void {
    if (this.players().length < 2) {
      return;
    }

    const target: HTMLElement = event.currentTarget as HTMLElement;
    target.setPointerCapture(event.pointerId);
    this.pointerState.set({id: event.pointerId, value: this.vertical() ? event.clientY : event.clientX});
    this.isDragging.set(true);
    this.clearTimeout(this.centerAnimationTimeoutId);
    this.centerAnimationTimeoutId = undefined;
    this.clearTimeout(this.snapResetTimeoutId);
    this.snapResetTimeoutId = undefined;
    this.animateSnap.set(false);
  }

  protected onPointerMove(event: PointerEvent): void {
    const pointerState: { id: number, value: number } | undefined = this.pointerState();
    if (pointerState?.id !== event.pointerId) {
      return;
    }

    const clientValue = this.vertical() ? event.clientY : event.clientX;
    const deltaY: number = clientValue - pointerState.value;
    this.pointerState.set({id: pointerState.id, value: clientValue});
    this.applyDelta(deltaY);
    event.preventDefault();
  }

  protected onPointerCancel(event: PointerEvent): void {
    const pointerState: { id: number, value: number } | undefined = this.pointerState();
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

  private applyDelta(delta: number): void {
    const centerSwitchThresholdPx: number = this.itemSideSize / 2;
    let nextOffset: number = this.dragOffsetPx() + delta;
    while (nextOffset >= centerSwitchThresholdPx) {
      nextOffset -= this.itemSideSize;
      this.shiftCenteredBy(-1);
    }
    while (nextOffset <= -centerSwitchThresholdPx) {
      nextOffset += this.itemSideSize;
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
    const steps: number = Math.round(this.dragOffsetPx() / this.itemSideSize);
    if (steps !== 0) {
      this.shiftCenteredBy(-steps);
    }
    this.clearTimeout(this.snapResetTimeoutId);
    this.animateSnap.set(true);
    this.dragOffsetPx.set(0);
    this.snapResetTimeoutId = setTimeout(() => {
      this.animateSnap.set(false);
      this.snapResetTimeoutId = undefined;
    }, this.snapAnimationMs);
  }

  private normalizeIndex(index: number, length: number): number {
    return ((index % length) + length) % length;
  }

  private shortestCircularDelta(fromIndex: number, toIndex: number, length: number): number {
    const forward: number = ((toIndex - fromIndex) % length + length) % length;
    const backward: number = forward - length;
    return Math.abs(forward) <= Math.abs(backward) ? forward : backward;
  }

  private clearTimeout(timeoutId: ReturnType<typeof setTimeout> | undefined): void {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
  }
}
