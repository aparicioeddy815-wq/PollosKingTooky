import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { PerfilUsuario } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';

@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule, DatePipe, ButtonModule, InputTextModule, PasswordModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss',
})
export class Perfil implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly aviso = inject(AvisoService);

  protected readonly perfil = signal<PerfilUsuario | null>(null);
  protected readonly guardando = signal(false);
  protected readonly cambiando = signal(false);

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
