import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface SerieGrafica {
  nombre: string;
  valores: number[];
  color?: string;
}

const PRIMER_COLOR = 'var(--kt-accion)';
const PALETA = [
  PRIMER_COLOR,
  'var(--kt-secondary-50)',
  'var(--kt-tertiary-70)',
  'var(--kt-neutral-40)',
  'var(--kt-primary-70)',
  'var(--kt-secondary-70)',
];
const ANCHO = 600;
const MARGEN = { izq: 56, der: 12, arr: 18, aba: 34 };
const RADIO = 70;
const CIRCUNFERENCIA = 2 * Math.PI * RADIO;
const numeroEs = new Intl.NumberFormat('es-BO', { maximumFractionDigits: 2 });

/** Redondea hacia arriba el máximo a un número "bonito" para el eje (1, 2, 5, 10 × 10^n). */
function techo(maximo: number): number {
  if (maximo <= 0) return 1;
  const exponente = Math.pow(10, Math.floor(Math.log10(maximo)));
  const f = maximo / exponente;
  const bonito = f <= 1 ? 1 : f <= 2 ? 2 : f <= 5 ? 5 : 10;
  return bonito * exponente;
}

function compacto(n: number): string {
  const a = Math.abs(n);
  if (a >= 1_000_000) return `${(n / 1_000_000).toFixed(1).replace(/\.0$/, '')} M`;
  if (a >= 1_000) return `${(n / 1_000).toFixed(1).replace(/\.0$/, '')} k`;
  return Number.isInteger(n) ? String(n) : n.toFixed(1);
}

function recortar(texto: string, maximo = 11): string {
  return texto.length > maximo ? `${texto.slice(0, maximo - 1)}…` : texto;
}

/** Gráfica sencilla sin dependencias: barras, líneas o dona. Usa los colores de la marca. */
@Component({
  selector: 'app-grafica',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (vacia()) {
      <p class="vacio">Todavía no hay datos para mostrar.</p>
    } @else if (tipo() === 'dona') {
      <div class="dona">
        <svg viewBox="0 0 200 200" role="img" [attr.aria-label]="titulo()">
          <circle class="fondo" cx="100" cy="100" r="70" />
          @for (s of segmentos(); track s.id) {
            <circle
              cx="100"
              cy="100"
              r="70"
              fill="none"
              stroke-width="28"
              transform="rotate(-90 100 100)"
              [style.stroke]="s.color"
              [attr.stroke-dasharray]="s.dash"
              [attr.stroke-dashoffset]="s.desfase"
            >
              <title>{{ s.etiqueta }}: {{ s.texto }} ({{ s.porcentaje }}%)</title>
            </circle>
          }
          <text x="100" y="98" text-anchor="middle" class="centro-valor">{{ totalDona() }}</text>
          <text x="100" y="118" text-anchor="middle" class="centro-etiqueta">{{ centro() }}</text>
        </svg>
        <ul class="leyenda">
          @for (s of segmentos(); track s.id) {
            <li>
              <span class="punto" [style.background]="s.color"></span>
              <span class="nombre">{{ s.etiqueta }}</span>
              <strong>{{ s.texto }}</strong>
              <small>{{ s.porcentaje }}%</small>
            </li>
          }
        </ul>
      </div>
    } @else {
      <svg [attr.viewBox]="viewBox()" role="img" [attr.aria-label]="titulo()">
        @for (g of guias(); track g.k) {
          <line class="guia" [attr.x1]="margen.izq" [attr.x2]="ancho - margen.der" [attr.y1]="g.y" [attr.y2]="g.y" />
          <text class="eje" text-anchor="end" [attr.x]="margen.izq - 8" [attr.y]="g.y + 4">{{ g.texto }}</text>
        }

        @if (tipo() === 'barras') {
          @for (b of barras(); track b.id) {
            <rect rx="3" [attr.x]="b.x" [attr.y]="b.y" [attr.width]="b.w" [attr.height]="b.h" [style.fill]="b.color">
              <title>{{ b.texto }}</title>
            </rect>
            @if (mostrarValores()) {
              <text class="valor" text-anchor="middle" [attr.x]="b.x + b.w / 2" [attr.y]="b.y - 5">{{ b.corto }}</text>
            }
          }
        } @else {
          @for (l of lineas(); track l.nombre) {
            @if (l.conArea) {
              <path class="area" [attr.d]="l.area" [style.fill]="l.color" />
            }
            <path class="linea" [attr.d]="l.trazo" [style.stroke]="l.color" />
            @for (p of l.puntos; track p.id) {
              <circle r="3.5" [attr.cx]="p.x" [attr.cy]="p.y" [style.fill]="l.color">
                <title>{{ p.texto }}</title>
              </circle>
            }
          }
        }

        @for (e of ejeX(); track e.id) {
          @if (e.visible) {
            <text class="eje" text-anchor="middle" [attr.x]="e.x" [attr.y]="e.y">
              {{ e.texto }}
              <title>{{ e.completo }}</title>
            </text>
          }
        }
      </svg>

      @if (series().length > 1) {
        <ul class="leyenda en-linea">
          @for (s of series(); track s.nombre; let i = $index) {
            <li><span class="punto" [style.background]="colorDe(i, 0)"></span>{{ s.nombre }}</li>
          }
        </ul>
      }
    }
  `,
  styles: [
    `
      :host {
        display: block;
      }
      svg {
        display: block;
        width: 100%;
        height: auto;
        overflow: visible;
      }
      .guia {
        stroke: var(--kt-linea);
        stroke-width: 1;
      }
      .eje {
        fill: var(--kt-texto-tenue);
        font-size: 11px;
      }
      .valor {
        fill: var(--kt-texto-suave);
        font-size: 11px;
        font-weight: 700;
      }
      .fondo {
        fill: none;
        stroke: var(--kt-superficie-alta);
        stroke-width: 28;
      }
      .centro-valor {
        fill: var(--kt-texto);
        font-size: 24px;
        font-weight: 800;
      }
      .centro-etiqueta {
        fill: var(--kt-texto-tenue);
        font-size: 11px;
      }
      .linea {
        fill: none;
        stroke-width: 2.5;
        stroke-linejoin: round;
        stroke-linecap: round;
      }
      .area {
        opacity: 0.12;
        stroke: none;
      }
      .dona {
        display: grid;
        align-items: center;
        gap: 1rem;
        grid-template-columns: minmax(140px, 190px) 1fr;
      }
      @media (max-width: 520px) {
        .dona {
          grid-template-columns: 1fr;
          justify-items: center;
        }
      }
      .leyenda {
        display: grid;
        gap: 0.55rem;
        margin: 0;
        padding: 0;
        list-style: none;
        font-size: 0.9rem;
      }
      .leyenda li {
        display: flex;
        align-items: center;
        gap: 0.5rem;
      }
      .leyenda .nombre {
        flex: 1;
      }
      .leyenda small {
        color: var(--kt-texto-tenue);
      }
      .leyenda.en-linea {
        display: flex;
        flex-wrap: wrap;
        gap: 0.4rem 1.25rem;
        margin-top: 0.75rem;
      }
      .punto {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        flex: none;
      }
      .vacio {
        padding: 2rem 1rem;
        text-align: center;
        color: var(--kt-texto-tenue);
      }
    `,
  ],
})
export class Grafica {
  readonly tipo = input<'barras' | 'linea' | 'dona'>('barras');
  readonly etiquetas = input<string[]>([]);
  readonly series = input<SerieGrafica[]>([]);
  readonly formato = input<'bs' | 'numero'>('numero');
  /** Colores por punto (dona o barras de una sola serie). */
  readonly colores = input<string[]>([]);
  readonly alto = input(240);
  readonly titulo = input('Gráfica');
  /** Texto bajo el total en el centro de la dona. */
  readonly centro = input('');

  protected readonly ancho = ANCHO;
  protected readonly margen = MARGEN;

  protected readonly viewBox = computed(() => `0 0 ${ANCHO} ${this.alto()}`);
  protected readonly vacia = computed(
    () => this.etiquetas().length === 0 || this.series().every((s) => s.valores.every((v) => !v)),
  );

  private readonly areaAlto = computed(() => this.alto() - MARGEN.arr - MARGEN.aba);
  private readonly maximo = computed(() => techo(Math.max(0, ...this.series().flatMap((s) => s.valores))));
  private readonly grupo = computed(() => (ANCHO - MARGEN.izq - MARGEN.der) / Math.max(1, this.etiquetas().length));
  protected readonly mostrarValores = computed(() => this.etiquetas().length <= 8 && this.series().length === 1);

  protected readonly guias = computed(() => {
    const maximo = this.maximo();
    const area = this.areaAlto();
    return [0, 1, 2, 3, 4].map((k) => ({
      k,
      y: MARGEN.arr + area - (area * k) / 4,
      texto: compacto((maximo * k) / 4),
    }));
  });

  protected readonly barras = computed(() => {
    const series = this.series();
    const grupo = this.grupo();
    const area = this.areaAlto();
    const maximo = this.maximo();
    const n = Math.max(1, series.length);
    const ancho = Math.min(40, (grupo * 0.72) / n);
    const separacion = 3;
    const total = ancho * n + separacion * (n - 1);
    const resultado: { id: string; x: number; y: number; w: number; h: number; color: string; texto: string; corto: string }[] = [];
    series.forEach((serie, s) => {
      this.etiquetas().forEach((etiqueta, p) => {
        const valor = serie.valores[p] ?? 0;
        const h = (valor / maximo) * area;
        resultado.push({
          id: `${s}-${p}`,
          x: MARGEN.izq + p * grupo + (grupo - total) / 2 + s * (ancho + separacion),
          y: MARGEN.arr + area - h,
          w: ancho,
          h,
          color: this.colorDe(s, p),
          texto: `${etiqueta} · ${serie.nombre}: ${this.texto(valor)}`,
          corto: compacto(valor),
        });
      });
    });
    return resultado;
  });

  protected readonly lineas = computed(() => {
    const grupo = this.grupo();
    const area = this.areaAlto();
    const maximo = this.maximo();
    const base = MARGEN.arr + area;
    return this.series().map((serie, s) => {
      const puntos = this.etiquetas().map((etiqueta, p) => {
        const valor = serie.valores[p] ?? 0;
        return {
          id: `${s}-${p}`,
          x: MARGEN.izq + p * grupo + grupo / 2,
          y: MARGEN.arr + area - (valor / maximo) * area,
          texto: `${etiqueta} · ${serie.nombre}: ${this.texto(valor)}`,
        };
      });
      const trazo = puntos.map((pt, i) => `${i === 0 ? 'M' : 'L'}${pt.x.toFixed(1)} ${pt.y.toFixed(1)}`).join(' ');
      const primero = puntos.at(0);
      const ultimo = puntos.at(-1);
      const areaRelleno =
        primero && ultimo ? `${trazo} L${ultimo.x.toFixed(1)} ${base} L${primero.x.toFixed(1)} ${base} Z` : '';
      return { nombre: serie.nombre, color: this.colorDe(s, 0), trazo, area: areaRelleno, puntos, conArea: s === 0 };
    });
  });

  protected readonly ejeX = computed(() => {
    const grupo = this.grupo();
    const salto = Math.max(1, Math.ceil(this.etiquetas().length / 14)); // evita amontonar etiquetas
    return this.etiquetas().map((texto, p) => ({
      id: p,
      x: MARGEN.izq + p * grupo + grupo / 2,
      y: this.alto() - MARGEN.aba + 18,
      texto: recortar(texto),
      completo: texto,
      visible: p % salto === 0,
    }));
  });

  protected readonly segmentos = computed(() => {
    const valores = this.series()[0]?.valores ?? [];
    const total = valores.reduce((suma, v) => suma + v, 0);
    let acumulado = 0;
    return this.etiquetas().map((etiqueta, p) => {
      const valor = valores[p] ?? 0;
      const largo = total > 0 ? (valor / total) * CIRCUNFERENCIA : 0;
      const segmento = {
        id: p,
        etiqueta,
        color: this.colorDe(0, p),
        dash: `${largo.toFixed(2)} ${(CIRCUNFERENCIA - largo).toFixed(2)}`,
        desfase: (-acumulado).toFixed(2),
        porcentaje: total > 0 ? Math.round((valor / total) * 100) : 0,
        texto: this.texto(valor),
      };
      acumulado += largo;
      return segmento;
    });
  });

  protected readonly totalDona = computed(() =>
    numeroEs.format((this.series()[0]?.valores ?? []).reduce((suma, v) => suma + v, 0)),
  );

  protected colorDe(serie: number, punto: number): string {
    const personalizados = this.colores();
    if (this.series().length === 1 && personalizados.length > 0) {
      return personalizados[punto % personalizados.length] ?? PRIMER_COLOR;
    }
    return this.series()[serie]?.color ?? PALETA[serie % PALETA.length] ?? PRIMER_COLOR;
  }

  private texto(valor: number): string {
    return `${this.formato() === 'bs' ? 'Bs ' : ''}${numeroEs.format(valor)}`;
  }
}
