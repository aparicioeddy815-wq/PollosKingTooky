export interface InsumoCritico {
  idInsumo: number;
  nombre: string;
  unidadMedida: string;
  stockActual: number;
  stockMinimo: number;
}

export interface VentaCategoria {
  categoria: string;
  nombre: string;
  monto: number;
}

export interface DashboardGerente {
  fecha: string;
  ventasDelDia: number;
  pedidosAtendidos: number;
  pedidosEnCurso: number;
  stockCritico: number;
  insumosCriticos: InsumoCritico[];
  masVendido: { nombre: string; unidades: number } | null;
  ventasPorCategoria: VentaCategoria[];
}

export interface Usuario {
  idUsuario: number;
  nombreUsuario: string;
  nombreCompleto: string;
  correo: string | null;
  idRol: number;
  rol: string;
  estado: number;
  fechaCreacion: string;
  ultimoAcceso: string | null;
  turno: string | null;
}
