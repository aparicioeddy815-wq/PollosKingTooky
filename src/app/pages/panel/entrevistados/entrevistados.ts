import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';
import { Estacion, Postulante, ResumenEntrevistados, Turno } from '../../../core/models/gestion.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { EntrevistasService, PersonalService } from '../../../core/services/gestion.service';
import { guardarArchivo } from '../../../core/utils/descargas';
import { Grafica, SerieGrafica } from '../../../shared/components/grafica/grafica';
import { ESTILOS_PANEL, TURNOS, infoEstadoPostulante, nombreConExtension } from '../../../shared/panel-comun';

@Component({
  selector: 'app-entrevistados',
  imports: [ReactiveFormsModule, ButtonModule, DialogModule, InputNumberModule, SelectModule, TableModule, TagModule, TextareaModule, Grafica],
  template: `
    <header class="encabezado">
      <div class="cabecera-pagina">
        <h1>Entrevistados</h1>
        <p>Decide quién entra al equipo: contrata o descarta a los postulantes con visto bueno.</p>
      </div>
      <div class="acciones">
        <p-button label="Informe Excel" icon="pi pi-file-excel" severity="secondary" [text]="true" (onClick)="informe()" />
      </div>
    </header>

    @if (resumen(); as r) {
      <div class="tarjetas">
        <article class="tarjeta"><span>Postulantes del ciclo</span><strong>{{ r.postulantesDelCiclo }}</strong></article>
        <article class="tarjeta"><span>Visto bueno gerencial</span><strong>{{ r.vistoBuenoGerencial }}</strong></article>
        <article class="tarjeta" [class.alerta]="r.urgenciaOperativa > 0"><span>Urgencia operativa</span><strong>{{ r.urgenciaOperativa }}</strong><small>puestos críticos abiertos</small></article>
        <article class="tarjeta"><span>Contratados del mes</span><strong>{{ r.contratadosDelMes }}</strong></article>
      </div>
    }

    <section class="bloque separado">
      <h2>Postulantes por etapa</h2>
      <app-grafica tipo="barras" titulo="Postulantes por etapa" [etiquetas]="etiquetasEtapa()" [series]="seriesEtapa()" [alto]="220" />
    </section>

    <div class="filtros">
      <p-select [options]="opcionesEstado" optionLabel="etiqueta" optionValue="valor" [formControl]="filtroEstado" [showClear]="true" placeholder="Todas las etapas" />
    </div>

    <p-table [value]="filtrados()" dataKey="idPostulante" [paginator]="true" [rows]="10" [tableStyle]="{ 'min-width': '58rem' }">
      <ng-template #header>
        <tr><th>Postulante</th><th>Puesto</th><th>Etapa</th><th>Evaluación del gerente</th><th style="width: 16rem">Acciones</th></tr>
      </ng-template>
      <ng-template #body let-p>
        <tr>
          <td><strong>{{ p.nombreCompleto }}</strong><div class="sub">{{ p.telefono }}</div></td>
          <td>{{ p.cargo }} @if (p.urgencia === 'CRITICA') { <span class="chip critica">Crítico</span> }</td>
          <td><p-tag [value]="info(p.estado).texto" [severity]="info(p.estado).severidad" /></td>
          <td class="sub">{{ p.evaluacionGerente ?? '—' }}</td>
          <td>
            <div class="acciones">
              @if (puedeDecidir(p)) {
                <p-button label="Contratar" size="small" (onClick)="abrirContratar(p)" />
                <p-button label="Descartar" size="small" severity="danger" [text]="true" (onClick)="abrirDescartar(p)" />
              }
              @if (tieneCv(p)) {
                <p-button icon="pi pi-download" size="small" severity="secondary" [text]="true" ariaLabel="Descargar CV" (onClick)="descargarCv(p)" />
              }
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template #emptymessage><tr><td colspan="5" class="vacio">No hay postulantes en esta etapa.</td></tr></ng-template>
    </p-table>

    <p-dialog [header]="'Contratar a ' + (actual()?.nombreCompleto ?? '')" [visible]="dialogo() === 'contratar'" (visibleChange)="cerrarSi($event)" [modal]="true" [style]="{ width: '460px' }">
      <form class="formulario" [formGroup]="formContrato" novalidate>
        <div class="fila">
          <div class="campo"><label for="c-turno">Turno</label><p-select inputId="c-turno" [options]="turnos" optionLabel="etiqueta" optionValue="valor" formControlName="turno" appendTo="body" /></div>
          <div class="campo"><label for="c-salario">Salario (Bs)</label><p-inputnumber inputId="c-salario" formControlName="salario" mode="decimal" [minFractionDigits]="0" [maxFractionDigits]="2" [min]="0" /></div>
        </div>
        <div class="campo"><label for="c-est">Estación</label><p-select inputId="c-est" [options]="estaciones()" optionLabel="nombre" optionValue="idEstacion" formControlName="idEstacion" [showClear]="true" appendTo="body" placeholder="Sin estación" /></div>
      </form>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="dialogo.set(null)" />
        <p-button label="Confirmar contratación" [loading]="guardando()" (onClick)="contratar()" />
      </ng-template>
    </p-dialog>

    <p-dialog [header]="'Descartar a ' + (actual()?.nombreCompleto ?? '')" [visible]="dialogo() === 'descartar'" (visibleChange)="cerrarSi($event)" [modal]="true" [style]="{ width: '460px' }">
      <div class="campo">
        <label for="motivo">Motivo (opcional)</label>
        <textarea pTextarea id="motivo" rows="3" [formControl]="motivo"></textarea>
      </div>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="dialogo.set(null)" />
        <p-button label="Descartar" severity="danger" [loading]="guardando()" (onClick)="descartar()" />
      </ng-template>
    </p-dialog>
  `,
  styles: [ESTILOS_PANEL],
})
export class Entrevistados implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(EntrevistasService);
  private readonly personal = inject(PersonalService);
  private readonly aviso = inject(AvisoService);

  protected readonly turnos = TURNOS;
  protected readonly info = infoEstadoPostulante;
  protected readonly opcionesEstado = [
    { etiqueta: 'Postulado', valor: 'POSTULADO' },
    { etiqueta: 'Entrevista agendada', valor: 'ENTREVISTA_AGENDADA' },
    { etiqueta: 'En evaluación', valor: 'EN_EVALUACION' },
    { etiqueta: 'Visto bueno gerencial', valor: 'APROBADO_GERENTE' },
    { etiqueta: 'Revisión de antecedentes', valor: 'EN_REVISION_ANTECEDENTES' },
    { etiqueta: 'Contratado', valor: 'CONTRATADO' },
    { etiqueta: 'Descartado', valor: 'DESCARTADO' },
  ];

  protected readonly resumen = signal<ResumenEntrevistados | null>(null);
  protected readonly postulantes = signal<Postulante[]>([]);
  protected readonly estaciones = signal<Estacion[]>([]);
  protected readonly dialogo = signal<'contratar' | 'descartar' | null>(null);
  protected readonly actual = signal<Postulante | null>(null);
  protected readonly guardando = signal(false);
  protected readonly filtroEstado = new FormControl<string | null>(null);
  protected readonly motivo = new FormControl('', { nonNullable: true });
  private readonly estadoSeleccionado = signal<string | null>(null);

  protected readonly filtrados = computed(() => {
    const estado = this.estadoSeleccionado();
    return estado ? this.postulantes().filter((p) => p.estado === estado) : this.postulantes();
  });

  protected readonly etiquetasEtapa = computed(() => this.opcionesEstado.map((o) => o.etiqueta));
  protected readonly seriesEtapa = computed<SerieGrafica[]>(() => [
    { nombre: 'Postulantes', valores: this.opcionesEstado.map((o) => this.postulantes().filter((p) => p.estado === o.valor).length) },
  ]);

  protected readonly formContrato = this.fb.group({
    turno: this.fb.nonNullable.control<Turno>('MANANA', Validators.required),
    salario: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    idEstacion: this.fb.control<number | null>(null),
  });

  constructor() {
    this.filtroEstado.valueChanges.subscribe((valor) => this.estadoSeleccionado.set(valor));
  }

  ngOnInit(): void {
    this.refrescar();
    this.personal.estaciones().subscribe({ next: (e) => this.estaciones.set(e), error: () => undefined });
  }

  private refrescar(): void {
    this.api.resumenEntrevistados().subscribe({ next: (r) => this.resumen.set(r), error: () => undefined });
    this.api.postulantes().subscribe({ next: (p) => this.postulantes.set(p), error: (e) => this.aviso.error(e, 'No pudimos cargar los postulantes') });
  }

  protected puedeDecidir(p: Postulante): boolean {
    return p.estado !== 'CONTRATADO' && p.estado !== 'DESCARTADO';
  }

  protected tieneCv(p: Postulante): boolean {
    return p.documentos.some((d) => d.tipo === 'CV');
  }

  protected cerrarSi(visible: boolean): void {
    if (!visible) this.dialogo.set(null);
  }

  protected abrirContratar(p: Postulante): void {
    this.actual.set(p);
    this.formContrato.reset({ turno: 'MANANA' });
    this.dialogo.set('contratar');
  }

  protected abrirDescartar(p: Postulante): void {
    this.actual.set(p);
    this.motivo.reset('');
    this.dialogo.set('descartar');
  }

  protected contratar(): void {
    const p = this.actual();
    if (!p) return;
    if (this.formContrato.invalid) {
      this.formContrato.markAllAsTouched();
      return;
    }
    const v = this.formContrato.getRawValue();
    this.guardando.set(true);
    this.api.contratar(p.idPostulante, { turno: v.turno, salario: v.salario as number, idEstacion: v.idEstacion }).subscribe({
      next: () => this.terminar(`${p.nombreCompleto} fue contratado.`),
      error: (e) => this.fallar(e),
    });
  }

  protected descartar(): void {
    const p = this.actual();
    if (!p) return;
    this.guardando.set(true);
    this.api.descartar(p.idPostulante, this.motivo.value.trim() || null).subscribe({
      next: () => this.terminar('Postulante descartado.'),
      error: (e) => this.fallar(e),
    });
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

  protected descargarCv(p: Postulante): void {
    this.api.cv(p.idPostulante).subscribe({
      next: (blob) => guardarArchivo(blob, nombreConExtension(`cv-${p.nombreCompleto.replace(/\s+/g, '-').toLowerCase()}`, blob)),
      error: (e) => this.aviso.error(e, 'No se pudo descargar el CV'),
    });
  }

  protected informe(): void {
    this.api.informe('xlsx').subscribe({
      next: (blob) => guardarArchivo(blob, 'informe-de-seleccion.xlsx'),
      error: (e) => this.aviso.error(e, 'No se pudo descargar'),
    });
  }
}
