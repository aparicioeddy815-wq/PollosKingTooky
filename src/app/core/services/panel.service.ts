import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardGerente, Usuario } from '../models/panel.model';
import { Pagina } from '../models/pagina.model';

/** Endpoints de gestión interna: resumen del gerente y usuarios. */
@Injectable({ providedIn: 'root' })
export class PanelService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/api`;

  dashboardGerente(fecha?: string | null): Observable<DashboardGerente> {
    let params = new HttpParams();
    if (fecha) params = params.set('fecha', fecha);
    return this.http.get<DashboardGerente>(`${this.url}/dashboard/gerente`, { params });
  }

  usuarios(q?: string | null, estado?: number | null, page = 0): Observable<Pagina<Usuario>> {
    let params = new HttpParams().set('page', page).set('size', 20);
    if (q) params = params.set('q', q);
    if (estado !== null && estado !== undefined) params = params.set('estado', estado);
    return this.http.get<Pagina<Usuario>>(`${this.url}/usuarios`, { params });
  }

  activarUsuario(id: number, activo: boolean): Observable<Usuario> {
    const accion = activo ? 'activar' : 'desactivar';
    return this.http.patch<Usuario>(`${this.url}/usuarios/${id}/${accion}`, null);
  }
}
