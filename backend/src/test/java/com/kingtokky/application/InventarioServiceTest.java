package com.kingtokky.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.kingtokky.application.dto.InventarioDtos.MermaRequest;
import com.kingtokky.application.service.InventarioService;
import com.kingtokky.domain.model.inventario.Insumo;
import com.kingtokky.domain.model.inventario.MotivoMerma;
import com.kingtokky.domain.repository.InsumoRepository;
import com.kingtokky.presentation.exception.ApiException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InsumoRepository insumos;

    @InjectMocks
    private InventarioService servicio;

    @Test
    void noPermiteRegistrarUnaMermaMayorAlStockDisponible() {
        Insumo pollo = new Insumo();
        pollo.setNombre("Pollo entero troceado");
        pollo.setUnidadMedida("kg");
        pollo.setStockActual(new BigDecimal("5.000"));
        when(insumos.buscarParaActualizar(1L)).thenReturn(Optional.of(pollo));

        MermaRequest pedido = new MermaRequest(1L, new BigDecimal("8.000"), MotivoMerma.VENCIMIENTO, null);

        assertThatThrownBy(() -> servicio.registrarMerma(pedido, 5L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("supera el stock disponible");
        assertThat(pollo.getStockActual()).isEqualByComparingTo("5.000");
    }
}
