package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.inventario.Insumo;
import com.kingtokky.domain.model.inventario.TipoInsumo;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    Optional<Insumo> findByCodigo(String codigo);

    @Query("""
            select i from Insumo i
            where i.activo = true and i.categoria.tipo = :tipo
              and (:patron = '' or lower(i.nombre) like :patron or lower(i.codigo) like :patron)
              and (:todasCategorias = true or i.categoria.id = :categoria)
            order by i.nombre
            """)
    List<Insumo> buscar(@Param("tipo") TipoInsumo tipo, @Param("patron") String patron,
                        @Param("todasCategorias") boolean todasCategorias, @Param("categoria") Long categoria);

    @Query("select i from Insumo i where i.activo = true and i.categoria.tipo = :tipo order by i.nombre")
    List<Insumo> listarPorTipo(@Param("tipo") TipoInsumo tipo);

    @Query("select i from Insumo i where i.activo = true and i.stockActual <= i.umbralMinimo order by (i.stockActual / i.umbralMinimo)")
    List<Insumo> listarBajoUmbral();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Insumo i where i.id = :id")
    Optional<Insumo> buscarParaActualizar(@Param("id") Long id);

    @Query("select coalesce(sum(i.stockActual * i.costoUnitario), 0) from Insumo i where i.activo = true and i.categoria.tipo = :tipo")
    BigDecimal valorInventario(@Param("tipo") TipoInsumo tipo);
}
