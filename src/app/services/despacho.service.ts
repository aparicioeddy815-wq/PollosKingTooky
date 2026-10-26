import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Ticket {
  id?: number;
  number: string;
  status: 'READY' | 'PREPARING';
  timeAgo: string;
}

@Injectable({
  providedIn: 'root'
})
export class DespachoService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/pedidos';
  getPedidos(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(this.apiUrl);
  }

  // Actualizar estado de comanda
  actualizarEstado(ticketNumber: string, status: 'READY'): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.apiUrl}/${ticketNumber}/estado`, { status });
  }

  // Redirección directa al menú de Pollos King Tooky en PedidosYa
  redirectToPedidosYa(): void {
    window.open(
      'https://www.pedidosya.com.bo/restaurantes/la-paz/pollos-king-tooky-56a72d64-005c-4453-8c3a-94eabe463e6b-menu',
      '_blank'
    );
  }
}
