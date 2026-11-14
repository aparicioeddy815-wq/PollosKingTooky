package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.inventario.MovimientoInventario;
import com.kingtokky.domain.model.inventario.TipoMovimiento;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    Page<MovimientoInventario> findByInsumo_IdOrderByFechaHoraDescIdDesc(Long idInsumo, Pageable pageable);

    Page<MovimientoInventario> findAllByOrderByFechaHoraDescIdDesc(Pageable pageable);

    /** Consumo (en valor positivo) de cada insumo desde una fecha. Devuelve [idInsumo, cantidad]. */
    @Query("""
            select m.insumo.id, sum(-m.variacion) from MovimientoInventario m
            where m.tipo = :tipo and m.fechaHora >= :desde group by m.insumo.id
            """)
    List<Object[]> consumoDesde(@Param("tipo") TipoMovimiento tipo, @Param("desde") LocalDateTime desde);

    /** Movimientos desde una fecha, del más antiguo al más reciente, con su insumo (para las gráficas). */
    @Query("""
            select m from MovimientoInventario m join fetch m.insumo
            where m.fechaHora >= :desde order by m.fechaHora asc
            """)
    List<MovimientoInventario> desdeConInsumo(@Param("desde") LocalDateTime desde);
}