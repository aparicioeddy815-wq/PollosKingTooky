package com.kingtokky.backend.dto;

import com.kingtokky.backend.entities.Inventario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDto {

    private Integer idInsumo;
    private String nombreInsumo;
    private String categoriaInsumo;
    private BigDecimal stockActual;
    private BigDecimal umbralMinimo;
    private BigDecimal consumoDia;
    private String unidadMedida;
    private LocalDateTime ultimaActualizacion;

    public InventarioDto(Inventario inventario) {
        this.idInsumo = inventario.getIdInsumo();
        this.nombreInsumo = inventario.getNombreInsumo();
        this.categoriaInsumo = inventario.getCategoriaInsumo();
        this.stockActual = inventario.getStockActual();
        this.umbralMinimo = inventario.getUmbralMinimo();
        this.consumoDia = inventario.getConsumoDia();
        this.unidadMedida = inventario.getUnidadMedida();
        this.ultimaActualizacion = inventario.getUltimaActualizacion();
    }
}