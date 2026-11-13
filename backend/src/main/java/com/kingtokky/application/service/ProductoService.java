package com.kingtokky.application.service;

import com.kingtokky.application.dto.ProductoDtos.CategoriaProductoResponse;
import com.kingtokky.application.dto.ProductoDtos.ProductoRequest;
import com.kingtokky.application.dto.ProductoDtos.ProductoResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.application.evento.EventoOperativo;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.menu.CategoriaProducto;
import com.kingtokky.domain.model.menu.Producto;
import com.kingtokky.domain.repository.CategoriaProductoRepository;
import com.kingtokky.domain.repository.ProductoRepository;
import com.kingtokky.presentation.exception.ApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestión del menú: productos, precios, disponibilidad y publicación en PedidosYA. */
@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productos;
    private final CategoriaProductoRepository categorias;
    private final AuditoriaService auditoria;
    private final ApplicationEventPublisher eventos;

    public ProductoService(ProductoRepository productos, CategoriaProductoRepository categorias, AuditoriaService auditoria,
                           ApplicationEventPublisher eventos) {
        this.productos = productos;
        this.categorias = categorias;
        this.auditoria = auditoria;
        this.eventos = eventos;
    }

    public PaginaResponse<ProductoResponse> listar(String q, String categoria, Boolean disponible, int pagina, int tamano) {
        String patron = q == null || q.isBlank() ? "" : "%" + q.trim().toLowerCase() + "%";
        boolean todasCategorias = categoria == null || categoria.isBlank();
        var page = productos.buscar(patron, todasCategorias, todasCategorias ? "" : categoria, disponible == null,
                disponible != null && disponible, PageRequest.of(pagina, Math.min(tamano, 100)));
        return PaginaResponse.de(page, this::aRespuesta);
    }

    public ProductoResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    public List<CategoriaProductoResponse> listarCategorias() {
        return categorias.findAllByOrderByOrdenAsc().stream()
                .map(c -> new CategoriaProductoResponse(c.getId(), c.getCodigo(), c.getNombre())).toList();
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        Producto p = new Producto();
        aplicar(p, req);
        if (req.costo() == null) p.setCosto(req.precio().multiply(new BigDecimal("0.45")).setScale(2, RoundingMode.HALF_UP));
        productos.save(p);
        auditoria.registrar(TipoEvento.CREACION, "Menú", "Creó el producto \"" + p.getNombre() + "\" (Bs " + p.getPrecio() + ")", NivelBitacora.INFO);
        avisarCambioDeMenu(p);
        return aRespuesta(p);
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        Producto p = buscar(id);
        BigDecimal precioAnterior = p.getPrecio();
        aplicar(p, req);
        if (precioAnterior.compareTo(p.getPrecio()) != 0) {
            auditoria.registrar(TipoEvento.AJUSTE_PRECIO, "Menú", "Ajustó el precio de " + p.getNombre() + " de Bs "
                    + precioAnterior.stripTrailingZeros().toPlainString() + " a Bs " + p.getPrecio().stripTrailingZeros().toPlainString(), NivelBitacora.ALERTA);
        } else {
            auditoria.registrar(TipoEvento.MODIFICACION, "Menú", "Modificó el producto \"" + p.getNombre() + "\"", NivelBitacora.INFO);
        }
        avisarCambioDeMenu(p);
        return aRespuesta(p);
    }

    @Transactional
    public ProductoResponse cambiarDisponibilidad(Long id, boolean disponible) {
        Producto p = buscar(id);
        p.setDisponible(disponible);
        p.setActualizadoEn(LocalDateTime.now());
        auditoria.registrar(TipoEvento.MODIFICACION, "Menú", "Cambió la disponibilidad de \"" + p.getNombre() + "\" a "
                + (disponible ? "Disponible" : "Agotado"), NivelBitacora.INFO);
        avisarCambioDeMenu(p);
        return aRespuesta(p);
    }

    @Transactional
    public ProductoResponse cambiarPublicacion(Long id, boolean publicado) {
        Producto p = buscar(id);
        p.setPublicadoPedidosYa(publicado);
        p.setActualizadoEn(LocalDateTime.now());
        auditoria.registrar(TipoEvento.MODIFICACION, "Menú", (publicado ? "Publicó" : "Retiró") + " \"" + p.getNombre() + "\" del menú de PedidosYA", NivelBitacora.INFO);
        avisarCambioDeMenu(p);
        return aRespuesta(p);
    }

    /** Baja lógica: el producto deja de mostrarse pero se conserva el historial de ventas. */
    @Transactional
    public void eliminar(Long id) {
        Producto p = buscar(id);
        p.setActivo(false);
        p.setActualizadoEn(LocalDateTime.now());
        auditoria.registrar(TipoEvento.ELIMINACION, "Menú", "Eliminó el producto \"" + p.getNombre() + "\" del menú", NivelBitacora.ALERTA);
        avisarCambioDeMenu(p);
    }

    private void aplicar(Producto p, ProductoRequest req) {
        CategoriaProducto categoria = categorias.findByCodigo(req.categoria())
                .orElseThrow(() -> ApiException.solicitudInvalida("La categoría \"" + req.categoria() + "\" no existe"));
        p.setNombre(req.nombre().trim());
        p.setDescripcion(req.descripcion() == null || req.descripcion().isBlank() ? null : req.descripcion().trim());
        p.setPrecio(req.precio());
        p.setCategoria(categoria);
        p.setImagenUrl(req.imagenUrl() == null || req.imagenUrl().isBlank() ? null : req.imagenUrl().trim());
        if (req.disponible() != null) p.setDisponible(req.disponible());
        if (req.publicadoPedidosYa() != null) p.setPublicadoPedidosYa(req.publicadoPedidosYa());
        if (req.enSalon() != null) p.setEnSalon(req.enSalon());
        if (req.costo() != null) p.setCosto(req.costo());
        p.setActualizadoEn(LocalDateTime.now());
    }

    private Producto buscar(Long id) {
        return productos.findByIdAndActivoTrue(id).orElseThrow(() -> ApiException.noEncontrado("Producto no encontrado"));
    }

    private void avisarCambioDeMenu(Producto p) {
        eventos.publishEvent(new EventoOperativo(EventoOperativo.CANAL_PUBLICO, "menu-actualizado",
                Map.of("idProducto", p.getId() == null ? 0L : p.getId())));
    }

    private ProductoResponse aRespuesta(Producto p) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getCosto(), p.getCategoria().getCodigo(),
                p.getCategoria().getNombre(), p.getImagenUrl(), p.isDisponible(), p.isPublicadoPedidosYa(), p.isEnSalon());
    }
}
