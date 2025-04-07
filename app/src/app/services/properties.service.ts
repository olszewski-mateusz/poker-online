import {Injectable} from '@angular/core';
import {AppProperties} from '../model';
import * as properties from '../../../public/properties/properties.json';

@Injectable({
  providedIn: 'root'
})
export class PropertiesService {
  private readonly properties: AppProperties = properties;

  get serverHost(): string {
    return this.properties.serverHost;
  }
}
