package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.application.dto.ProductoRequest;
import com.kingtooky.backend.application.dto.ProductoResponse;
import com.kingtooky.backend.application.dto.RecetaItemRequest;
import com.kingtooky.backend.application.dto.RecetaItemResponse;
import com.kingtooky.backend.infrastructure.entity.InsumoEntity;
import com.kingtooky.backend.infrastructure.entity.ProductoEntity;
import com.kingtooky.backend.infrastructure.entity.ProductoInsumoEntity;
import com.kingtooky.backend.infrastructure.entity.ProductoInsumoId;
import com.kingtooky.backend.presentation.exception.RecursoNoEncontradoException;
import com.kingtooky.backend.presentation.exception.ReglaNegocioException;
import com.kingtooky.backend.repository.InsumoJpaRepository;
import com.kingtooky.backend.repository.ProductoInsumoJpaRepository;
import com.kingtooky.backend.repository.ProductoJpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductoService {

    private final ProductoJpaRepository productoRepository;
    private final ProductoInsumoJpaRepository recetaRepository;
    private final InsumoJpaRepository insumoRepository;
    private final AuditoriaService auditoriaService;

    public ProductoService(ProductoJpaRepository productoRepository, ProductoInsumoJpaRepository recetaRepository,
                           InsumoJpaRepository insumoRepository, AuditoriaService auditoriaService) {
        this.productoRepository = productoRepository;
        this.recetaRepository = recetaRepository;
        this.insumoRepository = insumoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> listar(String q, Boolean disponible, Pageable pageable) {
        return PageUtil.toResponse(
                productoRepository.buscar(PageUtil.texto(q), disponible != null, disponible != null && disponible, pageable),
                this::aRespuesta);
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Integer id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        String nombre = request.getNombre().trim();
        if (productoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe un producto con ese nombre");
        }
        ProductoEntity p = new ProductoEntity();
        aplicar(p, request);
        ProductoEntity guardado = productoRepository.save(p);
        auditoriaService.registrar("CREAR", "producto", guardado.getIdProducto(), "Producto creado: " + nombre);
        return aRespuesta(guardado);
    }

    @Transactional
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        ProductoEntity p = buscar(id);
        String nombre = request.getNombre().trim();
        if (productoRepository.existsByNombreIgnoreCaseAndIdProductoNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe un producto con ese nombre");
        }
        aplicar(p, request);
        ProductoEntity guardado = productoRepository.save(p);
        auditoriaService.registrar("EDITAR", "producto", id, "Producto modificado: " + nombre);
        return aRespuesta(guardado);
    }

    @Transactional
    public ProductoResponse cambiarDisponibilidad(Integer id, boolean disponible) {
        ProductoEntity p = buscar(id);
        p.setDisponible(disponible);
        ProductoEntity guardado = productoRepository.save(p);
        auditoriaService.registrar("DISPONIBILIDAD", "producto", id,
                p.getNombre() + (disponible ? " disponible" : " agotado"));
        return aRespuesta(guardado);
    }

    @Transactional
    public void eliminar(Integer id) {
        ProductoEntity p = buscar(id);
        productoRepository.delete(p);
        productoRepository.flush();
        auditoriaService.registrar("ELIMINAR", "producto", id, "Producto eliminado: " + p.getNombre());
    }

    @Transactional(readOnly = true)
    public List<RecetaItemResponse> obtenerReceta(Integer idProducto) {
        buscar(idProducto);
        return recetaRepository.findByProducto(idProducto).stream().map(this::recetaARespuesta).toList();
    }

    @Transactional
    public List<RecetaItemResponse> definirReceta(Integer idProducto, List<RecetaItemRequest> items) {
        ProductoEntity p = buscar(idProducto);
        Set<Integer> vistos = new HashSet<>();
        for (RecetaItemRequest it : items) {
            if (!vistos.add(it.getIdInsumo())) {
                throw new ReglaNegocioException("El insumo " + it.getIdInsumo() + " esta repetido en la receta");
            }
        }
        recetaRepository.eliminarPorProducto(idProducto);
        for (RecetaItemRequest it : items) {
            InsumoEntity insumo = insumoRepository.findById(it.getIdInsumo())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Insumo no encontrado: " + it.getIdInsumo()));
            ProductoInsumoEntity pi = new ProductoInsumoEntity();
            pi.setId(new ProductoInsumoId(idProducto, insumo.getIdInsumo()));
            pi.setInsumo(insumo);
            pi.setCantidadRequerida(it.getCantidadRequerida().setScale(3, RoundingMode.HALF_UP));
            recetaRepository.save(pi);
        }
        auditoriaService.registrar("RECETA", "producto", idProducto,
                "Receta de " + p.getNombre() + " actualizada (" + items.size() + " insumos)");
        return recetaRepository.findByProducto(idProducto).stream().map(this::recetaARespuesta).toList();
    }

    private ProductoEntity buscar(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id));
    }

    private void aplicar(ProductoEntity p, ProductoRequest request) {
        p.setNombre(request.getNombre().trim());
        p.setDescripcion(request.getDescripcion());
        p.setPrecio(request.getPrecio().setScale(2, RoundingMode.HALF_UP));
        if (request.getDisponible() != null) {
            p.setDisponible(request.getDisponible());
        } else if (p.getDisponible() == null) {
            p.setDisponible(true);
        }
    }

    private ProductoResponse aRespuesta(ProductoEntity p) {
        ProductoResponse r = new ProductoResponse();
        r.setIdProducto(p.getIdProducto());
        r.setNombre(p.getNombre());
        r.setDescripcion(p.getDescripcion());
        r.setPrecio(p.getPrecio());
        r.setDisponible(p.getDisponible());
        return r;
    }

    private RecetaItemResponse recetaARespuesta(ProductoInsumoEntity pi) {
        RecetaItemResponse r = new RecetaItemResponse();
        r.setIdInsumo(pi.getId().getIdInsumo());
        r.setNombreInsumo(pi.getInsumo().getNombre());
        r.setUnidadMedida(pi.getInsumo().getUnidadMedida());
        r.setCantidadRequerida(pi.getCantidadRequerida());
        return r;
    }
}