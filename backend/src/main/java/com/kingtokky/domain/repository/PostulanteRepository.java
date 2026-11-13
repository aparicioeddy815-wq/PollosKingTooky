package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.entrevista.EstadoPostulante;
import com.kingtokky.domain.model.entrevista.Postulante;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostulanteRepository extends JpaRepository<Postulante, Long> {

    @Query("""
            select p from Postulante p
            where (:todasVacantes = true or p.vacante.id = :idVacante) and (:todosEstados = true or p.estado = :estado)
            order by p.creadoEn desc
            """)
    List<Postulante> filtrar(@Param("todasVacantes") boolean todasVacantes, @Param("idVacante") Long idVacante,
                             @Param("todosEstados") boolean todosEstados, @Param("estado") EstadoPostulante estado);

    long countByEstado(EstadoPostulante estado);

    long countByEstadoIn(Collection<EstadoPostulante> estados);
}
