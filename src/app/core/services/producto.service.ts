import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pagina } from '../models/pagina.model';
import { Producto, ProductoRequest } from '../models/producto.model';

export interface FiltroProductos {
  q?: string;
  categoria?: string | null;
  disponible?: boolean | null;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/api/productos`;

  listar(filtro: FiltroProductos = {}): Observable<Pagina<Producto>> {
    let params = new HttpParams().set('page', filtro.page ?? 0).set('size', filtro.size ?? 20);
    if (filtro.q) params = params.set('q', filtro.q);
    if (filtro.categoria) params = params.set('categoria', filtro.categoria);
    if (filtro.disponible !== null && filtro.disponible !== undefined) {
      params = params.set('disponible', filtro.disponible);
    }
    return this.http.get<Pagina<Producto>>(this.url, { params });
  }

  crear(datos: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.url, datos);
  }

  actualizar(id: number, datos: ProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.url}/${id}`, datos);
  }

  cambiarDisponibilidad(id: number, disponible: boolean): Observable<Producto> {
    return this.http.patch<Producto>(`${this.url}/${id}/disponibilidad`, null, { params: { disponible } });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
