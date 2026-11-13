import { Injectable, inject } from '@angular/core';
import { MessageService } from 'primeng/api';
import { mensajeDeError } from '../utils/errores';

/** Atajo para mostrar mensajes (toasts) con textos consistentes. */
@Injectable({ providedIn: 'root' })
export class AvisoService {
  private readonly mensajes = inject(MessageService);

  exito(detalle: string, titulo = 'Listo'): void {
    this.mensajes.add({ severity: 'success', summary: titulo, detail: detalle, life: 3500 });
  }

  info(detalle: string, titulo = 'Aviso'): void {
    this.mensajes.add({ severity: 'info', summary: titulo, detail: detalle, life: 5000 });
  }

  error(error: unknown, titulo = 'No se pudo completar'): void {
    this.mensajes.add({ severity: 'error', summary: titulo, detail: mensajeDeError(error), life: 6000 });
  }
}
