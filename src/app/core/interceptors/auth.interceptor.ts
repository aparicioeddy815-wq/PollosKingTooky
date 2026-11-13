import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

// Rutas que no necesitan (ni deben llevar) el token
const RUTAS_ABIERTAS = [
  '/api/publico',
  '/api/auth/login',
  '/api/auth/register',
  '/api/auth/refresh',
  '/api/auth/olvide-contrasena',
  '/api/auth/restablecer-contrasena',
];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const esDelBackend = req.url.startsWith(environment.apiUrl);
  const esAbierta = RUTAS_ABIERTAS.some((ruta) => req.url.includes(ruta));
  if (!esDelBackend || esAbierta) {
    return next(req);
  }

  const conToken = (peticion: HttpRequest<unknown>) => {
    const token = auth.accessToken();
    return token ? peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : peticion;
  };

  return next(conToken(req)).pipe(
    catchError((error: HttpErrorResponse) => {
      const tokenVencido = error.status === 401 && auth.refreshToken() !== null;
      if (!tokenVencido) {
        return throwError(() => error);
      }
      // Un intento de renovación; si falla, se cierra la sesión
      return auth.renovar().pipe(
        switchMap(() => next(conToken(req))),
        catchError((errorRenovacion) => {
          auth.cerrarSesionLocal();
          router.navigate(['/ingresar']);
          return throwError(() => errorRenovacion);
        }),
      );
    }),
  );
};
