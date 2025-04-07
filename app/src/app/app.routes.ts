import { Routes } from '@angular/router';
import {HomePageComponent} from './pages/home/home-page.component';
import {PageNotFoundComponent} from './pages/errors/page-not-found/page-not-found.component';
import {GamePageComponent} from './pages/game/game-page.component';
import {GameNotFoundComponent} from './pages/errors/game-not-found/game-not-found.component';

export const routes: Routes = [
  { path: 'start', component: HomePageComponent, title: 'Poker online',},
  { path: 'game/not-found', component: GameNotFoundComponent },
  { path: 'game/:gameId', component: GamePageComponent },
  { path: '',   redirectTo: '/start', pathMatch: 'full' },
  { path: '**', component: PageNotFoundComponent },
];
