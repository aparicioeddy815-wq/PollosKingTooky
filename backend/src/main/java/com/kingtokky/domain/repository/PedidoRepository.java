package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.pedido.EstadoPedido;
import com.kingtokky.domain.model.pedido.Pedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByFechaTurnoOrderByNumeroTurnoDesc(LocalDate fecha);

    List<Pedido> findByFechaTurnoAndEstadoOrderByNumeroTurnoDesc(LocalDate fecha, EstadoPedido estado);

    List<Pedido> findByFechaTurnoAndEstadoInOrderByNumeroTurnoAsc(LocalDate fecha, Collection<EstadoPedido> estados);

    long countByFechaTurnoAndEstado(LocalDate fecha, EstadoPedido estado);

    long countByFechaTurnoAndEstadoIn(LocalDate fecha, Collection<EstadoPedido> estados);

    @Query("select coalesce(sum(p.total), 0) from Pedido p where p.fechaTurno between :desde and :hasta and p.estado <> :cancelado")
    BigDecimal ventasEntre(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, @Param("cancelado") EstadoPedido cancelado);

    @Query("select count(p) from Pedido p where p.fechaTurno between :desde and :hasta and p.estado <> :cancelado")
    long contarEntre(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, @Param("cancelado") EstadoPedido cancelado);

    /** [codigoCategoria, nombre, ingresos, costo] por categoría en un rango de fechas. */
    @Query("""
            select c.codigo, c.nombre, sum(d.subtotal), sum(d.cantidad * p.costo)
            from DetallePedido d join d.producto p join p.categoria c join d.pedido ped
            where ped.fechaTurno between :desde and :hasta and ped.estado <> :cancelado
            group by c.codigo, c.nombre
            order by sum(d.subtotal) desc
            """)
    List<Object[]> ventasPorCategoria(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, @Param("cancelado") EstadoPedido cancelado);

    /** [idProducto, nombre, unidades, ingresos] de los productos más vendidos. */
    @Query("""
            select p.id, p.nombre, sum(d.cantidad), sum(d.subtotal)
            from DetallePedido d join d.producto p join d.pedido ped
            where ped.fechaTurno between :desde and :hasta and ped.estado <> :cancelado
            group by p.id, p.nombre
            order by sum(d.cantidad) desc
            """)
    List<Object[]> productosMasVendidos(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta,
                                        @Param("cancelado") EstadoPedido cancelado, Pageable pageable);

    /** [canal, ingresos, cantidad de pedidos] por canal de venta. */
    @Query("""
            select p.canal, sum(p.total), count(p) from Pedido p
            where p.fechaTurno between :desde and :hasta and p.estado <> :cancelado group by p.canal
            """)
    List<Object[]> ventasPorCanal(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, @Param("cancelado") EstadoPedido cancelado);

    /** [fecha, ingresos] por día. */
    @Query("""
            select p.fechaTurno, sum(p.total) from Pedido p
            where p.fechaTurno between :desde and :hasta and p.estado <> :cancelado
            group by p.fechaTurno order by p.fechaTurno
            """)
    List<Object[]> ventasPorDia(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, @Param("cancelado") EstadoPedido cancelado);

    /** [creadoEn, listoEn] de los pedidos ya despachados (para el tiempo promedio de despacho). */
    @Query("select p.creadoEn, p.listoEn from Pedido p where p.fechaTurno between :desde and :hasta and p.listoEn is not null")
    List<Object[]> tiemposDeDespacho(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("select p from Pedido p where p.fechaTurno = :fecha and p.estado <> :cancelado order by p.creadoEn")
    List<Pedido> vigentesDelDia(@Param("fecha") LocalDate fecha, @Param("cancelado") EstadoPedido cancelado);

    @Query("select coalesce(sum(d.cantidad * p.costo), 0) from DetallePedido d join d.producto p join d.pedido ped " +
           "where ped.fechaTurno between :desde and :hasta and ped.estado <> :cancelado")
    BigDecimal costoDeVentas(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, @Param("cancelado") EstadoPedido cancelado);
}
