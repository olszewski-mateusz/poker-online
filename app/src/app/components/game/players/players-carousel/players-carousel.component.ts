import {
  ChangeDetectionStrategy,
  Component,
  effect,
  input,
  InputSignal,
  OnDestroy,
  output,
  OutputEmitterRef,
  signal,
  WritableSignal
} from '@angular/core';

@Component({
  selector: 'poker-game-players-carousel',
  standalone: true,
  templateUrl: './players-carousel.component.html',
  styleUrl: './players-carousel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlayersCarouselComponent {
  protected readonly itemHeightPx: number = 80;

  private readonly scrollCenterIndex: WritableSignal<number> = signal(0);
  private readonly selectedPlayerName: WritableSignal<string | undefined> = signal(undefined);
  private readonly dragOffsetPx: WritableSignal<number> = signal(0);
  private readonly pointerState: WritableSignal<{ id: number, y: number } | undefined> = signal(undefined);

  players: InputSignal<readonly string[]> = input.required<readonly string[]>();
  centeredPlayer: InputSignal<string | undefined> = input<string>();

  protected readonly renderedSlots: readonly number[] = [-3, -2, -1, 0, 1, 2, 3];
  protected readonly isDragging = signal(false);
  protected readonly isSnapping = signal(true);

  constructor() {
    effect(() => {
      const players: readonly string[] = this.players();
      if (players.length === 0) {
        this.scrollCenterIndex.set(0);
        this.selectedPlayerName.set(undefined);
        this.dragOffsetPx.set(0);
        return;
      }

      this.scrollCenterIndex.update(currentIndex => this.normalizeIndex(currentIndex, players.length));

      const selectedPlayerName: string | undefined = this.selectedPlayerName();
      if (selectedPlayerName && !players.includes(selectedPlayerName)) {
        this.selectedPlayerName.set(undefined);
      }
    }, {allowSignalWrites: true});

    effect(() => {
      const centeredPlayer: string | undefined = this.centeredPlayer();
      if (centeredPlayer) {
        this.centerOnPlayer(centeredPlayer);
      } else {
        this.selectedPlayerName.set(undefined);
      }
    }, {allowSignalWrites: true});
  }

  centerOnPlayer(playerName: string) {
    const playerIndex: number = this.players().indexOf(playerName);

    const players: readonly string[] = this.players();
    if (players.length === 0) {
      return;
    }

    const normalizedIndex: number = this.normalizeIndex(playerIndex, players.length);
    const selectedPlayerName: string = players[normalizedIndex];

    this.scrollCenterIndex.set(normalizedIndex);
    this.selectedPlayerName.set(selectedPlayerName);
    this.dragOffsetPx.set(0);
  }

  protected playerNameForOffset(offsetFromCenter: number): string {
    const players: readonly string[] = this.players();
    if (players.length === 0) {
      return '';
    }

    const index: number = this.normalizeIndex(this.scrollCenterIndex() + offsetFromCenter, players.length);
    return players[index];
  }

  protected highlighted(offsetFromCenter: number): boolean {
    const selectedPlayerName: string | undefined = this.selectedPlayerName();
    return !!selectedPlayerName && this.playerNameForOffset(offsetFromCenter) === selectedPlayerName;
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
}
