import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CategoriaSalon,
  EvaluacionDiaria,
  ProductoSalon,
  Queja,
  QuejaRequest,
} from '../models/cliente.model';
import { Pagina } from '../models/pagina.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/api/cliente`;

  menuSalon(): Observable<CategoriaSalon[]> {
    return this.http.get<CategoriaSalon[]>(`${this.url}/menu-salon`);
  }

  favoritos(): Observable<ProductoSalon[]> {
    return this.http.get<ProductoSalon[]>(`${this.url}/favoritos`);
  }

  alternarMeGusta(idProducto: number): Observable<{ idProducto: number; meGusta: boolean }> {
    return this.http.post<{ idProducto: number; meGusta: boolean }>(
      `${this.url}/favoritos/${idProducto}/me-gusta`,
      null,
    );
  }

  crearQueja(datos: QuejaRequest): Observable<Queja> {
    return this.http.post<Queja>(`${this.url}/quejas`, datos);
  }

  misQuejas(): Observable<Pagina<Queja>> {
    return this.http.get<Pagina<Queja>>(`${this.url}/quejas`, { params: { size: 20 } });
  }

  evaluar(estrellas: number, comentario: string | null): Observable<EvaluacionDiaria> {
    return this.http.post<EvaluacionDiaria>(`${this.url}/evaluacion`, { estrellas, comentario });
  }

  evaluacionDeHoy(): Observable<EvaluacionDiaria | null> {
    return this.http.get<EvaluacionDiaria | null>(`${this.url}/evaluacion/hoy`);
  }
}
