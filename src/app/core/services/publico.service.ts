import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MenuPublico } from '../models/producto.model';
import { Horario, InicioPublico, SobreNosotros, TurnosPublico, Ubicacion } from '../models/publico.model';

@Injectable({ providedIn: 'root' })
export class PublicoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/api/publico`;

  inicio(): Observable<InicioPublico> {
    return this.http.get<InicioPublico>(`${this.url}/inicio`);
  }

  menu(): Observable<MenuPublico> {
    return this.http.get<MenuPublico>(`${this.url}/menu`);
  }

  horarios(): Observable<Horario[]> {
    return this.http.get<Horario[]>(`${this.url}/horarios`);
  }

  ubicacion(): Observable<Ubicacion> {
    return this.http.get<Ubicacion>(`${this.url}/ubicacion`);
  }

  sobreNosotros(): Observable<SobreNosotros> {
    return this.http.get<SobreNosotros>(`${this.url}/sobre-nosotros`);
  }

  turnos(): Observable<TurnosPublico> {
    return this.http.get<TurnosPublico>(`${this.url}/turnos`);
  }
}
