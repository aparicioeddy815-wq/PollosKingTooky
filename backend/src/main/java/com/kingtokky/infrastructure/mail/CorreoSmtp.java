package com.kingtokky.infrastructure.mail;

import com.kingtokky.domain.port.NotificadorCorreo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class CorreoSmtp implements NotificadorCorreo {

    private static final Logger log = LoggerFactory.getLogger(CorreoSmtp.class);

    private final JavaMailSender enviador;

    public CorreoSmtp(JavaMailSender enviador) {
        this.enviador = enviador;
    }

    @Override
    public void enviar(String destino, String asunto, String cuerpoTexto) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom("King Tokky <no-responder@kingtokky.bo>");
            mensaje.setTo(destino);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpoTexto);
            enviador.send(mensaje);
        } catch (RuntimeException e) {
            // Un correo que no sale no debe tumbar la operación; queda registrado para revisarlo
            log.warn("No se pudo enviar el correo a {}: {}", destino, e.getMessage());
        }
    }
}
