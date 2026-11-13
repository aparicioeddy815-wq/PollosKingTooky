import { Component, computed, input } from '@angular/core';
import { TagModule } from 'primeng/tag';
import { ESTADOS_PEDIDO, ESTADOS_RESERVA, etiquetaDe } from '../../../core/utils/etiquetas';

type Severidad = 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast';

const SEVERIDAD: Record<string, Severidad> = {
  PENDIENTE: 'warn',
  EN_PREPARACION: 'info',
  LISTO: 'success',
  ENTREGADO: 'secondary',
  CANCELADO: 'danger',
  PENDIENTE_PAGO: 'warn',
  PAGADA: 'info',
  COMPLETADA: 'success',
  CANCELADA: 'danger',
  CANCELADA_AUTO: 'danger',
};

/** Etiqueta de color para el estado de un pedido o de una reserva. */
@Component({
  selector: 'app-estado-tag',
  imports: [TagModule],
  template: `<p-tag [value]="texto()" [severity]="severidad()" />`,
})
export class EstadoTag {
  readonly estado = input.required<string>();
  readonly tipo = input<'pedido' | 'reserva'>('pedido');

  protected readonly severidad = computed(() => SEVERIDAD[this.estado()] ?? 'secondary');
  protected readonly texto = computed(() =>
    etiquetaDe<string>(this.tipo() === 'pedido' ? ESTADOS_PEDIDO : ESTADOS_RESERVA, this.estado()),
  );
}
