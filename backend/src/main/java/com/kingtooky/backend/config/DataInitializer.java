package com.kingtooky.backend.config;

import com.kingtooky.backend.infrastructure.entity.RolEntity;
import com.kingtooky.backend.infrastructure.entity.UsuarioEntity;
import com.kingtooky.backend.repository.RolJpaRepository;
import com.kingtooky.backend.repository.UsuarioJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioJpaRepository usuarioRepository;
    private final RolJpaRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.usuario:admin}")
    private String adminUsuario;

    @Value("${app.admin.contrasena:Admin123*}")
    private String adminContrasena;

    public DataInitializer(UsuarioJpaRepository usuarioRepository, RolJpaRepository rolRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<UsuarioEntity> usuarios = usuarioRepository.findAll();
        for (UsuarioEntity u : usuarios) {
            String pass = u.getContrasena();
            if (pass != null && !pass.startsWith("$2")) {
                u.setContrasena(passwordEncoder.encode(pass));
                usuarioRepository.save(u);
                log.info("Contrasena de '{}' migrada a BCrypt", u.getNombreUsuario());
            }
        }

        if (!usuarioRepository.existsByNombreUsuarioIgnoreCase(adminUsuario)) {
            RolEntity rolAdmin = rolRepository.findByNombre("ADMINISTRADOR").orElse(null);
            if (rolAdmin == null) {
                log.warn("No existe el rol ADMINISTRADOR; no se creo el usuario inicial");
                return;
            }
            UsuarioEntity admin = new UsuarioEntity();
            admin.setNombreUsuario(adminUsuario);
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setContrasena(passwordEncoder.encode(adminContrasena));
            admin.setEstado(1);
            admin.setRol(rolAdmin);
            usuarioRepository.save(admin);
            log.info("Usuario administrador inicial creado: '{}'", adminUsuario);
        }
    }
}