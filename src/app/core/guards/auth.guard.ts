import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AvisoService } from '../services/aviso.service';

/** Exige un permiso concreto (por ejemplo, RESERVA_CREAR). */
export const permisoGuard = (permiso: string): CanActivateFn => {
  return (_ruta, estado) => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.autenticado()) {
      return router.createUrlTree(['/ingresar'], { queryParams: { volverA: estado.url } });
    }
    if (auth.tienePermiso(permiso)) {
      return true;
    }
    inject(AvisoService).info('Tu cuenta no tiene acceso a esa sección.');
    return router.createUrlTree(['/']);
  };
};

/** Exige ser personal del local (cualquier rol distinto de cliente). */
export const personalGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.autenticado()) {
    return router.createUrlTree(['/ingresar'], { queryParams: { volverA: estado.url } });
  }
  return auth.esPersonal() ? true : router.createUrlTree(['/mi-cuenta']);
};

/** Impide que alguien con sesión vuelva a ver login o registro. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.autenticado()) {
    return true;
  }
  return router.createUrlTree([auth.esPersonal() ? '/panel' : '/mi-cuenta']);
};

/** Exige solamente haber iniciado sesión. */
export const autenticadoGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? true : router.createUrlTree(['/ingresar'], { queryParams: { volverA: estado.url } });
};
