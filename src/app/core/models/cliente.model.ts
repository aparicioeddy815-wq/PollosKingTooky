export interface ProductoSalon {
  idProducto: number;
  nombre: string;
  descripcion: string | null;
  precio: number;
  categoria: string;
  imagenUrl: string | null;
  disponible: boolean;
  meGusta: boolean;
  vecesConsumidas: number;
}

export interface CategoriaSalon {
  codigo: string;
  nombre: string;
  productos: ProductoSalon[];
}

export type TipoQueja = 'RECLAMO' | 'SUGERENCIA' | 'FELICITACION';
export type CategoriaQueja = 'ATENCION' | 'COMIDA' | 'TIEMPO_ESPERA' | 'LIMPIEZA' | 'OTRO';

export interface QuejaRequest {
  tipo: TipoQueja;
  categoria: CategoriaQueja;
  numeroPedido: number | null;
  mensaje: string;
}

export interface Queja {
  idQueja: number;
  idUsuario: number;
  cliente: string;
  tipo: TipoQueja;
  categoria: CategoriaQueja;
  numeroPedido: number | null;
  mensaje: string;
  estado: string;
  respuesta: string | null;
  fechaCreacion: string;
  fechaRespuesta: string | null;
}

export interface EvaluacionDiaria {
  fecha: string;
  estrellas: number;
  comentario: string | null;
}
