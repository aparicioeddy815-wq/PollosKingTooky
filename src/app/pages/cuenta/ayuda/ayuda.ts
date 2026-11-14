import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { AyudaCliente } from '../../../core/models/gestion.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { AyudaService } from '../../../core/services/gestion.service';

@Component({
  selector: 'app-ayuda',
  template: `
    <section>
      <h2>Ayuda</h2>
      <p class="texto-tenue">Así funciona tu cuenta y cómo contactarnos.</p>

      @if (ayuda(); as a) {
        <ol class="pasos">
          @for (paso of a.pasos; track paso.numero) {
            <li>
              <span class="numero">{{ paso.numero }}</span>
              <div><strong>{{ paso.titulo }}</strong><p>{{ paso.detalle }}</p></div>
            </li>
          }
        </ol>

        <div class="contacto">
          <a class="boton" [href]="enlaceWhatsapp()" target="_blank" rel="noopener"><i class="pi pi-whatsapp"></i> Escríbenos por WhatsApp</a>
          <a class="boton secundario" [href]="'mailto:' + a.correoContacto"><i class="pi pi-envelope"></i> {{ a.correoContacto }}</a>
        </div>
      } @else if (cargando()) {
        <p class="texto-tenue">Cargando…</p>
      }
    </section>
  `,
  styles: [
    `
      .pasos { display: grid; gap: 0.9rem; margin: 1.25rem 0 1.5rem; padding: 0; list-style: none; }
      .pasos li { display: flex; gap: 1rem; padding: 1rem 1.2rem; border-radius: var(--kt-radio); background: var(--kt-superficie); }
      .pasos p { margin: 0.2rem 0 0; color: var(--kt-texto-suave); }
      .numero { display: grid; flex: none; place-items: center; width: 2.2rem; height: 2.2rem; border-radius: 50%; background: var(--kt-accion); color: #fff; font-weight: 800; }
      .contacto { display: flex; flex-wrap: wrap; gap: 0.75rem; }
      .boton { display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.75rem 1.2rem; border-radius: 999px; background: var(--kt-accion); color: #fff; font-weight: 700; text-decoration: none; }
      .boton.secundario { background: var(--kt-superficie-alta); color: var(--kt-texto); }
    `,
  ],
})
export class Ayuda implements OnInit {
  private readonly api = inject(AyudaService);
  private readonly aviso = inject(AvisoService);

  protected readonly ayuda = signal<AyudaCliente | null>(null);
  protected readonly cargando = signal(true);

  protected readonly enlaceWhatsapp = computed(() => {
    const a = this.ayuda();
    if (!a) return '';
    return `https://wa.me/${a.whatsappCorporativo.replace(/\D/g, '')}?text=${encodeURIComponent(a.mensajeWhatsapp)}`;
  });

  ngOnInit(): void {
    this.api.ayuda().subscribe({
      next: (a) => {
        this.ayuda.set(a);
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar la ayuda');
        this.cargando.set(false);
      },
    });
  }
}
