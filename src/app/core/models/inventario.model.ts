export type EstadoStock = 'CRITICO' | 'BAJO' | 'NORMAL';
export type MotivoMerma = 'VENCIMIENTO' | 'DETERIORO' | 'CAIDA' | 'ERROR_PREPARACION' | 'OTRO';

export interface Insumo {
  idInsumo: number;
  codigo: string;
  nombre: string;
  idCategoria: number;
  categoria: string;
  tipo: string;
  unidadMedida: string;
  stockActual: number;
  umbralMinimo: number;
  consumoHoy: number;
  consumoPromedioDiario: number;
  estado: EstadoStock;
  consumoProyectado: number;
  deficitProyectado: number;
  superavitSeguro: number;
  costoUnitario: number;
  valorStock: number;
  actualizadoEn: string;
}

export interface ResumenInventario {
  alertasUrgentes: number;
  insumosBajos: number;
  abastecimientoNecesario: number;
  valoracionInventario: number;
  articulosAReponer: number;
  totalInsumos: number;
  diasProyeccion: number;
}

export interface Proveedor {
  idProveedor: number;
  nombre: string;
  telefono: string | null;
  contacto: string | null;
}

export interface Merma {
  idMerma: number;
  idInsumo: number;
  insumo: string;
  unidadMedida: string;
  cantidad: number;
  motivo: MotivoMerma;
  observacion: string | null;
  costoTotal: number;
  fechaHora: string;
  registradoPor: string;
}

export interface EntradaRequest {
  idInsumo: number;
  cantidad: number;
  idProveedor: number | null;
  costoUnitario: number | null;
  documento: string | null;
}

export interface MermaRequest {
  idInsumo: number;
  cantidad: number;
  motivo: MotivoMerma;
  observacion: string | null;
}

// ---- gráficas
export interface Punto {
  etiqueta: string;
  valor: number;
}

export interface MovimientoDia {
  fecha: string;
  entradas: number;
  consumos: number;
  mermas: number;
}

export interface Cobertura {
  idInsumo: number;
  nombre: string;
  unidadMedida: string;
  stockActual: number;
  umbralMinimo: number;
  consumoPromedioDiario: number;
  diasCobertura: number | null;
  estado: EstadoStock;
}

export interface GraficasInventario {
  dias: number;
  valorPorCategoria: Punto[];
  estadoDelStock: Punto[];
  mermasPorMotivo: Punto[];
  movimientosDiarios: MovimientoDia[];
  menorCobertura: Cobertura[];
}

export interface VentaDia {
  fecha: string;
  monto: number;
}
