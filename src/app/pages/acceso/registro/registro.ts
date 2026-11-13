import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { mensajeDeError } from '../../../core/utils/errores';
import { AccesoTarjeta } from '../../../shared/components/acceso-tarjeta/acceso-tarjeta';

function contrasenasIguales(grupo: AbstractControl): ValidationErrors | null {
  const contrasena = grupo.get('contrasena')?.value;
  const repetir = grupo.get('repetir')?.value;
  return contrasena === repetir ? null : { distintas: true };
}

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink, ButtonModule, InputTextModule, PasswordModule, AccesoTarjeta],
  templateUrl: './registro.html',
  styleUrl: './registro.scss',
})
export class Registro {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly aviso = inject(AvisoService);
  private readonly router = inject(Router);

  protected readonly enviando = signal(false);
  protected readonly error = signal('');

  protected readonly formulario = this.fb.nonNullable.group(
    {
      nombreCompleto: ['', [Validators.required, Validators.maxLength(255)]],
      nombreUsuario: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9._-]{3,50}$/)]],
      correo: ['', [Validators.required, Validators.email]],
      contrasena: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
      repetir: ['', Validators.required],
    },
    { validators: contrasenasIguales },
  );

  protected invalido(campo: 'nombreCompleto' | 'nombreUsuario' | 'correo' | 'contrasena' | 'repetir'): boolean {
    const control = this.formulario.controls[campo];
    return control.touched && control.invalid;
  }

  protected crearCuenta(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set('');

    const { repetir, ...datos } = this.formulario.getRawValue();
    this.auth.registrar(datos).subscribe({
      next: (sesion) => {
        this.aviso.exito(`Bienvenido a King Tooky, ${sesion.nombreCompleto}.`, 'Cuenta creada');
        this.router.navigate(['/mi-cuenta']);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.enviando.set(false);
      },
    });
  }
}
