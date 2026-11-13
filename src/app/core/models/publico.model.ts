import { ProductoPublico } from './producto.model';

export interface Horario {
  dias: string;
  horario: string;
}

export interface Ubicacion {
  direccion: string;
  latitud: number;
  longitud: number;
}

export interface InicioPublico {
  nombreLocal: string;
  productoMasVendido: ProductoPublico | null;
  horarios: Horario[];
  ubicacion: Ubicacion;
}

export interface SobreNosotros {
  nombreLocal: string;
  historia: string;
}

export interface TurnosPublico {
  enPreparacion: number[];
  listos: number[];
  actualizado: string;
}
