package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.menu.Producto;
import com.kingtokky.domain.model.pedido.EstadoPedido;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByIdAndActivoTrue(Long id);

    List<Producto> findByActivoTrueAndPublicadoPedidosYaTrueOrderByCategoria_OrdenAscNombreAsc();

    List<Producto> findByActivoTrueAndEnSalonTrueOrderByCategoria_OrdenAscNombreAsc();

    @Query("""
            select p from Producto p
            where p.activo = true
              and (:patron = '' or lower(p.nombre) like :patron)
              and (:todasCategorias = true or p.categoria.codigo = :categoria)
              and (:todasDisponibilidades = true or p.disponible = :disponible)
            order by p.categoria.orden, p.nombre
            """)
    Page<Producto> buscar(@Param("patron") String patron, @Param("todasCategorias") boolean todasCategorias,
                          @Param("categoria") String categoria, @Param("todasDisponibilidades") boolean todasDisponibilidades,
                          @Param("disponible") boolean disponible, Pageable pageable);

    /** Unidades vendidas por producto desde una fecha (para "el más vendido"). */
    @Query("""
            select d.producto.id, sum(d.cantidad) from DetallePedido d
            where d.pedido.fechaTurno >= :desde and d.pedido.estado <> :cancelado and d.producto.publicadoPedidosYa = true
            group by d.producto.id order by sum(d.cantidad) desc
            """)
    List<Object[]> masVendidos(@Param("desde") LocalDate desde, @Param("cancelado") EstadoPedido cancelado, Pageable pageable);
}
