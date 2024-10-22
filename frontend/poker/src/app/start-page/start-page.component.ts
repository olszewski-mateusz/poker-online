import {ChangeDetectionStrategy, Component, inject, model} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatFormFieldModule} from '@angular/material/form-field';
import {MatInput, MatInputModule} from '@angular/material/input';
import {MatIcon} from '@angular/material/icon';
import {HttpClient, provideHttpClient} from '@angular/common/http';
import {ConnectionService} from '../connection.service';
import {Router} from '@angular/router';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-start-page',
  standalone: true,
  imports: [
    MatButton,
    MatFormField,
    MatInput,
    MatFormFieldModule,
    MatInputModule,
    MatIcon,
    FormsModule
  ],
  templateUrl: './start-page.component.html',
  styleUrl: './start-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StartPageComponent {

  private readonly connectionService: ConnectionService = inject(ConnectionService);
  private readonly router: Router = inject(Router);
  gameId = model<string|null>(null);

  createGame() {
    this.connectionService.createGame().subscribe(value => {
      console.log(value);
      this.router.navigate(['game', value.gameId]);
    })
  }

  joinGame() {
    this.router.navigate(['game', this.gameId()]);
  }
}
