import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { AuthService } from '../../../core/services/auth.service';
import { mensajeDeError } from '../../../core/utils/errores';
import { AccesoTarjeta } from '../../../shared/components/acceso-tarjeta/acceso-tarjeta';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, ButtonModule, CheckboxModule, InputTextModule, PasswordModule, AccesoTarjeta],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Ruta a la que volver después de ingresar (llega por query param). */
  readonly volverA = input<string>();

  protected readonly enviando = signal(false);
  protected readonly error = signal('');

  protected readonly formulario = this.fb.nonNullable.group({
    nombreUsuario: ['', Validators.required],
    contrasena: ['', Validators.required],
    recordarme: [false],
  });

  protected ingresar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set('');

    this.auth.login(this.formulario.getRawValue()).subscribe({
      next: () => {
        const destino = this.volverA() || (this.auth.esPersonal() ? '/panel' : '/mi-cuenta');
        this.router.navigateByUrl(destino);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.enviando.set(false);
      },
    });
  }
}
