import { CategoriaProducto } from '../models/producto.model';
import { CategoriaQueja, TipoQueja } from '../models/cliente.model';
import { EstadoPedido, TipoPedido } from '../models/pedido.model';
import { EstadoReserva } from '../models/reserva.model';

export interface Opcion<T = string> {
  etiqueta: string;
  valor: T;
}

export const CATEGORIAS_PRODUCTO: Opcion<CategoriaProducto>[] = [
  { etiqueta: 'Combos populares', valor: 'COMBOS_POPULARES' },
  { etiqueta: 'Pollo frito', valor: 'POLLO_FRITO' },
  { etiqueta: 'Pipocas y salchipapas', valor: 'PIPOCAS_Y_SALCHIPAPAS' },
  { etiqueta: 'Guarniciones', valor: 'GUARNICIONES' },
  { etiqueta: 'Bebidas', valor: 'BEBIDAS' },
];

export const ESTADOS_PEDIDO: Opcion<EstadoPedido>[] = [
  { etiqueta: 'Pendiente', valor: 'PENDIENTE' },
  { etiqueta: 'En preparación', valor: 'EN_PREPARACION' },
  { etiqueta: 'Listo', valor: 'LISTO' },
  { etiqueta: 'Entregado', valor: 'ENTREGADO' },
  { etiqueta: 'Cancelado', valor: 'CANCELADO' },
];

export const TIPOS_PEDIDO: Opcion<TipoPedido>[] = [
  { etiqueta: 'En el local', valor: 'LOCAL' },
  { etiqueta: 'Para llevar', valor: 'PARA_LLEVAR' },
  { etiqueta: 'PedidosYa', valor: 'PEDIDOSYA' },
];

export const ESTADOS_RESERVA: Opcion<EstadoReserva>[] = [
  { etiqueta: 'Pendiente de pago', valor: 'PENDIENTE_PAGO' },
  { etiqueta: 'Pagada', valor: 'PAGADA' },
  { etiqueta: 'Completada', valor: 'COMPLETADA' },
  { etiqueta: 'Cancelada', valor: 'CANCELADA' },
  { etiqueta: 'Cancelada automáticamente', valor: 'CANCELADA_AUTO' },
];

export const TIPOS_QUEJA: Opcion<TipoQueja>[] = [
  { etiqueta: 'Reclamo', valor: 'RECLAMO' },
  { etiqueta: 'Sugerencia', valor: 'SUGERENCIA' },
  { etiqueta: 'Felicitación', valor: 'FELICITACION' },
];

export const CATEGORIAS_QUEJA: Opcion<CategoriaQueja>[] = [
  { etiqueta: 'Atención', valor: 'ATENCION' },
  { etiqueta: 'Comida', valor: 'COMIDA' },
  { etiqueta: 'Tiempo de espera', valor: 'TIEMPO_ESPERA' },
  { etiqueta: 'Limpieza', valor: 'LIMPIEZA' },
  { etiqueta: 'Otro', valor: 'OTRO' },
];

export function etiquetaDe<T>(opciones: Opcion<T>[], valor: T | null | undefined): string {
  return opciones.find((o) => o.valor === valor)?.etiqueta ?? String(valor ?? '');
}
