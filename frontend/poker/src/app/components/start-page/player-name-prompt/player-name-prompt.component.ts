import {ChangeDetectionStrategy, Component, model} from '@angular/core';
import {MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';
import {MatError, MatFormField} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'app-player-name-prompt',
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
  templateUrl: './player-name-prompt.component.html',
  styleUrl: './player-name-prompt.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlayerNamePromptComponent {
  playerName = model<string|null>(null);
}
