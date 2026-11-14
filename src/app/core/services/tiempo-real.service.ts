import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

const EVENTOS_COMANDAS = ['pedido-creado', 'pedido-listo', 'pedido-actualizado', 'timbre'];

/** Se conecta al flujo SSE del backend y avisa cada vez que cambia algo en las comandas. */
@Injectable({ providedIn: 'root' })
export class ComandasEnVivoService {
  private readonly auth = inject(AuthService);

  escuchar(): Observable<string> {
    return new Observable<string>((suscriptor) => {
      let fuente: EventSource | null = null;
      let reintento: ReturnType<typeof setTimeout> | undefined;
      let cerrado = false;

      const conectar = () => {
        const token = this.auth.accessToken();
        if (cerrado || !token) return;
        fuente = new EventSource(`${environment.apiUrl}/api/stream/comandas?access_token=${encodeURIComponent(token)}`);
        for (const nombre of EVENTOS_COMANDAS) {
          fuente.addEventListener(nombre, () => suscriptor.next(nombre));
        }
        fuente.onerror = () => {
          fuente?.close();
          fuente = null;
          // Reconecta con el token vigente (la lista se sigue refrescando sola mientras tanto)
          if (!cerrado) reintento = setTimeout(conectar, 5000);
        };
      };

      conectar();
      return () => {
        cerrado = true;
        if (reintento) clearTimeout(reintento);
        fuente?.close();
      };
    });
  }
}

/** Timbre generado con Web Audio (no necesita archivos de sonido). El navegador exige un clic para activarlo. */
@Injectable({ providedIn: 'root' })
export class SonidoService {
  private contexto: AudioContext | null = null;
  readonly activo = signal(false);

  activar(): void {
    try {
      this.contexto ??= new AudioContext();
      void this.contexto.resume();
      this.activo.set(true);
      this.timbre();
    } catch {
      this.activo.set(false);
    }
  }

  desactivar(): void {
    this.activo.set(false);
  }

  timbre(): void {
    const contexto = this.contexto;
    if (!this.activo() || !contexto) return;
    const inicio = contexto.currentTime;
    [880, 1175].forEach((frecuencia, i) => {
      const t = inicio + i * 0.25;
      const oscilador = contexto.createOscillator();
      const ganancia = contexto.createGain();
      oscilador.type = 'sine';
      oscilador.frequency.value = frecuencia;
      ganancia.gain.setValueAtTime(0.0001, t);
      ganancia.gain.exponentialRampToValueAtTime(0.35, t + 0.02);
      ganancia.gain.exponentialRampToValueAtTime(0.0001, t + 0.22);
      oscilador.connect(ganancia);
      ganancia.connect(contexto.destination);
      oscilador.start(t);
      oscilador.stop(t + 0.25);
    });
  }
}
