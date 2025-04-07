import {inject, Injectable, signal, WritableSignal} from '@angular/core';
import {AppProperties} from '../model';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class PropertiesService {
  private readonly http: HttpClient = inject(HttpClient);
  private readonly properties: WritableSignal<AppProperties | undefined> = signal<AppProperties| undefined>(undefined);

  constructor() {
    this.http.get<AppProperties>('/properties/properties.json').subscribe(value => {
      this.properties.set(value);
    })
  }

  get serverHost(): string {
    return this.properties()?.serverHost ?? location.host;
  }
}
