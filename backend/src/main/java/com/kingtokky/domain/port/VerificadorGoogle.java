package com.kingtokky.domain.port;

/** Puerto de salida para validar el token de "Iniciar sesión con Google". */
public interface VerificadorGoogle {

    record CuentaGoogle(String correo, String nombre, String foto) {}

    CuentaGoogle verificar(String idToken);
}
