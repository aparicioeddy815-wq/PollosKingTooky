package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.pedido.DetallePedido;
import com.kingtokky.domain.model.pedido.EstadoPedido;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedido_Id(Long idPedido);

    List<DetallePedido> findByPedido_IdIn(Collection<Long> idsPedido);

    /** [idProducto, veces] que un cliente consumió cada producto (sin pedidos cancelados). */
    @Query("""
            select d.producto.id, sum(d.cantidad) from DetallePedido d
            where d.pedido.cliente.idUsuario = :idUsuario and d.pedido.estado <> :cancelado
            group by d.producto.id
            """)
    List<Object[]> consumoDelCliente(@Param("idUsuario") Long idUsuario, @Param("cancelado") EstadoPedido cancelado);
}
