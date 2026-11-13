import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pagina } from '../models/pagina.model';
import { ReprogramarRequest, Reserva, ReservaRequest } from '../models/reserva.model';

@Injectable({ providedIn: 'root' })
export class ReservaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/api/reservas`;

  crear(datos: ReservaRequest): Observable<Reserva> {
    return this.http.post<Reserva>(this.url, datos);
  }

  misReservas(estado?: string | null, page = 0): Observable<Pagina<Reserva>> {
    let params = new HttpParams().set('page', page).set('size', 20);
    if (estado) params = params.set('estado', estado);
    return this.http.get<Pagina<Reserva>>(`${this.url}/mis`, { params });
  }

  gestion(estado?: string | null, fecha?: string | null, page = 0): Observable<Pagina<Reserva>> {
    let params = new HttpParams().set('page', page).set('size', 20);
    if (estado) params = params.set('estado', estado);
    if (fecha) params = params.set('fecha', fecha);
    return this.http.get<Pagina<Reserva>>(this.url, { params });
  }

  cancelar(id: number): Observable<Reserva> {
    return this.http.patch<Reserva>(`${this.url}/${id}/cancelar`, null);
  }

  reprogramar(id: number, datos: ReprogramarRequest): Observable<Reserva> {
    return this.http.patch<Reserva>(`${this.url}/${id}/reprogramar`, datos);
  }

  pagar(id: number): Observable<Reserva> {
    return this.http.patch<Reserva>(`${this.url}/${id}/pagar`, null);
  }

  completar(id: number): Observable<Reserva> {
    return this.http.patch<Reserva>(`${this.url}/${id}/completar`, null);
  }

  recibo(id: number): Observable<Blob> {
    return this.http.get(`${this.url}/${id}/recibo`, { responseType: 'blob' });
  }
}
