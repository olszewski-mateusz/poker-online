import {ChangeDetectionStrategy, Component, inject, input, InputSignal, Signal} from '@angular/core';
import {MatToolbar} from '@angular/material/toolbar';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {buildMyPlayerSignal, Game, Player} from '../../../model';
import {RouterService} from '../../../services';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';
import {ApiRestService} from '../../../services';
import {MatDialog} from '@angular/material/dialog';
import {AiPlayerNameDialogComponent} from '../../dialogs';
import {filter, map, mergeMap} from 'rxjs';

@Component({
  selector: 'poker-game-toolbar',
  standalone: true,
  imports: [
    MatToolbar,
    MatIconButton,
    MatIcon,
    MatButton,
    MatMenuTrigger,
    MatMenu,
    MatMenuItem
  ],
  templateUrl: './toolbar.component.html',
  styleUrl: './toolbar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToolbarComponent {
  private readonly routerService: RouterService = inject(RouterService);
  private readonly apiRestService: ApiRestService = inject(ApiRestService);
  private readonly dialog: MatDialog = inject(MatDialog);

  game: InputSignal<Game> = input.required<Game>();
  mobile: InputSignal<boolean> = input.required<boolean>();
  myPlayer: Signal<Player | undefined> = buildMyPlayerSignal(this.game);

  leave(): void {
    this.routerService.navigateToStartPage().subscribe();
  }

  addAiPlayer(): void {
    const gameId = this.game().gameId;
    this.dialog.open(AiPlayerNameDialogComponent, {disableClose: true}).afterClosed().pipe(
      map((displayName: string | undefined) => displayName?.trim() ?? ''),
      filter((displayName: string) => displayName.length > 0),
      mergeMap((displayName: string) => this.apiRestService.sendAiJoin(gameId, displayName))
    ).subscribe();
  }
}
