import {ChangeDetectionStrategy, Component, model, ModelSignal} from '@angular/core';
import {MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';
import {MatError, MatFormField} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'poker-dialog-player-name',
  standalone: true,
  imports: [
    MatDialogContent,
    MatDialogTitle,
    MatDialogActions,
    MatButton,
    MatDialogClose,
    MatError,
    MatFormField,
    MatInput,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './player-name-dialog.component.html',
  styleUrl: './player-name-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlayerNameDialogComponent {
  playerName: ModelSignal<string | null> = model<string | null>(null);
}
