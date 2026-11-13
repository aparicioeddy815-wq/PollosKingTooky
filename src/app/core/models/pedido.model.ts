export type EstadoPedido = 'PENDIENTE' | 'EN_PREPARACION' | 'LISTO' | 'ENTREGADO' | 'CANCELADO';
export type TipoPedido = 'LOCAL' | 'PARA_LLEVAR' | 'PEDIDOSYA';

export interface DetallePedido {
  idProducto: number;
  producto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  observaciones: string | null;
}

export interface Pedido {
  idPedido: number;
  numeroTurno: number;
  fechaTurno: string;
  estado: EstadoPedido;
  tipoPedido: TipoPedido;
  total: number;
  fechaHora: string;
  horaPreparacion: string | null;
  horaListo: string | null;
  horaEntrega: string | null;
  minutosTranscurridos: number;
  detalles: DetallePedido[];
}

export interface ItemPedidoRequest {
  idProducto: number;
  cantidad: number;
  observaciones: string | null;
  guarniciones: { idGuarnicion: number; cantidad: number }[];
}

export interface PedidoRequest {
  tipoPedido: TipoPedido;
  idUsuarioCliente: number | null;
  items: ItemPedidoRequest[];
}
