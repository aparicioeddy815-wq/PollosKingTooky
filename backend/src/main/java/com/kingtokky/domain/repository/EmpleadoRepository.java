package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.personal.Empleado;
import com.kingtokky.domain.model.personal.EstadoEmpleado;
import com.kingtokky.domain.model.personal.Turno;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    @Query("""
            select e from Empleado e left join e.usuario u left join u.rol r
            where (:patron = '' or lower(e.nombreCompleto) like :patron or lower(e.codigo) like :patron or lower(e.cargo) like :patron)
              and (:todosRoles = true or r.codigo = :rol)
              and (:todosTurnos = true or e.turno = :turno)
            order by e.codigo
            """)
    Page<Empleado> buscar(@Param("patron") String patron, @Param("todosRoles") boolean todosRoles, @Param("rol") String rol,
                          @Param("todosTurnos") boolean todosTurnos, @Param("turno") Turno turno, Pageable pageable);

    @Query("""
            select e from Empleado e left join e.usuario u left join u.rol r
            where (:todosRoles = true or r.codigo = :rol)
            order by e.codigo
            """)
    List<Empleado> listarParaNomina(@Param("todosRoles") boolean todosRoles, @Param("rol") String rol);

    Optional<Empleado> findByUsuario_IdUsuario(Long idUsuario);

    long countByEstado(EstadoEmpleado estado);

    long countByEstadoAndTurno(EstadoEmpleado estado, Turno turno);

    long countByUsuarioIsNotNull();

    List<Empleado> findByEstacion_IdAndEstado(Long idEstacion, EstadoEmpleado estado);

    @Query("select max(e.codigo) from Empleado e")
    String ultimoCodigo();
}
