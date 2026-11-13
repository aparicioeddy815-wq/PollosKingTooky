export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface ErrorApi {
  timestamp: string;
  status: number;
  error: string;
  mensaje: string;
  ruta: string;
  detalles: string[];
}
