import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  username: string = '';
  password: string = '';
  rememberMe: boolean = false;
  showPassword: boolean = false;
  isLoginTab: boolean = true;
  errorMessage: string = '';
  isLoading: boolean = false;

  goToMenu(): void {
    this.router.navigate(['/menu']);
  }

  goToDespacho(): void {
    this.router.navigate(['/despacho']);
  }

  goToGerente(): void {
    this.router.navigate(['/gerente']);
  }

  goToAdmin(): void {
    this.router.navigate(['/admin']);
  }

  goToCliente(): void {
    this.router.navigate(['/cliente']);
  }

  onLogin(): void {
    if (!this.username || !this.password) {
      this.errorMessage = 'Por favor, ingresa tu usuario y contraseña.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: (response) => {
        this.isLoading = false;

        // Redirección explícita según el rol que devuelve PostgreSQL
        if (response.role === 'DESPACHO') {
          this.router.navigate(['/despacho']);
        } else if (response.role === 'GERENCIA') {
          this.router.navigate(['/gerente']);
        } else if (response.role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else if (response.role === 'CLIENTE' || response.role === 'SALON') {
          this.router.navigate(['/cliente']); // <--- Te lleva directo a tus Comprobantes
        } else {
          this.router.navigate(['/cliente']);
        }
      },
      error: (err) => {
        this.isLoading = false;

        if (err.status === 401 || err.status === 403 || err.status === 400) {
          this.errorMessage = 'Usuario o contraseña incorrectos. Acceso denegado por la base de datos.';
        } else {
          this.errorMessage = 'No se pudo conectar con el servidor backend.';
        }
      }
    });
  }
}
