import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';
import { AgendaEntrevistas, Entrevista, ModalidadEntrevista, Postulante, TipoDocumento, Vacante } from '../../../core/models/gestion.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { EntrevistasService } from '../../../core/services/gestion.service';
import { guardarArchivo } from '../../../core/utils/descargas';
import { aFechaIso, aHoraIso } from '../../../core/utils/fechas';
import { ESTILOS_PANEL, MODALIDADES, infoEstadoPostulante, nombreConExtension } from '../../../shared/panel-comun';

type Dialogo = 'postulante' | 'agendar' | 'observaciones' | 'documento' | null;

@Component({
  selector: 'app-entrevistas',
  imports: [DatePipe, ReactiveFormsModule, ButtonModule, DatePickerModule, DialogModule, InputTextModule, SelectModule, TableModule, TagModule, TextareaModule],
  template: `
    <header class="encabezado">
      <div class="cabecera-pagina">
        <h1>Entrevistas</h1>
        <p>Agenda del equipo de selección y seguimiento de postulantes.</p>
      </div>
      @if (puedeGestionar) {
        <div class="acciones">
          <p-button label="Nuevo postulante" icon="pi pi-user-plus" severity="secondary" (onClick)="abrir('postulante')" />
          <p-button label="Agendar entrevista" icon="pi pi-calendar-plus" (onClick)="abrir('agendar')" />
        </div>
      }
    </header>

    @if (agenda(); as a) {
      <div class="tarjetas">
        <article class="tarjeta"><span>Entrevistas hoy</span><strong>{{ a.resumen.entrevistasHoy }}</strong></article>
        <article class="tarjeta"><span>En evaluación</span><strong>{{ a.resumen.enEvaluacion }}</strong></article>
        <article class="tarjeta"><span>Postulantes activos</span><strong>{{ a.resumen.postulantesActivos }}</strong></article>
        <article class="tarjeta">
          <span>Vacantes abiertas</span>
          <strong>{{ a.resumen.vacantesAbiertas }}</strong>
          @if (a.resumen.puestosCriticos.length > 0) {
            <div class="chips">
              @for (puesto of a.resumen.puestosCriticos; track puesto) {
                <span class="chip critica">{{ puesto }}</span>
              }
            </div>
          }
        </article>
      </div>
    }

    <div class="acciones separado">
      <p-button label="Agenda" [outlined]="vista() !== 'agenda'" (onClick)="vista.set('agenda')" />
      <p-button label="Postulantes" [outlined]="vista() !== 'postulantes'" (onClick)="vista.set('postulantes')" />
    </div>

    @if (vista() === 'agenda') {
      @if (agenda(); as a) {
        <div class="rejilla">
          <section class="bloque">
            <h2>Hoy</h2>
            @if (a.hoy.length === 0) {
              <p class="sub">No hay entrevistas para hoy.</p>
            } @else {
              <ul class="lista">
                @for (e of a.hoy; track e.idEntrevista) {
                  <ng-container *ngTemplateOutlet="tarjetaEntrevista; context: { $implicit: e }" />
                }
              </ul>
            }
          </section>
          <section class="bloque">
            <h2>Próximas</h2>
            @if (a.programadas.length === 0) {
              <p class="sub">No hay entrevistas programadas.</p>
            } @else {
              <ul class="lista">
                @for (e of a.programadas; track e.idEntrevista) {
                  <ng-container *ngTemplateOutlet="tarjetaEntrevista; context: { $implicit: e }" />
                }
              </ul>
            }
          </section>
        </div>
      }
    } @else {
      <p-table [value]="postulantes()" dataKey="idPostulante" [paginator]="true" [rows]="10" [tableStyle]="{ 'min-width': '56rem' }">
        <ng-template #header>
          <tr><th>Postulante</th><th>Puesto</th><th>Estado</th><th>Documentos</th><th style="width: 15rem">Acciones</th></tr>
        </ng-template>
        <ng-template #body let-p>
          <tr>
            <td><strong>{{ p.nombreCompleto }}</strong><div class="sub">{{ p.telefono }}@if (p.correo) { · {{ p.correo }} }</div></td>
            <td>{{ p.cargo }} @if (p.urgencia === 'CRITICA') { <span class="chip critica">Crítico</span> }</td>
            <td><p-tag [value]="info(p.estado).texto" [severity]="info(p.estado).severidad" /></td>
            <td>
              @if (p.documentosCompletos) { Completos } @else { <span class="sub">Faltan: {{ p.documentosFaltantes.join(', ') }}</span> }
            </td>
            <td>
              <div class="acciones">
                @if (puedeGestionar) {
                  <p-button label="Subir archivo" size="small" severity="secondary" (onClick)="abrirDocumento(p)" />
                }
                @if (tieneCv(p)) {
                  <p-button icon="pi pi-download" size="small" [text]="true" ariaLabel="Descargar CV" (onClick)="descargarCv(p.idPostulante, p.nombreCompleto)" />
                }
              </div>
            </td>
          </tr>
        </ng-template>
        <ng-template #emptymessage><tr><td colspan="5" class="vacio">Todavía no hay postulantes.</td></tr></ng-template>
      </p-table>
    }

    <ng-template #tarjetaEntrevista let-e>
      <li>
        <div class="cabecera-item">
          <strong>{{ e.fechaHora | date: 'dd/MM · HH:mm' }} — {{ e.postulante }}</strong>
          <span class="chip" [class.critica]="e.urgencia === 'CRITICA'">{{ e.urgencia === 'CRITICA' ? 'Puesto crítico' : 'Normal' }}</span>
        </div>
        <span class="sub">{{ e.cargo }} · {{ nombreModalidad(e.modalidad) }} · {{ e.telefono }}</span>
        @if (e.estado === 'PROGRAMADA' && e.minutosParaInicio !== null && e.minutosParaInicio > 0 && e.minutosParaInicio < 1440) {
          <span class="sub">Empieza en {{ e.minutosParaInicio }} min</span>
        }
        <span class="sub">{{ e.documentosCompletos ? 'Documentos completos' : 'Faltan documentos' }} · {{ info(e.estadoPostulante).texto }}</span>
        <div class="acciones">
          @if (puedeGestionar && e.estado === 'PROGRAMADA') {
            <p-button label="Observaciones" size="small" (onClick)="abrirObservaciones(e)" />
          }
          @if (e.cvDisponible) {
            <p-button label="Ver CV" icon="pi pi-download" size="small" severity="secondary" [text]="true" (onClick)="descargarCv(e.idPostulante, e.postulante)" />
          }
        </div>
      </li>
    </ng-template>

    <p-dialog header="Nuevo postulante" [visible]="dialogo() === 'postulante'" (visibleChange)="cerrarSi($event)" [modal]="true" [style]="{ width: '480px' }">
      <form class="formulario" [formGroup]="formPostulante" novalidate>
        <div class="campo"><label for="p-nombre">Nombre completo</label><input pInputText id="p-nombre" formControlName="nombreCompleto" /></div>
        <div class="fila">
          <div class="campo"><label for="p-tel">Teléfono</label><input pInputText id="p-tel" formControlName="telefono" /></div>
          <div class="campo"><label for="p-correo">Correo (opcional)</label><input pInputText id="p-correo" type="email" formControlName="correo" /></div>
        </div>
        <div class="campo">
          <label for="p-vacante">Puesto</label>
          <p-select inputId="p-vacante" [options]="opcionesVacante()" optionLabel="etiqueta" optionValue="valor" formControlName="idVacante" appendTo="body" placeholder="Elige una vacante" />
        </div>
      </form>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="dialogo.set(null)" />
        <p-button label="Registrar" [loading]="guardando()" (onClick)="guardarPostulante()" />
      </ng-template>
    </p-dialog>

    <p-dialog header="Agendar entrevista" [visible]="dialogo() === 'agendar'" (visibleChange)="cerrarSi($event)" [modal]="true" [style]="{ width: '480px' }">
      <form class="formulario" [formGroup]="formAgenda" novalidate>
        <div class="campo">
          <label for="a-post">Postulante</label>
          <p-select inputId="a-post" [options]="opcionesPostulante()" optionLabel="etiqueta" optionValue="valor" formControlName="idPostulante" appendTo="body" placeholder="Postulantes sin entrevista" />
        </div>
        <div class="campo">
          <label for="a-fecha">Fecha y hora</label>
          <p-datepicker inputId="a-fecha" formControlName="fechaHora" [showTime]="true" hourFormat="24" [minDate]="ahora" dateFormat="dd/mm/yy" appendTo="body" />
        </div>
        <div class="campo">
          <label for="a-mod">Modalidad</label>
          <p-select inputId="a-mod" [options]="modalidades" optionLabel="etiqueta" optionValue="valor" formControlName="modalidad" appendTo="body" />
        </div>
      </form>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="dialogo.set(null)" />
        <p-button label="Agendar" [loading]="guardando()" (onClick)="guardarAgenda()" />
      </ng-template>
    </p-dialog>

    <p-dialog [header]="'Observaciones · ' + (entrevistaActual()?.postulante ?? '')" [visible]="dialogo() === 'observaciones'" (visibleChange)="cerrarSi($event)" [modal]="true" [style]="{ width: '520px' }">
      <form class="formulario" [formGroup]="formObs" novalidate>
        <div class="campo"><label for="o-obs">Observaciones de la entrevista</label><textarea pTextarea id="o-obs" rows="3" formControlName="observaciones"></textarea></div>
        <div class="campo"><label for="o-eval">Evaluación del gerente</label><textarea pTextarea id="o-eval" rows="3" formControlName="evaluacionGerente"></textarea></div>
        <label><input type="checkbox" formControlName="realizada" /> Marcar la entrevista como realizada</label>
        <label><input type="checkbox" formControlName="aprobar" /> Dar mi visto bueno (pasa a revisión del administrador)</label>
      </form>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="dialogo.set(null)" />
        <p-button label="Guardar" [loading]="guardando()" (onClick)="guardarObservaciones()" />
      </ng-template>
    </p-dialog>

    <p-dialog [header]="'Subir archivo · ' + (postulanteActual()?.nombreCompleto ?? '')" [visible]="dialogo() === 'documento'" (visibleChange)="cerrarSi($event)" [modal]="true" [style]="{ width: '460px' }">
      <form class="formulario" [formGroup]="formDoc" novalidate>
        <div class="campo">
          <label for="d-tipo">Tipo de documento</label>
          <p-select inputId="d-tipo" [options]="tiposDocumento" optionLabel="etiqueta" optionValue="valor" formControlName="tipo" appendTo="body" />
        </div>
        <div class="campo">
          <label for="d-archivo">Archivo</label>
          <input id="d-archivo" type="file" accept=".pdf,.png,.jpg,.jpeg,.doc,.docx" (change)="elegirArchivo($event)" />
        </div>
      </form>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="dialogo.set(null)" />
        <p-button label="Subir" [loading]="guardando()" [disabled]="!archivo()" (onClick)="subirDocumento()" />
      </ng-template>
    </p-dialog>
  `,
  styles: [ESTILOS_PANEL],
})
export class Entrevistas implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(EntrevistasService);
  private readonly aviso = inject(AvisoService);
  protected readonly puedeGestionar = inject(AuthService).tienePermiso('ENTREVISTA_GESTIONAR');

  protected readonly modalidades = MODALIDADES;
  protected readonly tiposDocumento = [
    { etiqueta: 'Currículum (CV)', valor: 'CV' },
    { etiqueta: 'Carnet de identidad', valor: 'CARNET' },
    { etiqueta: 'Antecedentes', valor: 'ANTECEDENTES' },
    { etiqueta: 'Otro', valor: 'OTRO' },
  ];
  protected readonly info = infoEstadoPostulante;
  protected readonly ahora = new Date();

  protected readonly agenda = signal<AgendaEntrevistas | null>(null);
  protected readonly postulantes = signal<Postulante[]>([]);
  protected readonly vacantes = signal<Vacante[]>([]);
  protected readonly vista = signal<'agenda' | 'postulantes'>('agenda');
  protected readonly dialogo = signal<Dialogo>(null);
  protected readonly guardando = signal(false);
  protected readonly entrevistaActual = signal<Entrevista | null>(null);
  protected readonly postulanteActual = signal<Postulante | null>(null);
  protected readonly archivo = signal<File | null>(null);

  protected readonly opcionesVacante = computed(() =>
    this.vacantes().map((v) => ({ etiqueta: `${v.cargo} · ${v.estacion}`, valor: v.idVacante })),
  );
  protected readonly opcionesPostulante = computed(() =>
    this.postulantes()
      .filter((p) => p.estado === 'POSTULADO')
      .map((p) => ({ etiqueta: `${p.nombreCompleto} · ${p.cargo}`, valor: p.idPostulante })),
  );

  protected readonly formPostulante = this.fb.group({
    nombreCompleto: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(255)]),
    telefono: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(30)]),
    correo: this.fb.nonNullable.control('', Validators.email),
    idVacante: this.fb.control<number | null>(null, Validators.required),
  });
  protected readonly formAgenda = this.fb.group({
    idPostulante: this.fb.control<number | null>(null, Validators.required),
    fechaHora: this.fb.control<Date | null>(null, Validators.required),
    modalidad: this.fb.nonNullable.control<ModalidadEntrevista>('PRESENCIAL', Validators.required),
  });
  protected readonly formObs = this.fb.group({
    observaciones: this.fb.nonNullable.control('', Validators.maxLength(1000)),
    evaluacionGerente: this.fb.nonNullable.control('', Validators.maxLength(1000)),
    realizada: this.fb.nonNullable.control(false),
    aprobar: this.fb.nonNullable.control(false),
  });
  protected readonly formDoc = this.fb.group({
    tipo: this.fb.nonNullable.control<TipoDocumento>('CV'),
  });

  ngOnInit(): void {
    this.refrescar();
    this.api.vacantes().subscribe({ next: (v) => this.vacantes.set(v), error: () => undefined });
  }

  private refrescar(): void {
    this.api.agenda().subscribe({ next: (a) => this.agenda.set(a), error: (e) => this.aviso.error(e, 'No pudimos cargar la agenda') });
    this.api.postulantes().subscribe({ next: (p) => this.postulantes.set(p), error: () => undefined });
  }

  protected abrir(dialogo: 'postulante' | 'agendar'): void {
    if (dialogo === 'postulante') this.formPostulante.reset();
    else this.formAgenda.reset({ modalidad: 'PRESENCIAL' });
    this.dialogo.set(dialogo);
  }

  protected cerrarSi(visible: boolean): void {
    if (!visible) this.dialogo.set(null);
  }

  protected abrirObservaciones(entrevista: Entrevista): void {
    this.entrevistaActual.set(entrevista);
    this.formObs.reset({ observaciones: entrevista.observaciones ?? '', evaluacionGerente: '', realizada: false, aprobar: false });
    this.dialogo.set('observaciones');
  }

  protected abrirDocumento(postulante: Postulante): void {
    this.postulanteActual.set(postulante);
    this.archivo.set(null);
    this.formDoc.reset({ tipo: 'CV' });
    this.dialogo.set('documento');
  }

  protected elegirArchivo(evento: Event): void {
    this.archivo.set((evento.target as HTMLInputElement).files?.item(0) ?? null);
  }

  private terminar(mensaje: string): void {
    this.aviso.exito(mensaje);
    this.dialogo.set(null);
    this.guardando.set(false);
    this.refrescar();
  }

  private fallar(e: unknown): void {
    this.aviso.error(e);
    this.guardando.set(false);
  }

  protected guardarPostulante(): void {
    if (this.formPostulante.invalid) {
      this.formPostulante.markAllAsTouched();
      return;
    }
    const v = this.formPostulante.getRawValue();
    this.guardando.set(true);
    this.api
      .registrar({ nombreCompleto: v.nombreCompleto.trim(), telefono: v.telefono.trim(), correo: v.correo.trim() || null, idVacante: v.idVacante as number })
      .subscribe({ next: () => this.terminar('Postulante registrado.'), error: (e) => this.fallar(e) });
  }

  protected guardarAgenda(): void {
    if (this.formAgenda.invalid) {
      this.formAgenda.markAllAsTouched();
      return;
    }
    const v = this.formAgenda.getRawValue();
    const fecha = v.fechaHora as Date;
    this.guardando.set(true);
    this.api
      .agendar({ idPostulante: v.idPostulante as number, fechaHora: `${aFechaIso(fecha)}T${aHoraIso(fecha)}:00`, modalidad: v.modalidad })
      .subscribe({ next: () => this.terminar('Entrevista agendada.'), error: (e) => this.fallar(e) });
  }

  protected guardarObservaciones(): void {
    const entrevista = this.entrevistaActual();
    if (!entrevista || this.formObs.invalid) return;
    const v = this.formObs.getRawValue();
    this.guardando.set(true);
    this.api
      .observaciones(entrevista.idEntrevista, {
        observaciones: v.observaciones.trim() || null,
        evaluacionGerente: v.evaluacionGerente.trim() || null,
        realizada: v.realizada,
        aprobar: v.aprobar,
      })
      .subscribe({ next: () => this.terminar('Observaciones guardadas.'), error: (e) => this.fallar(e) });
  }

  protected subirDocumento(): void {
    const postulante = this.postulanteActual();
    const archivo = this.archivo();
    if (!postulante || !archivo) return;
    this.guardando.set(true);
    this.api
      .subirDocumento(postulante.idPostulante, this.formDoc.getRawValue().tipo, archivo)
      .subscribe({ next: () => this.terminar('Archivo subido.'), error: (e) => this.fallar(e) });
  }

  protected descargarCv(idPostulante: number, nombre: string): void {
    this.api.cv(idPostulante).subscribe({
      next: (blob) => guardarArchivo(blob, nombreConExtension(`cv-${nombre.replace(/\s+/g, '-').toLowerCase()}`, blob)),
      error: (e) => this.aviso.error(e, 'No se pudo descargar el CV'),
    });
  }

  protected tieneCv(postulante: Postulante): boolean {
    return postulante.documentos.some((d) => d.tipo === 'CV');
  }

  protected nombreModalidad(modalidad: string): string {
    return this.modalidades.find((m) => m.valor === modalidad)?.etiqueta ?? modalidad;
  }
}
