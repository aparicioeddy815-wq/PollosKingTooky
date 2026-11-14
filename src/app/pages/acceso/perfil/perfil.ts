import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { PerfilUsuario } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { PerfilService } from '../../../core/services/gestion.service';

@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe, ButtonModule, InputTextModule, PasswordModule],
  template: `
    <div class="pagina">
      <header class="cabecera-perfil">
        <div class="avatar">
          @if (perfil()?.fotoUrl; as foto) {
            <img [src]="foto" alt="Tu foto de perfil" />
          } @else {
            <span>{{ iniciales() }}</span>
          }
        </div>
        <div class="datos-cabecera">
          <h1>Mi perfil</h1>
          @if (perfil(); as p) {
            <p>{{ p.nombreUsuario }} · {{ p.rol }} · en King Tooky desde {{ p.fechaIngreso | date: 'longDate' }}</p>
          }
          <input #archivo type="file" accept="image/png,image/jpeg,image/webp" hidden (change)="elegirFoto($event)" />
          <p-button label="Cambiar foto" icon="pi pi-camera" severity="secondary" size="small" [loading]="subiendo()" (onClick)="archivo.click()" />
        </div>
      </header>

      @if (perfil(); as p) {
        <div class="frecuencia">
          <article><span>Último acceso</span><strong>{{ p.ultimoAcceso ? (p.ultimoAcceso | date: "dd/MM/yyyy HH:mm") : 'Primera vez' }}</strong></article>
          <article><span>Accesos totales</span><strong>{{ p.totalAccesos }}</strong></article>
          <article><span>Frecuencia</span><strong>{{ p.accesosPorSemana | number: '1.0-1' }} por semana</strong></article>
        </div>
      }

      <div class="columnas">
        <form class="formulario bloque" [formGroup]="datos" (ngSubmit)="guardarDatos()" novalidate>
          <h2>Tus datos</h2>
          <div class="campo">
            <label for="nombre">Nombre completo</label>
            <input pInputText id="nombre" formControlName="nombreCompleto" />
          </div>
          <div class="campo">
            <label for="correo">Correo</label>
            <input pInputText id="correo" type="email" formControlName="correo" />
            @if (datos.controls.correo.touched && datos.controls.correo.invalid) { <small class="error">Escribe un correo válido.</small> }
          </div>
          <p-button type="submit" label="Guardar cambios" [loading]="guardando()" />
        </form>

        <form class="formulario bloque" [formGroup]="clave" (ngSubmit)="cambiarContrasena()" novalidate>
          <h2>Cambiar contraseña</h2>
          <div class="campo">
            <label for="actual">Contraseña actual</label>
            <p-password inputId="actual" formControlName="contrasenaActual" [feedback]="false" [toggleMask]="true" />
          </div>
          <div class="campo">
            <label for="nueva">Nueva contraseña</label>
            <p-password inputId="nueva" formControlName="nuevaContrasena" [feedback]="false" [toggleMask]="true" />
            @if (clave.controls.nuevaContrasena.touched && clave.controls.nuevaContrasena.invalid) { <small class="error">Mínimo 6 caracteres.</small> }
          </div>
          <p-button type="submit" label="Actualizar contraseña" severity="secondary" [loading]="cambiando()" />
        </form>
      </div>
    </div>
  `,
  styles: [
    `
      .pagina { display: grid; gap: 1.5rem; max-width: 1000px; margin: 0 auto; padding: 2rem 1rem; }
      .cabecera-perfil { display: flex; flex-wrap: wrap; align-items: center; gap: 1.5rem; }
      .avatar { display: grid; flex: none; place-items: center; width: 96px; height: 96px; overflow: hidden; border-radius: 50%; background: var(--kt-accion); color: #fff; font-size: 2rem; font-weight: 800; }
      .avatar img { width: 100%; height: 100%; object-fit: cover; }
      .datos-cabecera { display: grid; gap: 0.4rem; justify-items: start; }
      .datos-cabecera h1 { margin: 0; }
      .datos-cabecera p { margin: 0; color: var(--kt-texto-suave); }
      .frecuencia { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); }
      .frecuencia article { display: grid; gap: 0.25rem; padding: 1.1rem 1.3rem; border-radius: var(--kt-radio); background: var(--kt-superficie); }
      .frecuencia span { font-size: 0.88rem; font-weight: 600; color: var(--kt-texto-suave); }
      .frecuencia strong { font-family: var(--kt-fuente-titulo); font-size: 1.3rem; }
      .columnas { display: grid; gap: 1.5rem; }
      @media (min-width: 860px) { .columnas { grid-template-columns: 1fr 1fr; align-items: start; } }
      .bloque { padding: 1.5rem; border-radius: var(--kt-radio-grande); background: var(--kt-superficie); }
      .formulario { display: grid; gap: 1rem; }
      .campo { display: grid; gap: 0.35rem; }
      .campo label { font-weight: 600; font-size: 0.9rem; }
      .error { color: var(--kt-primary-40); }
    `,
  ],
})
export class Perfil implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly perfilApi = inject(PerfilService);
  private readonly aviso = inject(AvisoService);

  protected readonly perfil = signal<PerfilUsuario | null>(null);
  protected readonly guardando = signal(false);
  protected readonly cambiando = signal(false);
  protected readonly subiendo = signal(false);

  protected readonly iniciales = computed(() =>
    (this.perfil()?.nombreCompleto ?? '')
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((parte) => parte.charAt(0).toUpperCase())
      .join(''),
  );

  protected readonly datos = this.fb.nonNullable.group({
    nombreCompleto: ['', [Validators.required, Validators.maxLength(255)]],
    correo: ['', [Validators.required, Validators.email]],
  });

  protected readonly clave = this.fb.nonNullable.group({
    contrasenaActual: ['', Validators.required],
    nuevaContrasena: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
  });

  ngOnInit(): void {
    this.auth.obtenerPerfil().subscribe({
      next: (perfil) => {
        this.perfil.set(perfil);
        this.datos.patchValue({ nombreCompleto: perfil.nombreCompleto, correo: perfil.correo });
      },
      error: (e) => this.aviso.error(e, 'No pudimos cargar tu perfil'),
    });
  }

  protected elegirFoto(evento: Event): void {
    const entrada = evento.target as HTMLInputElement;
    const archivo = entrada.files?.item(0);
    entrada.value = '';
    if (!archivo) return;
    this.subiendo.set(true);
    this.perfilApi.subirFoto(archivo).subscribe({
      next: (perfil) => {
        this.perfil.set(perfil);
        this.aviso.exito('Tu foto se actualizó.');
        this.subiendo.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No se pudo subir la foto');
        this.subiendo.set(false);
      },
    });
  }

  protected guardarDatos(): void {
    if (this.datos.invalid) {
      this.datos.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.auth.actualizarPerfil({ ...this.datos.getRawValue(), fotoUrl: this.perfil()?.fotoUrl ?? null }).subscribe({
      next: (perfil) => {
        this.perfil.set(perfil);
        this.aviso.exito('Tus datos se guardaron.');
        this.guardando.set(false);
      },
      error: (e) => {
        this.aviso.error(e);
        this.guardando.set(false);
      },
    });
  }

  protected cambiarContrasena(): void {
    if (this.clave.invalid) {
      this.clave.markAllAsTouched();
      return;
    }
    this.cambiando.set(true);
    this.auth.cambiarContrasena(this.clave.getRawValue()).subscribe({
      next: () => {
        this.aviso.exito('Tu contraseña fue actualizada.');
        this.clave.reset();
        this.cambiando.set(false);
      },
      error: (e) => {
        this.aviso.error(e);
        this.cambiando.set(false);
      },
    });
  }
}
