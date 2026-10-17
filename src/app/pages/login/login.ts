import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent {
  username: string = '';
  password: string = '';
  rememberMe: boolean = false;
  showPassword: boolean = false;
  isLoginTab: boolean = true;
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private router: Router) {}

  goToMenu(): void {
    this.router.navigate(['/menu']);
  }

  onLogin(): void {
    if (!this.username || !this.password) {
      this.errorMessage = 'Por favor, ingresa tu usuario y contraseña.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    setTimeout(() => {
      this.isLoading = false;
      if (this.username === 'admin' && this.password === 'admin123') {
        this.router.navigate(['/menu']);
      } else {
        this.errorMessage = 'Credenciales incorrectas. Intenta nuevamente.';
      }
    }, 1200);
  }
}
