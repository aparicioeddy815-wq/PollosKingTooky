import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardAdmin } from '../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/api/dashboard/admin`;

  dashboard(fecha?: string | null): Observable<DashboardAdmin> {
    let params = new HttpParams();
    if (fecha) params = params.set('fecha', fecha);
    return this.http.get<DashboardAdmin>(this.url, { params });
  }

  exportarResumenDiario(fecha?: string | null): Observable<Blob> {
    let params = new HttpParams();
    if (fecha) params = params.set('fecha', fecha);
    return this.http.get(`${this.url}/exportar`, { params, responseType: 'blob' });
  }
}
