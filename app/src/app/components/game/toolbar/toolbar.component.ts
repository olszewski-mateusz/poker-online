import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  InputSignal,
  signal,
  Signal,
  WritableSignal
} from '@angular/core';
import {MatToolbar} from '@angular/material/toolbar';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {buildMyPlayerSignal, Game, Player} from '../../../model';
import {RouterService, ApiRestService} from '../../../services';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';
import {MatDialog} from '@angular/material/dialog';
import {AiPlayerNameDialogComponent} from '../../dialogs';
import {filter, map, mergeMap} from 'rxjs';
import {AdminService} from '../../../services/admin.service';

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
  private readonly adminService: AdminService = inject(AdminService);

  game: InputSignal<Game> = input.required<Game>();
  mobile: InputSignal<boolean> = input.required<boolean>();
  myPlayer: Signal<Player | undefined> = buildMyPlayerSignal(this.game);
  removablePlayers: Signal<Player[]> = computed(() => {
    const game = this.game();
    return game.players.filter(player => player.index !== game.myIndex);
  });

  protected adminAccess: WritableSignal<boolean> = signal<boolean>(false);
  protected adminSecret: WritableSignal<string | null> = signal<string | null>(null);


  constructor() {
    effect(() => {
      const game = this.game();
      if (this.adminService.hasAdminAccess(game.gameId)) {
        this.adminAccess.set(true);
        this.adminSecret.set(this.adminService.getAdminSecret(game.gameId));
      } else {
        this.adminAccess.set(false);
        this.adminSecret.set(null);
      }
    }, {allowSignalWrites: true});
  }

  leave(): void {
    this.routerService.navigateToStartPage().subscribe();
  }

  addAiPlayer(): void {
    const gameId = this.game().gameId;
    this.dialog.open(AiPlayerNameDialogComponent, {disableClose: true}).afterClosed().pipe(
      map((displayName: string | undefined) => displayName?.trim() ?? ''),
      filter((displayName: string) => displayName.length > 0),
      mergeMap((displayName: string) => this.apiRestService.sendAiJoin(gameId, `${displayName} (AI)`, this.adminSecret()))
    ).subscribe();
  }

  removePlayer(playerIndex: number): void {
    const game = this.game();
    this.apiRestService.sendRemovePlayer(game.gameId, playerIndex, game.myId, this.adminSecret()).subscribe();
  }

  resetGame(): void {
    const game = this.game();
    this.apiRestService.sendResetGame(game.gameId, game.myId, this.adminSecret()).subscribe();
  }
}
