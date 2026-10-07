import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccesoModel, AuthResponse } from '../interface/acceso-model';

@Injectable({
  providedIn: 'root'
})
export class ValidarAccesoService {
  private apiUrl = 'http://localhost:8080/api/auth/login';

  constructor(private http: HttpClient) {}

  login(credenciales: AccesoModel): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.apiUrl, credenciales);
  }
}
