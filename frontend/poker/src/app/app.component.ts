import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {CardComponent, CardSize} from "./card/card.component";
import {Rank} from './model/rank';
import {Suit} from './model/suit';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
}
