import { CategoriaQueja, TipoQueja } from './cliente.model';

// ---------- Quejas (gerente)
export type EstadoQueja = 'RECIBIDA' | 'EN_REVISION' | 'RESPONDIDA';

export interface QuejaGestion {
  idQueja: number;
  idUsuario: number;
  cliente: string;
  tipo: TipoQueja;
  categoria: CategoriaQueja;
  numeroPedido: number | null;
  valoracion: number | null;
  mensaje: string;
  estado: EstadoQueja;
  respuesta: string | null;
  fechaCreacion: string;
  fechaRespuesta: string | null;
  respondidoPor: string | null;
}

export interface ResumenQuejas {
  recibidasMes: number;
  reclamos: number;
  sugerencias: number;
  felicitaciones: number;
  pendientes: number;
  tiempoPromedioDespachoMin: number;
  objetivoDespachoMin: number;
  satisfaccionPorcentaje: number;
  horasRespuestaPromedio: number;
}

// ---------- Entrevistas y entrevistados
export type UrgenciaVacante = 'CRITICA' | 'NORMAL';
export type ModalidadEntrevista = 'PRESENCIAL' | 'VIRTUAL' | 'TELEFONICA';
export type EstadoEntrevista = 'PROGRAMADA' | 'REALIZADA' | 'CANCELADA';
export type TipoDocumento = 'CV' | 'CARNET' | 'ANTECEDENTES' | 'OTRO';
export type EstadoPostulante =
  | 'POSTULADO'
  | 'ENTREVISTA_AGENDADA'
  | 'EN_EVALUACION'
  | 'APROBADO_GERENTE'
  | 'EN_REVISION_ANTECEDENTES'
  | 'CONTRATADO'
  | 'DESCARTADO';

export interface Vacante {
  idVacante: number;
  cargo: string;
  estacion: string;
  urgencia: UrgenciaVacante;
  cantidad: number;
  estado: string;
}

export interface DocumentoPostulante {
  idDocumento: number;
  tipo: TipoDocumento;
  nombreArchivo: string;
  tamanoBytes: number;
  subidoEn: string;
}

export interface Postulante {
  idPostulante: number;
  nombreCompleto: string;
  telefono: string;
  correo: string | null;
  idVacante: number;
  cargo: string;
  urgencia: UrgenciaVacante;
  estado: EstadoPostulante;
  evaluacionGerente: string | null;
  creadoEn: string;
  documentosCompletos: boolean;
  documentosFaltantes: TipoDocumento[];
  documentos: DocumentoPostulante[];
  fechaEntrevista: string | null;
}

export interface Entrevista {
  idEntrevista: number;
  idPostulante: number;
  postulante: string;
  telefono: string;
  cargo: string;
  urgencia: UrgenciaVacante;
  fechaHora: string;
  modalidad: ModalidadEntrevista;
  estado: EstadoEntrevista;
  observaciones: string | null;
  minutosParaInicio: number | null;
  documentosCompletos: boolean;
  cvDisponible: boolean;
  estadoPostulante: EstadoPostulante;
}

export interface ResumenEntrevistas {
  entrevistasHoy: number;
  enEvaluacion: number;
  postulantesActivos: number;
  vacantesAbiertas: number;
  puestosCriticos: string[];
}

export interface AgendaEntrevistas {
  resumen: ResumenEntrevistas;
  hoy: Entrevista[];
  programadas: Entrevista[];
}

export interface ResumenEntrevistados {
  postulantesDelCiclo: number;
  vistoBuenoGerencial: number;
  urgenciaOperativa: number;
  contratadosDelMes: number;
}

export interface PostulanteRequest {
  nombreCompleto: string;
  telefono: string;
  correo: string | null;
  idVacante: number;
}

export interface AgendarRequest {
  idPostulante: number;
  fechaHora: string;
  modalidad: ModalidadEntrevista;
}

export interface ObservacionesRequest {
  observaciones: string | null;
  evaluacionGerente: string | null;
  realizada: boolean;
  aprobar: boolean;
}

export interface ContratarRequest {
  turno: Turno;
  salario: number;
  idEstacion: number | null;
}

// ---------- Personal y roles
export type Turno = 'MANANA' | 'TARDE';

export interface ResumenPersonal {
  totalEmpleados: number;
  empleadosActivos: number;
  suspendidos: number;
  perfilesCreados: number;
  turnoActual: Turno;
  activosEnTurnoActual: number;
  totalPuestos: number;
  puestosOcupados: number;
  ocupacionPorcentaje: number;
}

export interface Empleado {
  idEmpleado: number;
  codigo: string;
  nombreCompleto: string;
  cargo: string;
  turno: Turno;
  salario: number;
  estado: 'ACTIVO' | 'SUSPENDIDO';
  fechaIngreso: string;
  telefono: string | null;
  idUsuario: number | null;
  nombreUsuario: string | null;
  rolCodigo: string | null;
  rol: string | null;
  idEstacion: number | null;
  estacion: string | null;
}

export interface Estacion {
  idEstacion: number;
  nombre: string;
  puestosTotales: number;
  ocupados: number;
  porcentaje: number;
}

export interface Rol {
  idRol: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  nivelSeguridad: string;
  usuariosActivos: number;
  permisos: string[];
}

export interface PermisoCatalogo {
  codigo: string;
  descripcion: string;
}

export interface CrearPerfilRequest {
  nombreUsuario: string;
  correo: string;
  nombreCompleto: string;
  rolCodigo: string;
  contrasena: string | null;
  idEmpleado: number | null;
}

export interface PerfilCreado {
  idUsuario: number;
  nombreUsuario: string;
  rolCodigo: string;
  contrasenaTemporal: string | null;
}

export interface AsignarEmpleadoRequest {
  nombreCompleto: string;
  cargo: string;
  turno: Turno;
  salario: number;
  idEstacion: number | null;
  telefono: string | null;
}

// ---------- Bitácora
export interface ResumenBitacora {
  eventosHoy: number;
  iniciosDeSesionHoy: number;
  sesionesActivas: number;
  suspensionesActivas: number;
  ajustesDeStockHoy: number;
  alertasDeSeguridadHoy: number;
}

export interface FiltrosBitacora {
  categoria: string;
  actor: string;
  desde: string | null;
  hasta: string | null;
  q: string | null;
}

// ---------- Reportes
export interface CanalVenta {
  canal: string;
  monto: number;
  pedidos: number;
  porcentaje: number;
}

export interface SemanaVenta {
  semana: string;
  desde: string;
  hasta: string;
  monto: number;
}

export interface DistribucionCategoria {
  categoria: string;
  nombre: string;
  ingresos: number;
  porcentajeIngresos: number;
  margenNetoEstimadoPct: number;
}

export interface ReporteMensual {
  mes: string;
  ingresos: number;
  costoAlimento: number;
  costoMermas: number;
  margenOperativoPct: number;
  desperdicioPct: number;
  pedidos: number;
  ticketPromedio: number;
  canales: CanalVenta[];
  evolucionSemanal: SemanaVenta[];
  distribucion: DistribucionCategoria[];
}

export interface ReporteGenerado {
  idReporte: number;
  nombre: string;
  tipo: string;
  formato: string;
  periodo: string;
  generadoEn: string;
  tamanoBytes: number;
  programado: boolean;
  generadoPor: string;
}

export interface Respaldo {
  idRespaldo: number;
  fechaHora: string;
  archivo: string;
  tamanoBytes: number;
  estado: string;
  duracionMs: number;
  mensaje: string | null;
  disponible: boolean;
}

// ---------- Cliente
export interface DiaReserva {
  fecha: string;
  diaSemana: string;
  etiqueta: string;
  disponible: boolean;
}

export interface HoraReserva {
  hora: string;
  turno: string;
  cupoRestante: number;
  disponible: boolean;
}

export interface PasoAyuda {
  numero: number;
  titulo: string;
  detalle: string;
}

export interface AyudaCliente {
  whatsappCorporativo: string;
  correoContacto: string;
  mensajeWhatsapp: string;
  pasos: PasoAyuda[];
}
