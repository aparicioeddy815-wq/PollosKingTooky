package com.kingtokky.backend.repositories;

import com.kingtokky.backend.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Busca el usuario por su nombre de usuario en la base de datos
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}