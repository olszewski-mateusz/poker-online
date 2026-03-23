import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  constructor() { }

  hasAdminAccess(gameId: string) {
    return localStorage.getItem(`admin-secret:${gameId}`) !== null;
  }

  getAdminSecret(gameId: string) {
    return localStorage.getItem(`admin-secret:${gameId}`);
  }
}
