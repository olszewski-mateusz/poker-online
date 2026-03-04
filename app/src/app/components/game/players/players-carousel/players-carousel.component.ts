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
export class PlayersCarouselComponent implements OnDestroy {
  private readonly itemHeightPx: number = 44;
  private readonly snapAnimationMs: number = 180;

  private readonly centeredIndex: WritableSignal<number> = signal(0);
  private readonly dragOffsetPx: WritableSignal<number> = signal(0);
  private readonly pointerState: WritableSignal<{ id: number, y: number } | undefined> = signal(undefined);

  private wheelSnapTimeoutId: ReturnType<typeof setTimeout> | undefined;
  private snapAnimationTimeoutId: ReturnType<typeof setTimeout> | undefined;

  players: InputSignal<readonly string[]> = input.required<readonly string[]>();
  centeredPlayer: InputSignal<string | undefined> = input<string>();

  centeredPlayerChange: OutputEmitterRef<string> = output<string>();

  protected readonly renderedSlots: readonly number[] = [-3, -2, -1, 0, 1, 2, 3];
  protected readonly isDragging = signal(false);
  protected readonly isSnapping = signal(false);

  constructor() {
    effect(() => {
      const players: readonly string[] = this.players();
      if (players.length === 0) {
        this.centeredIndex.set(0);
        this.dragOffsetPx.set(0);
        return;
      }
      this.centeredIndex.update(currentIndex => this.normalizeIndex(currentIndex, players.length));
    }, {allowSignalWrites: true});

    effect(() => {
      const centeredPlayer: string | undefined = this.centeredPlayer();
      if (centeredPlayer) {
        this.centerOnPlayer(centeredPlayer);
      }
    }, {allowSignalWrites: true});

    effect(() => {
      const players: readonly string[] = this.players();
      if (players.length === 0) {
        return;
      }
      const centeredIndex: number = this.normalizeIndex(this.centeredIndex(), players.length);
      this.centeredPlayerChange.emit(players[centeredIndex]);
    });
  }

  ngOnDestroy(): void {
    this.clearWheelSnapTimeout();
    this.clearSnapAnimationTimeout();
  }

  centerOnPlayer(playerName: string): boolean {
    const playerIndex: number = this.players().indexOf(playerName);
    if (playerIndex < 0) {
      return false;
    }
    return this.centerOnIndex(playerIndex);
  }

  centerOnIndex(index: number): boolean {
    const playersCount: number = this.players().length;
    if (playersCount === 0) {
      return false;
    }

    this.centeredIndex.set(this.normalizeIndex(index, playersCount));
    this.dragOffsetPx.set(0);
    return true;
  }

  protected playerNameForOffset(offsetFromCenter: number): string {
    const players: readonly string[] = this.players();
    if (players.length === 0) {
      return '';
    }

    const index: number = this.normalizeIndex(this.centeredIndex() + offsetFromCenter, players.length);
    return players[index];
  }

  protected slotTransform(offsetFromCenter: number): string {
    const translateY: number = offsetFromCenter * this.itemHeightPx + this.dragOffsetPx();
    return `translate(-50%, ${translateY}px)`;
  }

  protected onPointerDown(event: PointerEvent): void {
    if (this.players().length < 2) {
      return;
    }

    const target: HTMLElement = event.currentTarget as HTMLElement;
    target.setPointerCapture(event.pointerId);
    this.pointerState.set({id: event.pointerId, y: event.clientY});
    this.isDragging.set(true);
    this.clearWheelSnapTimeout();
    this.stopSnapAnimation();
  }

  protected onPointerMove(event: PointerEvent): void {
    const pointerState: { id: number, y: number } | undefined = this.pointerState();
    if (!pointerState || pointerState.id !== event.pointerId) {
      return;
    }

    const deltaY: number = event.clientY - pointerState.y;
    this.pointerState.set({id: pointerState.id, y: event.clientY});
    this.applyDelta(deltaY);
    event.preventDefault();
  }

  protected onPointerUp(event: PointerEvent): void {
    const pointerState: { id: number, y: number } | undefined = this.pointerState();
    if (!pointerState || pointerState.id !== event.pointerId) {
      return;
    }
    this.releasePointer(event);
    this.snapToNearestItem(true);
  }

  protected onPointerCancel(event: PointerEvent): void {
    const pointerState: { id: number, y: number } | undefined = this.pointerState();
    if (!pointerState || pointerState.id !== event.pointerId) {
      return;
    }
    this.releasePointer(event);
    this.snapToNearestItem(true);
  }

  protected onWheel(event: WheelEvent): void {
    if (this.players().length < 2) {
      return;
    }

    event.preventDefault();
    this.stopSnapAnimation();
    this.applyDelta(-event.deltaY);
    this.clearWheelSnapTimeout();
    this.wheelSnapTimeoutId = setTimeout(() => this.snapToNearestItem(true), 120);
  }

  protected hasPlayers(): boolean {
    return this.players().length > 0;
  }

  protected itemHeight(): number {
    return this.itemHeightPx;
  }

  private releasePointer(event: PointerEvent): void {
    const target: HTMLElement = event.currentTarget as HTMLElement;
    if (target.hasPointerCapture(event.pointerId)) {
      target.releasePointerCapture(event.pointerId);
    }
    this.pointerState.set(undefined);
    this.isDragging.set(false);
  }

  private applyDelta(deltaY: number): void {
    let nextOffset: number = this.dragOffsetPx() + deltaY;
    while (nextOffset >= this.itemHeightPx) {
      nextOffset -= this.itemHeightPx;
      this.shiftCenteredBy(-1);
    }
    while (nextOffset <= -this.itemHeightPx) {
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
    this.centeredIndex.update(index => this.normalizeIndex(index + offset, playersCount));
  }

  private snapToNearestItem(animate: boolean): void {
    const steps: number = Math.round(this.dragOffsetPx() / this.itemHeightPx);
    const hadOffset: boolean = this.dragOffsetPx() !== 0;
    if (steps !== 0) {
      this.shiftCenteredBy(-steps);
    }
    if (animate && (steps !== 0 || hadOffset)) {
      this.startSnapAnimation();
    }
    this.dragOffsetPx.set(0);
  }

  private clearWheelSnapTimeout(): void {
    if (this.wheelSnapTimeoutId) {
      clearTimeout(this.wheelSnapTimeoutId);
      this.wheelSnapTimeoutId = undefined;
    }
  }

  private startSnapAnimation(): void {
    this.clearSnapAnimationTimeout();
    this.isSnapping.set(true);
    this.snapAnimationTimeoutId = setTimeout(() => this.isSnapping.set(false), this.snapAnimationMs);
  }

  private stopSnapAnimation(): void {
    this.clearSnapAnimationTimeout();
    this.isSnapping.set(false);
  }

  private clearSnapAnimationTimeout(): void {
    if (this.snapAnimationTimeoutId) {
      clearTimeout(this.snapAnimationTimeoutId);
      this.snapAnimationTimeoutId = undefined;
    }
  }

  private normalizeIndex(index: number, length: number): number {
    return ((index % length) + length) % length;
  }
}
