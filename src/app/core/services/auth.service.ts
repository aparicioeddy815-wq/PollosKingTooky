import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, finalize, shareReplay, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CambiarContrasenaRequest,
  LoginRequest,
  Mensaje,
  PerfilUsuario,
  PerfilRequest,
  RegistroRequest,
  Sesion,
} from '../models/auth.model';

const CLAVE_SESION = 'kingtooky.sesion';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/api`;

  private readonly _sesion = signal<Sesion | null>(this.leerSesionGuardada());
  private renovacion$?: Observable<Sesion>;

  readonly sesion = this._sesion.asReadonly();
  readonly autenticado = computed(() => this._sesion() !== null);
  readonly esCliente = computed(() => this._sesion()?.rol === 'CLIENTE');
  readonly esPersonal = computed(() => this.autenticado() && !this.esCliente());

  accessToken(): string | null {
    return this._sesion()?.accessToken ?? null;
  }

  refreshToken(): string | null {
    return this._sesion()?.refreshToken ?? null;
  }

  tienePermiso(permiso: string): boolean {
    return this._sesion()?.permisos.includes(permiso) ?? false;
  }

  login(datos: LoginRequest): Observable<Sesion> {
    return this.http.post<Sesion>(`${this.url}/auth/login`, datos).pipe(tap((s) => this.guardar(s)));
  }

  registrar(datos: RegistroRequest): Observable<Sesion> {
    return this.http.post<Sesion>(`${this.url}/auth/register`, datos).pipe(tap((s) => this.guardar(s)));
  }

  /** Pide un nuevo par de tokens; varias peticiones simultáneas comparten una sola renovación. */
  renovar(): Observable<Sesion> {
    if (!this.renovacion$) {
      this.renovacion$ = this.http
        .post<Sesion>(`${this.url}/auth/refresh`, { refreshToken: this.refreshToken() })
        .pipe(
          tap((s) => this.guardar(s)),
          finalize(() => (this.renovacion$ = undefined)),
          shareReplay(1),
        );
    }
    return this.renovacion$;
  }

  logout(): void {
    const refreshToken = this.refreshToken();
    if (refreshToken) {
      this.http.post(`${this.url}/auth/logout`, { refreshToken }).subscribe({ error: () => undefined });
    }
    this.cerrarSesionLocal();
  }

  cerrarSesionLocal(): void {
    localStorage.removeItem(CLAVE_SESION);
    this._sesion.set(null);
  }

  olvideContrasena(correo: string): Observable<Mensaje> {
    return this.http.post<Mensaje>(`${this.url}/auth/olvide-contrasena`, { correo });
  }

  restablecerContrasena(token: string, nuevaContrasena: string): Observable<Mensaje> {
    return this.http.post<Mensaje>(`${this.url}/auth/restablecer-contrasena`, { token, nuevaContrasena });
  }

  obtenerPerfil(): Observable<PerfilUsuario> {
    return this.http.get<PerfilUsuario>(`${this.url}/perfil`);
  }

  actualizarPerfil(datos: PerfilRequest): Observable<PerfilUsuario> {
    return this.http.put<PerfilUsuario>(`${this.url}/perfil`, datos).pipe(
      tap((perfil) => {
        const actual = this._sesion();
        if (actual) {
          this.guardar({ ...actual, nombreCompleto: perfil.nombreCompleto, correo: perfil.correo });
        }
      }),
    );
  }

  cambiarContrasena(datos: CambiarContrasenaRequest): Observable<Mensaje> {
    return this.http.post<Mensaje>(`${this.url}/perfil/contrasena`, datos);
  }

  private guardar(sesion: Sesion): void {
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
    this._sesion.set(sesion);
  }

  private leerSesionGuardada(): Sesion | null {
    try {
      const texto = localStorage.getItem(CLAVE_SESION);
      return texto ? (JSON.parse(texto) as Sesion) : null;
    } catch {
      return null;
    }
  }
}
