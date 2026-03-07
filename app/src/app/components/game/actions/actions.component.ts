import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  InputSignal,
  signal,
  WritableSignal
} from '@angular/core';
import {ActionType, Card, Game, GamePhase, ValidAction} from '../../../model';
import {ApiRestService} from '../../../services';
import {ReplaceCardsService} from './replace-cards.service';
import {ChipsAmountSetterComponent} from './chips-amount-setter/chips-amount-setter.component';
import {MatButton} from '@angular/material/button';
import {ActionConfirmation} from './action-confirmation/action-confirmation';

@Component({
  selector: 'poker-game-actions',
  standalone: true,
  imports: [
    ChipsAmountSetterComponent,
    MatButton,
    ActionConfirmation
  ],
  templateUrl: './actions.component.html',
  styleUrl: './actions.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActionsComponent {
  game: InputSignal<Game> = input.required<Game>();
  mobile: InputSignal<boolean> = input.required<boolean>();

  private readonly apiRestService: ApiRestService = inject(ApiRestService);
  private readonly replaceCardsService: ReplaceCardsService = inject(ReplaceCardsService);

  protected readonly additionalPanelContext: WritableSignal<AdditionalPanelContext | null> = signal<AdditionalPanelContext | null>(null);

  protected readonly availableActions = computed(() => {
    return this.game().validActions;
  });

  onActionClick(action: ValidAction): void {
    const game: Game = this.game();
    switch (action.type) {
      case ActionType.ALL_IN:
        this.additionalPanelContext.set({type: AdditionalPanelType.ACTION_CONFIRMATION, action});
        break;
      case ActionType.CALL:
        this.apiRestService.sendCall(game.gameId, game.myId).subscribe();
        break;
      case ActionType.CHECK:
        this.apiRestService.sendCheck(game.gameId, game.myId).subscribe();
        break;
      case ActionType.FOLD:
        this.apiRestService.sendFold(game.gameId, game.myId).subscribe();
        break;
      case ActionType.REPLACE: {
        const cardsToReplace: Card[] = this.replaceCardsService.cardsToReplace();
        this.apiRestService.sendReplace(game.gameId, game.myId, cardsToReplace).subscribe();
        this.replaceCardsService.clearSelection();
        break;
      }
      case ActionType.READY: {
        const ready: boolean = Boolean(JSON.parse(action.properties["ready"]));
        this.apiRestService.sendReady(game.gameId, game.myId, ready).subscribe();
        break;
      }
      case ActionType.BET:
      case ActionType.RAISE:
        this.additionalPanelContext.set({type: AdditionalPanelType.CHIPS_AMOUNT_SETTER, action});
        break;
      case ActionType.JOIN:
        break;
    }
  }


  protected buildChipsAmountSetterContext(action: ValidAction) {
    let label;
    switch (action.type) {
      case ActionType.RAISE:
        label = "Raise"
        break;
      case ActionType.BET:
        label = "Bet"
        break;
      default:
        console.error("Wrong action type", action);
    }

    return <ChipsAmountSetterContext> {
      type: action.type,
      label: label,
      minValue: +action.properties["min"],
      maxValue: +action.properties["max"]
    }
  }

  protected buildAdditionalConfirmationContext(action: ValidAction) {
    if (action.type !== ActionType.ALL_IN) {
      console.error("Wrong action type", action);
    }

    return <ActionConfirmationContext> {
      type: action.type,
      title: "Are you sure you want to go all in?"
    }
  }

  protected readyButtonLabel(action: ValidAction): string {
    const targetReady: boolean = action.properties["ready"] === "true";
    if (!targetReady) {
      return "Not ready";
    }
    return this.game().phase === GamePhase.SHOWDOWN || this.game().phase === GamePhase.FINISHED
      ? "Ready for next round"
      : "Ready";
  }

  protected closeAmountPanel(): void {
    this.additionalPanelContext.set(null);
  }

  protected actionButtonLabel(action: ValidAction) {
    switch (action.type) {
      case ActionType.READY:
        return this.readyButtonLabel(action)
      case ActionType.REPLACE:
        return `Replace cards (${this.replaceCardsService.cardsToReplace().length})`;
      case ActionType.CHECK:
        return "Check"
      case ActionType.CALL:
        return "Call"
      case ActionType.BET:
        return "Bet"
      case ActionType.RAISE:
        return "Raise"
      case ActionType.ALL_IN:
        return "All in"
      case ActionType.FOLD:
        return "Fold"
      default:
        return "Unknown"
    }
  }

  protected actionWidthByCount(size: number) {
    let value;
    if (size === 1) {
      value = 100
    } else if (size === 4) {
      value = 50;
    } else if (size === 3) {
      value = 25;
    } else if (size === 2) {
      value = 50;
    }
    return value + '%';
  }
}

export type AdditionalPanelContext = {
  type: AdditionalPanelType
  action: ValidAction
}

export type ActionConfirmationContext = {
  title: string
  type: ActionType.ALL_IN
}

export type ChipsAmountSetterContext = {
  label: string
  minValue: number
  maxValue: number
  type: ActionType.BET | ActionType.RAISE
}

export enum AdditionalPanelType {
  ACTION_CONFIRMATION = 'ACTION_CONFIRMATION',
  CHIPS_AMOUNT_SETTER = 'CHIPS_AMOUNT_SETTER',
}
