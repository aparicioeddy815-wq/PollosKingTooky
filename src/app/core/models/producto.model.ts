export type CategoriaProducto =
  | 'COMBOS_POPULARES'
  | 'POLLO_FRITO'
  | 'PIPOCAS_Y_SALCHIPAPAS'
  | 'GUARNICIONES'
  | 'BEBIDAS';

export interface Producto {
  idProducto: number;
  nombre: string;
  descripcion: string | null;
  precio: number;
  categoria: CategoriaProducto | null;
  imagenUrl: string | null;
  disponible: boolean;
}

export interface ProductoRequest {
  nombre: string;
  descripcion: string | null;
  precio: number;
  categoria: CategoriaProducto | null;
  imagenUrl: string | null;
  disponible: boolean;
}

/** Producto tal como lo devuelve el menú público (sin categoría). */
export interface ProductoPublico {
  idProducto: number;
  nombre: string;
  descripcion: string | null;
  precio: number;
  imagenUrl: string | null;
  disponible: boolean;
}

export interface CategoriaMenu {
  codigo: string;
  nombre: string;
  productos: ProductoPublico[];
  urlPedirAhora: string | null;
}

export interface MenuPublico {
  categorias: CategoriaMenu[];
  actualizado: string;
}
