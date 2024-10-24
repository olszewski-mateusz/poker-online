import { Routes } from '@angular/router';
import {StartPageComponent} from './components/start-page/start-page.component';
import {PageNotFoundComponent} from './components/errors/page-not-found/page-not-found.component';
import {GamePageComponent} from './components/game-page/game-page.component';
import {GameNotFoundComponent} from './components/errors/game-not-found/game-not-found.component';

export const routes: Routes = [
  { path: 'start', component: StartPageComponent, title: 'First component',},
  { path: 'game/not-found', component: GameNotFoundComponent },
  { path: 'game/:gameId', component: GamePageComponent },
  { path: '',   redirectTo: '/start', pathMatch: 'full' }, // redirect to `StartPageComponent`
  { path: '**', component: PageNotFoundComponent },  // Wildcard route for a 404 page
];
