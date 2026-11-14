import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PerfilUsuario } from '../models/auth.model';
import { EventoBitacora } from '../models/admin.model';
import {
  AgendaEntrevistas,
  AgendarRequest,
  AsignarEmpleadoRequest,
  AyudaCliente,
  ContratarRequest,
  CrearPerfilRequest,
  DiaReserva,
  Empleado,
  Entrevista,
  Estacion,
  FiltrosBitacora,
  HoraReserva,
  ObservacionesRequest,
  PerfilCreado,
  PermisoCatalogo,
  Postulante,
  PostulanteRequest,
  QuejaGestion,
  ReporteGenerado,
  ReporteMensual,
  ResumenBitacora,
  ResumenEntrevistados,
  ResumenPersonal,
  ResumenQuejas,
  Respaldo,
  Rol,
  TipoDocumento,
  Vacante,
} from '../models/gestion.model';
import { Pagina } from '../models/pagina.model';

const API = `${environment.apiUrl}/api`;

type Valor = string | number | boolean | null | undefined;

/** Arma los parámetros de la URL ignorando los vacíos. */
function armar(valores: Record<string, Valor>): HttpParams {
  let params = new HttpParams();
  for (const [clave, valor] of Object.entries(valores)) {
    if (valor !== null && valor !== undefined && valor !== '') params = params.set(clave, valor);
  }
  return params;
}

@Injectable({ providedIn: 'root' })
export class QuejasService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API}/quejas`;

  listar(tipo: string | null, page = 0, size = 10): Observable<Pagina<QuejaGestion>> {
    return this.http.get<Pagina<QuejaGestion>>(this.url, { params: armar({ tipo, page, size }) });
  }

  resumen(): Observable<ResumenQuejas> {
    return this.http.get<ResumenQuejas>(`${this.url}/resumen`);
  }

  responder(id: number, respuesta: string): Observable<QuejaGestion> {
    return this.http.patch<QuejaGestion>(`${this.url}/${id}/responder`, { respuesta });
  }

  enRevision(id: number): Observable<QuejaGestion> {
    return this.http.patch<QuejaGestion>(`${this.url}/${id}/revision`, null);
  }

  exportar(formato: 'csv' | 'xlsx' | 'pdf', tipo: string | null): Observable<Blob> {
    return this.http.get(`${this.url}/exportar`, { params: armar({ formato, tipo }), responseType: 'blob' });
  }
}

@Injectable({ providedIn: 'root' })
export class EntrevistasService {
  private readonly http = inject(HttpClient);

  agenda(): Observable<AgendaEntrevistas> {
    return this.http.get<AgendaEntrevistas>(`${API}/entrevistas/agenda`);
  }

  vacantes(): Observable<Vacante[]> {
    return this.http.get<Vacante[]>(`${API}/entrevistas/vacantes`);
  }

  postulantes(estado?: string | null): Observable<Postulante[]> {
    return this.http.get<Postulante[]>(`${API}/postulantes`, { params: armar({ estado }) });
  }

  registrar(datos: PostulanteRequest): Observable<Postulante> {
    return this.http.post<Postulante>(`${API}/postulantes`, datos);
  }

  agendar(datos: AgendarRequest): Observable<Entrevista> {
    return this.http.post<Entrevista>(`${API}/entrevistas`, datos);
  }

  observaciones(id: number, datos: ObservacionesRequest): Observable<Entrevista> {
    return this.http.put<Entrevista>(`${API}/entrevistas/${id}/observaciones`, datos);
  }

  subirDocumento(idPostulante: number, tipo: TipoDocumento, archivo: File): Observable<unknown> {
    const formulario = new FormData();
    formulario.append('archivo', archivo);
    return this.http.post(`${API}/postulantes/${idPostulante}/documentos`, formulario, { params: { tipo } });
  }

  cv(idPostulante: number): Observable<Blob> {
    return this.http.get(`${API}/postulantes/${idPostulante}/cv`, { responseType: 'blob' });
  }

  resumenEntrevistados(): Observable<ResumenEntrevistados> {
    return this.http.get<ResumenEntrevistados>(`${API}/entrevistados/resumen`);
  }

  contratar(idPostulante: number, datos: ContratarRequest): Observable<Postulante> {
    return this.http.post<Postulante>(`${API}/entrevistados/${idPostulante}/contratar`, datos);
  }

  descartar(idPostulante: number, motivo: string | null): Observable<Postulante> {
    return this.http.post<Postulante>(`${API}/entrevistados/${idPostulante}/descartar`, { motivo });
  }

  informe(formato: 'xlsx' | 'pdf' | 'csv'): Observable<Blob> {
    return this.http.get(`${API}/entrevistados/exportar`, { params: { formato }, responseType: 'blob' });
  }
}

@Injectable({ providedIn: 'root' })
export class PersonalService {
  private readonly http = inject(HttpClient);

  resumen(): Observable<ResumenPersonal> {
    return this.http.get<ResumenPersonal>(`${API}/personal/resumen`);
  }

  listar(q: string | null, turno: string | null, page = 0, size = 10): Observable<Pagina<Empleado>> {
    return this.http.get<Pagina<Empleado>>(`${API}/personal`, { params: armar({ q, turno, page, size }) });
  }

  estaciones(): Observable<Estacion[]> {
    return this.http.get<Estacion[]>(`${API}/personal/estaciones`);
  }

  crearPerfil(datos: CrearPerfilRequest): Observable<PerfilCreado> {
    return this.http.post<PerfilCreado>(`${API}/personal/perfiles`, datos);
  }

  asignar(datos: AsignarEmpleadoRequest): Observable<Empleado> {
    return this.http.post<Empleado>(`${API}/personal/empleados`, datos);
  }

  cambiarRol(id: number, rolCodigo: string): Observable<Empleado> {
    return this.http.put<Empleado>(`${API}/personal/${id}/rol`, { rolCodigo });
  }

  suspender(id: number): Observable<Empleado> {
    return this.http.patch<Empleado>(`${API}/personal/${id}/suspender`, {});
  }

  reactivar(id: number): Observable<Empleado> {
    return this.http.patch<Empleado>(`${API}/personal/${id}/reactivar`, null);
  }

  exportar(formato: 'xlsx' | 'pdf' | 'csv'): Observable<Blob> {
    return this.http.get(`${API}/personal/exportar`, { params: { formato }, responseType: 'blob' });
  }

  roles(): Observable<Rol[]> {
    return this.http.get<Rol[]>(`${API}/roles`);
  }

  permisos(): Observable<PermisoCatalogo[]> {
    return this.http.get<PermisoCatalogo[]>(`${API}/roles/permisos`);
  }

  editarPermisos(idRol: number, permisos: string[]): Observable<Rol> {
    return this.http.put<Rol>(`${API}/roles/${idRol}/permisos`, { permisos });
  }
}

@Injectable({ providedIn: 'root' })
export class BitacoraService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API}/bitacora`;

  listar(f: FiltrosBitacora, page = 0, size = 15): Observable<Pagina<EventoBitacora>> {
    return this.http.get<Pagina<EventoBitacora>>(this.url, { params: armar({ ...f, page, size }) });
  }

  resumen(): Observable<ResumenBitacora> {
    return this.http.get<ResumenBitacora>(`${this.url}/resumen`);
  }

  exportar(formato: 'csv' | 'pdf' | 'xlsx', f: FiltrosBitacora): Observable<Blob> {
    return this.http.get(`${this.url}/exportar`, { params: armar({ ...f, formato }), responseType: 'blob' });
  }
}

@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);

  resumen(mes: string): Observable<ReporteMensual> {
    return this.http.get<ReporteMensual>(`${API}/reportes/resumen`, { params: armar({ mes }) });
  }

  excel(mes: string): Observable<Blob> {
    return this.http.get(`${API}/reportes/exportar/excel`, { params: armar({ mes }), responseType: 'blob' });
  }

  imprimible(mes: string): Observable<Blob> {
    return this.http.get(`${API}/reportes/imprimible`, { params: armar({ mes }), responseType: 'blob' });
  }

  archivos(page = 0, size = 8): Observable<Pagina<ReporteGenerado>> {
    return this.http.get<Pagina<ReporteGenerado>>(`${API}/reportes/archivos`, { params: armar({ page, size }) });
  }

  generar(tipo: string, formato: string): Observable<ReporteGenerado> {
    return this.http.post<ReporteGenerado>(`${API}/reportes/archivos`, null, { params: armar({ tipo, formato }) });
  }

  descargar(id: number): Observable<Blob> {
    return this.http.get(`${API}/reportes/archivos/${id}/descargar`, { responseType: 'blob' });
  }

  respaldos(page = 0, size = 6): Observable<Pagina<Respaldo>> {
    return this.http.get<Pagina<Respaldo>>(`${API}/respaldos`, { params: armar({ page, size }) });
  }

  respaldar(): Observable<Respaldo> {
    return this.http.post<Respaldo>(`${API}/respaldos`, null);
  }

  descargarRespaldo(id: number): Observable<Blob> {
    return this.http.get(`${API}/respaldos/${id}/descargar`, { responseType: 'blob' });
  }
}

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly http = inject(HttpClient);

  subirFoto(archivo: File): Observable<PerfilUsuario> {
    const formulario = new FormData();
    formulario.append('foto', archivo);
    return this.http.post<PerfilUsuario>(`${API}/perfil/foto`, formulario);
  }
}

@Injectable({ providedIn: 'root' })
export class AyudaService {
  private readonly http = inject(HttpClient);

  ayuda(): Observable<AyudaCliente> {
    return this.http.get<AyudaCliente>(`${API}/cliente/ayuda`);
  }
}

@Injectable({ providedIn: 'root' })
export class DisponibilidadService {
  private readonly http = inject(HttpClient);

  dias(): Observable<DiaReserva[]> {
    return this.http.get<DiaReserva[]>(`${API}/reservas/dias`);
  }

  horarios(fecha: string): Observable<HoraReserva[]> {
    return this.http.get<HoraReserva[]>(`${API}/reservas/horarios`, { params: { fecha } });
  }
}
