import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { GraficasInventario, VentaDia } from '../models/inventario.model';

@Injectable({ providedIn: 'root' })
export class GraficasService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/api/graficas`;

  inventario(dias = 14): Observable<GraficasInventario> {
    return this.http.get<GraficasInventario>(`${this.url}/inventario`, { params: { dias } });
  }

  ventas(dias = 7): Observable<VentaDia[]> {
    return this.http.get<VentaDia[]>(`${this.url}/ventas`, { params: { dias } });
  }
}
