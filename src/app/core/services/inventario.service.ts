import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pagina } from '../models/pagina.model';
import { EntradaRequest, Insumo, Merma, MermaRequest, Proveedor, ResumenInventario } from '../models/inventario.model';

@Injectable({ providedIn: 'root' })
export class InventarioService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/api/inventario`;

  listar(): Observable<Insumo[]> {
    return this.http.get<Insumo[]>(this.url);
  }

  resumen(): Observable<ResumenInventario> {
    return this.http.get<ResumenInventario>(`${this.url}/resumen`);
  }

  proveedores(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(`${this.url}/proveedores`);
  }

  mermas(page = 0, size = 50): Observable<Pagina<Merma>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Pagina<Merma>>(`${this.url}/mermas`, { params });
  }

  registrarEntrada(datos: EntradaRequest): Observable<Insumo> {
    return this.http.post<Insumo>(`${this.url}/entradas`, datos);
  }

  registrarMerma(datos: MermaRequest): Observable<Merma> {
    return this.http.post<Merma>(`${this.url}/mermas`, datos);
  }

  exportar(formato: 'xlsx' | 'pdf' | 'csv'): Observable<Blob> {
    return this.http.get(`${this.url}/exportar`, { params: { formato }, responseType: 'blob' });
  }
}
