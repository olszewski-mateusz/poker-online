import {ChangeDetectionStrategy, Component} from '@angular/core';
import {CardComponent, CardSize} from "./card/card.component";
import {Suit} from '../../model/suit';
import {Rank} from '../../model/rank';

@Component({
  selector: 'app-game-page',
  standalone: true,
    imports: [
        CardComponent
    ],
  templateUrl: './game-page.component.html',
  styleUrl: './game-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GamePageComponent {
  protected readonly Object = Object;
  protected readonly Suit = Suit;
  protected readonly Rank = Rank;
  protected readonly CardSize = CardSize;
}
