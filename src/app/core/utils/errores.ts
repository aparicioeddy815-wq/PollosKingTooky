import { HttpErrorResponse } from '@angular/common/http';
import { ErrorApi } from '../models/pagina.model';

/** Convierte cualquier error HTTP en un texto legible para el usuario. */
export function mensajeDeError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Ocurrió un error inesperado. Intenta de nuevo.';
  }
  if (error.status === 0) {
    return 'No hay conexión con el servidor. Revisa que el backend esté encendido.';
  }
  const cuerpo = error.error as Partial<ErrorApi> | null;
  if (cuerpo?.detalles?.length) {
    return cuerpo.detalles.join('. ');
  }
  if (cuerpo?.mensaje) {
    return cuerpo.mensaje;
  }
  if (error.status === 403) {
    return 'No tienes permiso para realizar esta acción.';
  }
  return 'No se pudo completar la operación.';
}
