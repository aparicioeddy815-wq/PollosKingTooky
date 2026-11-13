export type EstadoReserva = 'PENDIENTE_PAGO' | 'PAGADA' | 'COMPLETADA' | 'CANCELADA' | 'CANCELADA_AUTO';

export interface ReservaItem {
  idProducto: number;
  producto: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Reserva {
  idReserva: number;
  codigo: string;
  nombreCliente: string;
  telefono: string;
  correo: string | null;
  fecha: string;
  hora: string;
  turno: string;
  personas: number;
  notas: string | null;
  total: number;
  estado: EstadoReserva;
  fechaCreacion: string;
  fechaPago: string | null;
  puedeCancelar: boolean;
  puedeReprogramar: boolean;
  advertenciaPago: string | null;
  items: ReservaItem[];
}

export interface ReservaRequest {
  nombreCliente: string;
  telefono: string;
  correo: string | null;
  fecha: string;
  hora: string;
  personas: number;
  notas: string | null;
  items: { idProducto: number; cantidad: number }[];
}

export interface ReprogramarRequest {
  fecha: string;
  hora: string;
}
