import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ResumenGerente {
  ventasHoy: number;
  pedidosAtendidos: number;
  stockCriticoCount: number;
  sugerenciasPendientes: number;
  entrevistasHoyCount: number;
}

@Injectable({
  providedIn: 'root'
})
export class GerenteService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/gerencia';

  // Endpoint para obtener el resumen del dashboard (lo conectaremos cuando habilitemos los endpoints de Spring Boot)
  obtenerResumen(): Observable<ResumenGerente> {
    return this.http.get<ResumenGerente>(`${this.apiUrl}/resumen`);
  }
}
