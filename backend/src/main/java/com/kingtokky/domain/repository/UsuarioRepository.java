package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.seguridad.EstadoUsuario;
import com.kingtokky.domain.model.seguridad.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreUsuarioIgnoreCaseOrCorreoIgnoreCase(String nombreUsuario, String correo);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByCorreoIgnoreCase(String correo);

    long countByRol_CodigoAndEstado(String codigoRol, EstadoUsuario estado);

    long countByRol_CodigoNot(String codigoRol);

    long countByRol_Codigo(String codigoRol);

    List<Usuario> findByRol_Codigo(String codigoRol);

    @Query("""
            select u from Usuario u
            where (:patron = '' or lower(u.nombreCompleto) like :patron or lower(u.nombreUsuario) like :patron or lower(u.correo) like :patron)
              and (:todosEstados = true or u.estado = :estado)
            order by u.nombreCompleto
            """)
    Page<Usuario> buscar(@Param("patron") String patron, @Param("todosEstados") boolean todosEstados,
                         @Param("estado") EstadoUsuario estado, Pageable pageable);
}
