package com.kingtokky.application.service;

import com.kingtokky.application.dto.PublicoDtos.CategoriaMenu;
import com.kingtokky.application.dto.PublicoDtos.Horario;
import com.kingtokky.application.dto.PublicoDtos.InicioPublico;
import com.kingtokky.application.dto.PublicoDtos.MenuPublico;
import com.kingtokky.application.dto.PublicoDtos.ProductoPublico;
import com.kingtokky.application.dto.PublicoDtos.SobreNosotros;
import com.kingtokky.application.dto.PublicoDtos.TurnosPublico;
import com.kingtokky.application.dto.PublicoDtos.Ubicacion;
import com.kingtokky.domain.model.menu.CategoriaProducto;
import com.kingtokky.domain.model.menu.Producto;
import com.kingtokky.domain.model.pedido.EstadoPedido;
import com.kingtokky.domain.model.pedido.Pedido;
import com.kingtokky.domain.repository.CategoriaProductoRepository;
import com.kingtokky.domain.repository.HorarioAtencionRepository;
import com.kingtokky.domain.repository.PedidoRepository;
import com.kingtokky.domain.repository.ProductoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Información que ve cualquier visitante, sin iniciar sesión. */
@Service
@Transactional(readOnly = true)
public class PublicoService {

    private final ProductoRepository productos;
    private final CategoriaProductoRepository categorias;
    private final PedidoRepository pedidos;
    private final HorarioAtencionRepository horarios;
    private final ConfiguracionService config;

    public PublicoService(ProductoRepository productos, CategoriaProductoRepository categorias, PedidoRepository pedidos,
                          HorarioAtencionRepository horarios, ConfiguracionService config) {
        this.productos = productos;
        this.categorias = categorias;
        this.pedidos = pedidos;
        this.horarios = horarios;
        this.config = config;
    }

    public InicioPublico inicio() {
        return new InicioPublico(config.texto("nombre_local", "King Tokky"), masVendido(), listarHorarios(), ubicacion());
    }

    /** Solo se muestran los productos publicados en PedidosYA, indicando si hoy están disponibles. */
    public MenuPublico menu() {
        Map<Long, List<ProductoPublico>> porCategoria = new LinkedHashMap<>();
        for (Producto p : productos.findByActivoTrueAndPublicadoPedidosYaTrueOrderByCategoria_OrdenAscNombreAsc()) {
            porCategoria.computeIfAbsent(p.getCategoria().getId(), k -> new java.util.ArrayList<>()).add(aPublico(p));
        }
        List<CategoriaMenu> resultado = categorias.findAllByOrderByOrdenAsc().stream()
                .filter(c -> porCategoria.containsKey(c.getId()))
                .map(c -> aCategoria(c, porCategoria.get(c.getId())))
                .toList();
        return new MenuPublico(resultado, LocalDateTime.now());
    }

    public List<Horario> listarHorarios() {
        return horarios.findAllByOrderByOrdenAsc().stream().map(h -> new Horario(h.getDias(), h.getHorario())).toList();
    }

    public Ubicacion ubicacion() {
        return new Ubicacion(config.texto("direccion", ""), config.decimal("latitud", 0), config.decimal("longitud", 0));
    }

    public SobreNosotros sobreNosotros() {
        return new SobreNosotros(config.texto("nombre_local", "King Tokky"), config.texto("historia", ""));
    }

    public TurnosPublico turnos() {
        List<Pedido> vivos = pedidos.findByFechaTurnoAndEstadoInOrderByNumeroTurnoAsc(
                LocalDate.now(), List.of(EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO));
        List<Integer> enPreparacion = vivos.stream().filter(p -> p.getEstado() == EstadoPedido.EN_PREPARACION).map(Pedido::getNumeroTurno).toList();
        List<Integer> listos = vivos.stream().filter(p -> p.getEstado() == EstadoPedido.LISTO).map(Pedido::getNumeroTurno).toList();
        return new TurnosPublico(enPreparacion, listos, LocalDateTime.now());
    }

    private ProductoPublico masVendido() {
        List<Object[]> ranking = productos.masVendidos(LocalDate.now().minusDays(30), EstadoPedido.CANCELADO, PageRequest.of(0, 1));
        if (!ranking.isEmpty()) {
            Long id = ((Number) ranking.get(0)[0]).longValue();
            return productos.findById(id).map(this::aPublico).orElse(null);
        }
        return productos.findByActivoTrueAndPublicadoPedidosYaTrueOrderByCategoria_OrdenAscNombreAsc().stream().findFirst().map(this::aPublico).orElse(null);
    }

    private CategoriaMenu aCategoria(CategoriaProducto c, List<ProductoPublico> lista) {
        return new CategoriaMenu(c.getCodigo(), c.getNombre(), lista, c.getUrlPedidosYa());
    }

    public ProductoPublico aPublico(Producto p) {
        return new ProductoPublico(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getImagenUrl(), p.isDisponible());
    }
}
