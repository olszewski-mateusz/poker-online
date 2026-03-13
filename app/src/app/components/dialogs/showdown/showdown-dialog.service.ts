import {inject, Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Game, GamePhase} from '../../../model';
import {ShowdownBetterComponent, ShowdownBetterDialogData} from '../index';

@Injectable({
  providedIn: 'root'
})
export class ShowdownDialogService {
  private readonly dialog: MatDialog = inject(MatDialog);

  private showdownDialogRef?: MatDialogRef<ShowdownBetterComponent>;

  public sync(game: Game, mobile: boolean): void {
    const showdownPhase = game.phase === GamePhase.SHOWDOWN;

    if (!showdownPhase) {
      this.closeShowdownDialog();
      return;
    }

    if (!this.showdownDialogRef) {
      const data: ShowdownBetterDialogData = {game, mobile};
      this.showdownDialogRef = this.dialog.open(ShowdownBetterComponent, {
        data,
        disableClose: true,
        hasBackdrop: false,
        autoFocus: false,
        restoreFocus: false,
        width: '100vw',
        maxWidth: '100vw',
        height: mobile ? 'calc(100dvh - 56px)' : 'calc(100dvh - 64px)',
        maxHeight: mobile ? 'calc(100dvh - 56px)' : 'calc(100dvh - 64px)',
        position: {
          left: '0',
          top: mobile ? '56px' : '64px'
        },
        panelClass: 'poker-showdown-better-dialog-panel'
      });

      this.showdownDialogRef.afterClosed().subscribe(() => {
        this.showdownDialogRef = undefined;
      });
      return;
    }

    this.showdownDialogRef.componentInstance.updateGame(game);
  }

  private closeShowdownDialog(): void {
    this.showdownDialogRef?.close();
    this.showdownDialogRef = undefined;
  }
}
