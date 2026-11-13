package com.kingtokky.domain.port;

/** Puerto de salida para enviar correos (el adaptador usa SMTP). */
public interface NotificadorCorreo {
    void enviar(String destino, String asunto, String cuerpoTexto);
}
