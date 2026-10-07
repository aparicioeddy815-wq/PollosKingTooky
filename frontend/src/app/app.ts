import { Component } from '@angular/core';
import { ValidarAccesoComponent } from './validar-acceso/validar-acceso';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ValidarAccesoComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent {
  title = 'frontend';
}
