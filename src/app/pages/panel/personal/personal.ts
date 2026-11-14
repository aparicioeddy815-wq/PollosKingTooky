import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmationService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { Empleado, Estacion, PermisoCatalogo, ResumenPersonal, Rol, Turno } from '../../../core/models/gestion.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { PersonalService } from '../../../core/services/gestion.service';
import { guardarArchivo } from '../../../core/utils/descargas';
import { Grafica, SerieGrafica } from '../../../shared/components/grafica/grafica';
import { ESTILOS_PANEL, TURNOS, textoTurno } from '../../../shared/panel-comun';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

type Dialogo = 'perfil' | 'empleado' | 'rol' | 'permisos' | null;

@Component({
  selector: 'app-personal',
  imports: [DatePipe, DecimalPipe, FormsModule, ReactiveFormsModule, ButtonModule, DialogModule, InputNumberModule, InputTextModule, SelectModule, TableModule, TagModule, Grafica, BolivianosPipe],
  template: `
    <header class="encabezado">
      <div class="cabecera-pagina">
        <h1>Roles y personal</h1>
        <p>Equipo del local, puestos por estación y permisos de cada rol.</p>
      </div>
      <div class="acciones">
        <p-button label="Excel" icon="pi pi-file-excel" severity="secondary" [text]="true" (onClick)="exportar()" />
        @if (puedeGestionar) {
          <p-button label="Asignar empleado" icon="pi pi-plus" severity="secondary" (onClick)="abrirEmpleado()" />
          <p-button label="Crear perfil" icon="pi pi-user-plus" (onClick)="abrirPerfil()" />
        }
      </div>
    </header>

    @if (resumen(); as r) {
      <div class="tarjetas">
        <article class="tarjeta"><span>Empleados</span><strong>{{ r.totalEmpleados }}</strong><small>{{ r.empleadosActivos }} activos</small></article>
        <article class="tarjeta" [class.alerta]="r.suspendidos > 0"><span>Suspendidos</span><strong>{{ r.suspendidos }}</strong></article>
        <article class="tarjeta"><span>Perfiles creados</span><strong>{{ r.perfilesCreados }}</strong></article>
        <article class="tarjeta"><span>Turno actual</span><strong>{{ turno(r.turnoActual) }}</strong><small>{{ r.activosEnTurnoActual }} activos</small></article>
        <article class="tarjeta"><span>Ocupación de puestos</span><strong>{{ r.ocupacionPorcentaje | number: '1.0-0' }}%</strong><small>{{ r.puestosOcupados }} de {{ r.totalPuestos }}</small></article>
      </div>
    }

    <div class="rejilla">
      <section class="bloque">
        <h2>Ocupación por estación</h2>
        <app-grafica tipo="barras" titulo="Puestos ocupados por estación" [etiquetas]="etiquetasEstacion()" [series]="seriesEstacion()" />
      </section>
      <section class="bloque">
        <h2>Roles del sistema</h2>
        <ul class="lista">
          @for (rol of roles(); track rol.idRol) {
            <li>
              <div class="cabecera-item">
                <strong>{{ rol.nombre }}</strong>
                <span class="chip">{{ rol.usuariosActivos }} usuarios activos</span>
              </div>
              <span class="sub">{{ rol.descripcion }}</span>
              <div class="acciones">
                <p-button [label]="puedeEditarRoles ? 'Editar permisos (' + rol.permisos.length + ')' : 'Ver permisos (' + rol.permisos.length + ')'" size="small" severity="secondary" (onClick)="abrirPermisos(rol)" />
              </div>
            </li>
          }
        </ul>
      </section>
    </div>

    <div class="filtros">
      <input pInputText type="search" placeholder="Buscar empleado" aria-label="Buscar empleado" [ngModel]="busqueda()" (ngModelChange)="cambiarBusqueda($event)" />
      <p-select [options]="turnos" optionLabel="etiqueta" optionValue="valor" [ngModel]="turnoFiltro()" (ngModelChange)="cambiarTurno($event)" [showClear]="true" placeholder="Todos los turnos" />
    </div>

    <p-table [value]="empleados()" dataKey="idEmpleado" [loading]="cargando()" [tableStyle]="{ 'min-width': '62rem' }">
      <ng-template #header>
        <tr><th>Empleado</th><th>Cargo</th><th>Turno</th><th>Rol</th><th>Estación</th><th>Salario</th><th>Estado</th>@if (puedeGestionar) { <th style="width: 13rem">Acciones</th> }</tr>
      </ng-template>
      <ng-template #body let-e>
        <tr>
          <td><strong>{{ e.nombreCompleto }}</strong><div class="sub">{{ e.codigo }} · desde {{ e.fechaIngreso | date: 'dd/MM/yyyy' }}</div></td>
          <td>{{ e.cargo }}</td>
          <td>{{ turno(e.turno) }}</td>
          <td>{{ e.rol ?? 'Sin perfil' }}</td>
          <td>{{ e.estacion ?? '—' }}</td>
          <td>{{ e.salario | bs }}</td>
          <td><p-tag [value]="e.estado === 'ACTIVO' ? 'Activo' : 'Suspendido'" [severity]="e.estado === 'ACTIVO' ? 'success' : 'danger'" /></td>
          @if (puedeGestionar) {
            <td>
              <div class="acciones">
                @if (e.idUsuario) { <p-button label="Rol" size="small" severity="secondary" (onClick)="abrirRol(e)" /> }
                @if (e.estado === 'ACTIVO') {
                  <p-button label="Suspender" size="small" severity="danger" [text]="true" (onClick)="suspender(e)" />
                } @else {
                  <p-button label="Reactivar" size="small" [text]="true" (onClick)="reactivar(e)" />
                }
              </div>
            </td>
          }
        </tr>
      </ng-template>
      <ng-template #emptymessage><tr><td colspan="8" class="vacio">No hay empleados con esos filtros.</td></tr></ng-template>
    </p-table>

    <div class="paginador">
      <span>Página {{ pagina() + 1 }} de {{ paginas() }}</span>
      <p-button icon="pi pi-chevron-left" [text]="true" [disabled]="pagina() === 0" ariaLabel="Anterior" (onClick)="irA(pagina() - 1)" />
      <p-button icon="pi pi-chevron-right" [text]="true" [disabled]="pagina() + 1 >= paginas()" ariaLabel="Siguiente" (onClick)="irA(pagina() + 1)" />
    </div>

    <p-dialog header="Crear perfil de acceso" [visible]="dialogo() === 'perfil'" (visibleChange)="cerrarSi($event)" [modal]="true" [style]="{ width: '500px' }">
      <form class="formulario" [formGroup]="formPerfil" novalidate>
        <div class="campo"><label for="pf-nombre">Nombre completo</label><input pInputText id="pf-nombre" formControlName="nombreCompleto" /></div>
        <div class="fila">
          <div class="campo">
            <label for="pf-usuario">Usuario</label>
            <input pInputText id="pf-usuario" formControlName="nombreUsuario" />
            @if (formPerfil.controls.nombreUsuario.touched && formPerfil.controls.nombreUsuario.invalid) { <small class="error">3 a 50 caracteres: letras, números, punto, guion.</small> }
          </div>
          <div class="campo"><label for="pf-correo">Correo</label><input pInputText id="pf-correo" type="email" formControlName="correo" /></div>
        </div>
        <div class="fila">
          <div class="campo"><label for="pf-rol">Rol</label><p-select inputId="pf-rol" [options]="roles()" optionLabel="nombre" optionValue="codigo" formControlName="rolCodigo" appendTo="body" placeholder="Elige un rol" /></div>
          <div class="campo"><label for="pf-clave">Contraseña (opcional)</label><input pInputText id="pf-clave" type="text" formControlName="contrasena" placeholder="Se genera una temporal" /></div>
        </div>
      </form>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="dialogo.set(null)" />
        <p-button label="Crear perfil" [loading]="guardando()" (onClick)="crearPerfil()" />
      </ng-template>
    </p-dialog>

    <p-dialog header="Asignar empleado a una estación" [visible]="dialogo() === 'empleado'" (visibleChange)="cerrarSi($event)" [modal]="true" [style]="{ width: '500px' }">
      <form class="formulario" [formGroup]="formEmpleado" novalidate>
        <div class="campo"><label for="em-nombre">Nombre completo</label><input pInputText id="em-nombre" formControlName="nombreCompleto" /></div>
        <div class="fila">
          <div class="campo"><label for="em-cargo">Cargo</label><input pInputText id="em-cargo" formControlName="cargo" /></div>
          <div class="campo"><label for="em-tel">Teléfono</label><input pInputText id="em-tel" formControlName="telefono" /></div>
        </div>
        <div class="fila">
          <div class="campo"><label for="em-turno">Turno</label><p-select inputId="em-turno" [options]="turnos" optionLabel="etiqueta" optionValue="valor" formControlName="turno" appendTo="body" /></div>
          <div class="campo"><label for="em-salario">Salario (Bs)</label><p-inputnumber inputId="em-salario" formControlName="salario" mode="decimal" [minFractionDigits]="0" [maxFractionDigits]="2" [min]="0" /></div>
        </div>
        <div class="campo"><label for="em-est">Estación</label><p-select inputId="em-est" [options]="estaciones()" optionLabel="nombre" optionValue="idEstacion" formControlName="idEstacion" [showClear]="true" appendTo="body" placeholder="Sin estación" /></div>
      </form>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="dialogo.set(null)" />
        <p-button label="Guardar" [loading]="guardando()" (onClick)="asignarEmpleado()" />
      </ng-template>
    </p-dialog>

    <p-dialog [header]="'Cambiar rol · ' + (empleadoActual()?.nombreCompleto ?? '')" [visible]="dialogo() === 'rol'" (visibleChange)="cerrarSi($event)" [modal]="true" [style]="{ width: '420px' }">
      <div class="campo">
        <label for="nuevo-rol">Nuevo rol</label>
        <p-select inputId="nuevo-rol" [options]="roles()" optionLabel="nombre" optionValue="codigo" [formControl]="nuevoRol" appendTo="body" placeholder="Elige un rol" />
      </div>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="dialogo.set(null)" />
        <p-button label="Cambiar rol" [loading]="guardando()" [disabled]="!nuevoRol.value" (onClick)="cambiarRol()" />
      </ng-template>
    </p-dialog>

    <p-dialog [header]="'Permisos · ' + (rolActual()?.nombre ?? '')" [visible]="dialogo() === 'permisos'" (visibleChange)="cerrarSi($event)" [modal]="true" [style]="{ width: '560px' }">
      <div class="permisos">
        @for (p of catalogo(); track p.codigo) {
          <label class="permiso">
            <input type="checkbox" [checked]="marcados().has(p.codigo)" [disabled]="!puedeEditarRoles" (change)="alternarPermiso(p.codigo)" />
            <span><strong>{{ p.codigo }}</strong><small>{{ p.descripcion }}</small></span>
          </label>
        }
      </div>
      <ng-template #footer>
        <p-button [label]="puedeEditarRoles ? 'Cancelar' : 'Cerrar'" [text]="true" (onClick)="dialogo.set(null)" />
        @if (puedeEditarRoles) {
          <p-button label="Guardar permisos" [loading]="guardando()" (onClick)="guardarPermisos()" />
        }
      </ng-template>
    </p-dialog>
  `,
  styles: [
    ESTILOS_PANEL,
    `
      .permisos { display: grid; gap: 0.6rem; max-height: 55vh; overflow: auto; }
      .permiso { display: flex; align-items: flex-start; gap: 0.6rem; padding: 0.5rem 0.7rem; border-radius: var(--kt-radio-chico); background: var(--kt-fondo); }
      .permiso span { display: grid; }
      .permiso small { color: var(--kt-texto-tenue); }
    `,
  ],
})
export class RolesPersonal implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(PersonalService);
  private readonly aviso = inject(AvisoService);
  private readonly confirmar = inject(ConfirmationService);
  private readonly auth = inject(AuthService);

  protected readonly puedeGestionar = this.auth.tienePermiso('PERSONAL_GESTIONAR');
  protected readonly puedeEditarRoles = this.auth.tienePermiso('ROL_EDITAR');
  protected readonly turnos = TURNOS;
  protected readonly turno = textoTurno;

  protected readonly resumen = signal<ResumenPersonal | null>(null);
  protected readonly empleados = signal<Empleado[]>([]);
  protected readonly estaciones = signal<Estacion[]>([]);
  protected readonly roles = signal<Rol[]>([]);
  protected readonly catalogo = signal<PermisoCatalogo[]>([]);
  protected readonly pagina = signal(0);
  protected readonly paginas = signal(1);
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly dialogo = signal<Dialogo>(null);
  protected readonly busqueda = signal('');
  protected readonly turnoFiltro = signal<string | null>(null);
  protected readonly empleadoActual = signal<Empleado | null>(null);
  protected readonly rolActual = signal<Rol | null>(null);
  protected readonly marcados = signal<Set<string>>(new Set());
  protected readonly nuevoRol = new FormControl<string | null>(null);

  protected readonly etiquetasEstacion = computed(() => this.estaciones().map((e) => e.nombre));
  protected readonly seriesEstacion = computed<SerieGrafica[]>(() => [
    { nombre: 'Ocupados', valores: this.estaciones().map((e) => e.ocupados) },
    { nombre: 'Puestos totales', valores: this.estaciones().map((e) => e.puestosTotales) },
  ]);

  protected readonly formPerfil = this.fb.group({
    nombreCompleto: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(255)]),
    nombreUsuario: this.fb.nonNullable.control('', [Validators.required, Validators.pattern(/^[A-Za-z0-9._-]{3,50}$/)]),
    correo: this.fb.nonNullable.control('', [Validators.required, Validators.email]),
    rolCodigo: this.fb.control<string | null>(null, Validators.required),
    contrasena: this.fb.nonNullable.control('', Validators.minLength(6)),
  });

  protected readonly formEmpleado = this.fb.group({
    nombreCompleto: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(255)]),
    cargo: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(60)]),
    telefono: this.fb.nonNullable.control('', Validators.maxLength(30)),
    turno: this.fb.nonNullable.control<Turno>('MANANA', Validators.required),
    salario: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    idEstacion: this.fb.control<number | null>(null),
  });

  ngOnInit(): void {
    this.cargar();
    this.refrescarGeneral();
    this.api.roles().subscribe({ next: (r) => this.roles.set(r), error: () => undefined });
    this.api.permisos().subscribe({ next: (p) => this.catalogo.set(p), error: () => undefined });
  }

  private refrescarGeneral(): void {
    this.api.resumen().subscribe({ next: (r) => this.resumen.set(r), error: () => undefined });
    this.api.estaciones().subscribe({ next: (e) => this.estaciones.set(e), error: () => undefined });
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.api.listar(this.busqueda().trim() || null, this.turnoFiltro(), this.pagina()).subscribe({
      next: (p) => {
        this.empleados.set(p.contenido);
        this.paginas.set(Math.max(1, p.totalPaginas));
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar el personal');
        this.cargando.set(false);
      },
    });
  }

  protected cambiarBusqueda(texto: string): void {
    this.busqueda.set(texto);
    this.pagina.set(0);
    this.cargar();
  }

  protected cambiarTurno(turno: string | null): void {
    this.turnoFiltro.set(turno);
    this.pagina.set(0);
    this.cargar();
  }

  protected irA(pagina: number): void {
    this.pagina.set(pagina);
    this.cargar();
  }

  protected cerrarSi(visible: boolean): void {
    if (!visible) this.dialogo.set(null);
  }

  private terminar(mensaje: string): void {
    this.aviso.exito(mensaje);
    this.dialogo.set(null);
    this.guardando.set(false);
    this.cargar();
    this.refrescarGeneral();
    this.api.roles().subscribe({ next: (r) => this.roles.set(r), error: () => undefined });
  }

  private fallar(e: unknown): void {
    this.aviso.error(e);
    this.guardando.set(false);
  }

  // ---- perfil
  protected abrirPerfil(): void {
    this.formPerfil.reset();
    this.dialogo.set('perfil');
  }

  protected crearPerfil(): void {
    if (this.formPerfil.invalid) {
      this.formPerfil.markAllAsTouched();
      return;
    }
    const v = this.formPerfil.getRawValue();
    this.guardando.set(true);
    this.api
      .crearPerfil({ nombreUsuario: v.nombreUsuario.trim(), correo: v.correo.trim(), nombreCompleto: v.nombreCompleto.trim(), rolCodigo: v.rolCodigo as string, contrasena: v.contrasena.trim() || null, idEmpleado: null })
      .subscribe({
        next: (r) => {
          this.terminar(r.contrasenaTemporal ? `Perfil creado. Contraseña temporal: ${r.contrasenaTemporal}` : 'Perfil creado.');
        },
        error: (e) => this.fallar(e),
      });
  }

  // ---- empleado
  protected abrirEmpleado(): void {
    this.formEmpleado.reset({ turno: 'MANANA' });
    this.dialogo.set('empleado');
  }

  protected asignarEmpleado(): void {
    if (this.formEmpleado.invalid) {
      this.formEmpleado.markAllAsTouched();
      return;
    }
    const v = this.formEmpleado.getRawValue();
    this.guardando.set(true);
    this.api
      .asignar({ nombreCompleto: v.nombreCompleto.trim(), cargo: v.cargo.trim(), turno: v.turno, salario: v.salario as number, idEstacion: v.idEstacion, telefono: v.telefono.trim() || null })
      .subscribe({ next: () => this.terminar('Empleado asignado.'), error: (e) => this.fallar(e) });
  }

  // ---- rol del empleado
  protected abrirRol(empleado: Empleado): void {
    this.empleadoActual.set(empleado);
    this.nuevoRol.setValue(empleado.rolCodigo);
    this.dialogo.set('rol');
  }

  protected cambiarRol(): void {
    const empleado = this.empleadoActual();
    const rol = this.nuevoRol.value;
    if (!empleado || !rol) return;
    this.guardando.set(true);
    this.api.cambiarRol(empleado.idEmpleado, rol).subscribe({ next: () => this.terminar('Rol actualizado.'), error: (e) => this.fallar(e) });
  }

  protected suspender(empleado: Empleado): void {
    this.confirmar.confirm({
      header: 'Suspender empleado',
      message: `¿Suspender a ${empleado.nombreCompleto}? No podrá ingresar al sistema hasta que lo reactives.`,
      acceptLabel: 'Sí, suspender',
      rejectLabel: 'Volver',
      accept: () => this.api.suspender(empleado.idEmpleado).subscribe({ next: () => this.terminar('Empleado suspendido.'), error: (e) => this.fallar(e) }),
    });
  }

  protected reactivar(empleado: Empleado): void {
    this.api.reactivar(empleado.idEmpleado).subscribe({ next: () => this.terminar('Empleado reactivado.'), error: (e) => this.fallar(e) });
  }

  // ---- permisos
  protected abrirPermisos(rol: Rol): void {
    this.rolActual.set(rol);
    this.marcados.set(new Set(rol.permisos));
    this.dialogo.set('permisos');
  }

  protected alternarPermiso(codigo: string): void {
    this.marcados.update((actual) => {
      const copia = new Set(actual);
      if (copia.has(codigo)) copia.delete(codigo);
      else copia.add(codigo);
      return copia;
    });
  }

  protected guardarPermisos(): void {
    const rol = this.rolActual();
    if (!rol) return;
    this.guardando.set(true);
    this.api.editarPermisos(rol.idRol, [...this.marcados()]).subscribe({ next: () => this.terminar('Permisos actualizados.'), error: (e) => this.fallar(e) });
  }

  protected exportar(): void {
    this.api.exportar('xlsx').subscribe({
      next: (blob) => guardarArchivo(blob, 'personal.xlsx'),
      error: (e) => this.aviso.error(e, 'No se pudo descargar'),
    });
  }
}
