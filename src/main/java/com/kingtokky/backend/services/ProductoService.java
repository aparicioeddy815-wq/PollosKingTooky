package com.kingtokky.backend.services;

import com.kingtokky.backend.dto.ProductoDto;
import com.kingtokky.backend.entities.Producto;
import com.kingtokky.backend.repositories.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    public List<ProductoDto> listarProductos() {
        return productoRepository.findAll().stream()
                .map(ProductoDto::new)
                .collect(Collectors.toList());
    }

    public ProductoDto guardarProducto(ProductoDto productoDto) {
        Producto producto = new Producto();
        producto.setNombre(productoDto.getNombre());
        producto.setDescripcion(productoDto.getDescripcion());
        producto.setPrecio(productoDto.getPrecio());
        producto.setIdCategoria(productoDto.getIdCategoria());
        producto.setDisponibleLocal(productoDto.getDisponibleLocal());
        producto.setDisponiblePedidosya(productoDto.getDisponiblePedidosya());
        producto.setImagenUrl(productoDto.getImagenUrl());

        Producto guardado = productoRepository.save(producto);
        return new ProductoDto(guardado);
    }
}