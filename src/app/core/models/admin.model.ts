export interface RitmoVenta {
  hora: number;
  monto: number;
  pedidos: number;
}

export interface TopMenu {
  idProducto: number;
  nombre: string;
  unidades: number;
  monto: number;
}

export interface Operaciones {
  tiempoPromedioDespachoMin: number;
  objetivoDespachoMin: number;
  quejasDelMes: number;
  stockPolloKg: number;
  umbralPolloKg: number;
  diasCoberturaPollo: number;
  alertasDeStock: number;
}

export interface EventoBitacora {
  idBitacora: number;
  fechaHora: string;
  tipo: string;
  modulo: string;
  usuario: string;
  rolCodigo: string;
  accion: string;
  nivel: string;
  ip: string | null;
}

export interface DashboardAdmin {
  fecha: string;
  ventasTotales: number;
  variacionVsPromedioPct: number;
  comandasAtendidas: number;
  margenOperativoPct: number;
  sesionesEnVivo: number;
  ritmoDeVentas: RitmoVenta[];
  topMenu: TopMenu[];
  operaciones: Operaciones;
  bitacoraReciente: EventoBitacora[];
}
