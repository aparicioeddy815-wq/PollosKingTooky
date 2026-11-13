export interface LoginRequest {
  nombreUsuario: string;
  contrasena: string;
  recordarme: boolean;
}

export interface RegistroRequest {
  nombreUsuario: string;
  nombreCompleto: string;
  correo: string;
  contrasena: string;
}

export interface Sesion {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  idUsuario: number;
  nombreUsuario: string;
  nombreCompleto: string;
  correo: string;
  rol: string;
  permisos: string[];
}

export interface Mensaje {
  mensaje: string;
}

export interface PerfilUsuario {
  idUsuario: number;
  nombreUsuario: string;
  nombreCompleto: string;
  correo: string;
  fotoUrl: string | null;
  rol: string;
  fechaIngreso: string;
  ultimoAcceso: string | null;
  totalAccesos: number;
  accesosPorSemana: number;
}

export interface PerfilRequest {
  nombreCompleto: string;
  correo: string;
  fotoUrl: string | null;
}

export interface CambiarContrasenaRequest {
  contrasenaActual: string;
  nuevaContrasena: string;
}
