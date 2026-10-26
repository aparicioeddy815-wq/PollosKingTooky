package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.ConsumoItemRequest;
import com.kingtooky.backend.application.dto.ConsumoRequest;
import com.kingtooky.backend.application.dto.ConsumoResponse;
import com.kingtooky.backend.application.dto.MovimientoResponse;
import com.kingtooky.backend.infrastructure.entity.MovimientoInventarioEntity;
import com.kingtooky.backend.infrastructure.entity.ProductoEntity;
import com.kingtooky.backend.infrastructure.entity.ProductoInsumoEntity;
import com.kingtooky.backend.presentation.exception.RecursoNoEncontradoException;
import com.kingtooky.backend.presentation.exception.ReglaNegocioException;
import com.kingtooky.backend.repository.ProductoInsumoJpaRepository;
import com.kingtooky.backend.repository.ProductoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ConsumoService {

    private final ProductoJpaRepository productoRepository;
    private final ProductoInsumoJpaRepository recetaRepository;
    private final MovimientoInventarioService movimientoService;
    private final AuditoriaService auditoriaService;

    public ConsumoService(ProductoJpaRepository productoRepository, ProductoInsumoJpaRepository recetaRepository,
                          MovimientoInventarioService movimientoService, AuditoriaService auditoriaService) {
        this.productoRepository = productoRepository;
        this.recetaRepository = recetaRepository;
        this.movimientoService = movimientoService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ConsumoResponse registrar(ConsumoRequest request) {
        movimientoService.validarPedido(request.getIdPedido());

        Map<Integer, BigDecimal> requeridos = new TreeMap<>();
        for (ConsumoItemRequest item : request.getItems()) {
            ProductoEntity p = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + item.getIdProducto()));
            List<ProductoInsumoEntity> receta = recetaRepository.findByProducto(p.getIdProducto());
            if (receta.isEmpty()) {
                throw new ReglaNegocioException("El producto '" + p.getNombre() + "' no tiene receta (insumos) definida");
            }
            for (ProductoInsumoEntity ri : receta) {
                BigDecimal total = ri.getCantidadRequerida().multiply(BigDecimal.valueOf(item.getCantidad()));
                requeridos.merge(ri.getId().getIdInsumo(), total, BigDecimal::add);
            }
        }

        String motivo = request.getIdPedido() != null ? "Consumo pedido #" + request.getIdPedido() : "Consumo de preparacion";
        List<MovimientoResponse> movimientos = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> e : requeridos.entrySet()) {
            BigDecimal cantidad = e.getValue().setScale(2, RoundingMode.CEILING);
            MovimientoInventarioEntity m = movimientoService.aplicar(e.getKey(), MovimientoInventarioService.SALIDA,
                    cantidad, null, request.getIdPedido(), motivo);
            movimientos.add(movimientoService.aRespuesta(m));
        }
        auditoriaService.registrar("CONSUMO", "pedido", request.getIdPedido(),
                "Consumo registrado: " + movimientos.size() + " insumos descontados");

        ConsumoResponse r = new ConsumoResponse();
        r.setIdPedido(request.getIdPedido());
        r.setMovimientos(movimientos);
        return r;
    }
}