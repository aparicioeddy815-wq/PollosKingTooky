import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pedido, PedidoRequest } from '../models/pedido.model';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/api/pedidos`;

  listar(fecha?: string | null, estado?: string | null): Observable<Pedido[]> {
    let params = new HttpParams();
    if (fecha) params = params.set('fecha', fecha);
    if (estado) params = params.set('estado', estado);
    return this.http.get<Pedido[]>(this.url, { params });
  }

  crear(datos: PedidoRequest): Observable<Pedido> {
    return this.http.post<Pedido>(this.url, datos);
  }

  cambiarEstado(id: number, estado: string): Observable<Pedido> {
    return this.http.patch<Pedido>(`${this.url}/${id}/estado`, null, { params: { estado } });
  }

  marcarListo(id: number): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.url}/${id}/listo`, null);
  }

  cancelar(id: number): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.url}/${id}/cancelar`, null);
  }

  timbre(id: number): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.url}/${id}/timbre`, null);
  }
}
