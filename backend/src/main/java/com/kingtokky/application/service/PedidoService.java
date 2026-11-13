package com.kingtokky.application.service;

import com.kingtokky.application.dto.PedidoDtos.ComandasResponse;
import com.kingtokky.application.dto.PedidoDtos.DetallePedidoResponse;
import com.kingtokky.application.dto.PedidoDtos.ItemPedidoRequest;
import com.kingtokky.application.dto.PedidoDtos.PedidoRequest;
import com.kingtokky.application.dto.PedidoDtos.PedidoResponse;
import com.kingtokky.application.evento.EventoOperativo;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.menu.Producto;
import com.kingtokky.domain.model.pedido.CanalPedido;
import com.kingtokky.domain.model.pedido.ContadorTurno;
import com.kingtokky.domain.model.pedido.DetallePedido;
import com.kingtokky.domain.model.pedido.EstadoPedido;
import com.kingtokky.domain.model.pedido.Pedido;
import com.kingtokky.domain.model.reserva.Reserva;
import com.kingtokky.domain.model.seguridad.Usuario;
import com.kingtokky.domain.regla.ReglaEstadoPedido;
import com.kingtokky.domain.repository.ContadorTurnoRepository;
import com.kingtokky.domain.repository.DetallePedidoRepository;
import com.kingtokky.domain.repository.PedidoRepository;
import com.kingtokky.domain.repository.ProductoRepository;
import com.kingtokky.domain.repository.UsuarioRepository;
import com.kingtokky.presentation.exception.ApiException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Comandas: registro con número de turno diario, ciclo de estados, avisos en tiempo real y descuento de insumos. */
@Service
@Transactional(readOnly = true)
public class PedidoService {

    /** Línea ya validada que se convertirá en detalle del pedido. */
    private record Linea(Producto producto, int cantidad, BigDecimal precio, String observaciones) {}

    private final PedidoRepository pedidos;
    private final DetallePedidoRepository detalles;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final ContadorTurnoRepository contadores;
    private final InventarioService inventario;
    private final AuditoriaService auditoria;
    private final ApplicationEventPublisher eventos;

    public PedidoService(PedidoRepository pedidos, DetallePedidoRepository detalles, ProductoRepository productos,
                         UsuarioRepository usuarios, ContadorTurnoRepository contadores, InventarioService inventario,
                         AuditoriaService auditoria, ApplicationEventPublisher eventos) {
        this.pedidos = pedidos;
        this.detalles = detalles;
        this.productos = productos;
        this.usuarios = usuarios;
        this.contadores = contadores;
        this.inventario = inventario;
        this.auditoria = auditoria;
        this.eventos = eventos;
    }

    // ------------------------------------------------------------------ consultas

    public List<PedidoResponse> listar(LocalDate fecha, EstadoPedido estado) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        List<Pedido> lista = estado == null ? pedidos.findByFechaTurnoOrderByNumeroTurnoDesc(dia)
                : pedidos.findByFechaTurnoAndEstadoOrderByNumeroTurnoDesc(dia, estado);
        return aRespuestas(lista);
    }

    public PedidoResponse obtener(Long id) {
        return aRespuestas(List.of(buscar(id))).get(0);
    }

    /** Tablero de despacho: lo que está en cocina y lo que ya espera al cliente. */
    public ComandasResponse comandas() {
        LocalDate hoy = LocalDate.now();
        List<PedidoResponse> vivos = aRespuestas(pedidos.findByFechaTurnoAndEstadoInOrderByNumeroTurnoAsc(
                hoy, List.of(EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO)));
        List<PedidoResponse> enPreparacion = vivos.stream().filter(p -> p.estado() == EstadoPedido.EN_PREPARACION).toList();
        List<PedidoResponse> listos = vivos.stream().filter(p -> p.estado() == EstadoPedido.LISTO).toList();
        return new ComandasResponse(enPreparacion, listos, enPreparacion.size(), tiempoPromedioDespachoMin(hoy, hoy));
    }

    public double tiempoPromedioDespachoMin(LocalDate desde, LocalDate hasta) {
        List<Object[]> filas = pedidos.tiemposDeDespacho(desde, hasta);
        if (filas.isEmpty()) return 0;
        double minutos = filas.stream().mapToLong(f -> Duration.between((LocalDateTime) f[0], (LocalDateTime) f[1]).toSeconds()).average().orElse(0) / 60.0;
        return Math.round(minutos * 10) / 10.0;
    }

    // ------------------------------------------------------------------ operaciones

    @Transactional
    public PedidoResponse crear(PedidoRequest req, Long idRegistrador) {
        if (req.tipoPedido() == CanalPedido.RESERVA) {
            throw ApiException.solicitudInvalida("Los pedidos de reserva se generan al cobrar la reserva en caja");
        }
        List<Linea> lineas = new ArrayList<>();
        for (ItemPedidoRequest item : req.items()) {
            Producto p = productos.findByIdAndActivoTrue(item.idProducto())
                    .orElseThrow(() -> ApiException.noEncontrado("El producto " + item.idProducto() + " no existe"));
            if (!p.isDisponible()) {
                throw ApiException.reglaDeNegocio("\"" + p.getNombre() + "\" está agotado y no se puede registrar");
            }
            lineas.add(new Linea(p, item.cantidad(), p.getPrecio(), item.observaciones()));
        }
        Usuario cliente = req.idUsuarioCliente() == null ? null
                : usuarios.findById(req.idUsuarioCliente()).orElseThrow(() -> ApiException.noEncontrado("El cliente indicado no existe"));
        Usuario registrador = usuarios.findById(idRegistrador).orElse(null);
        return aRespuestas(List.of(guardar(req.tipoPedido(), cliente, registrador, null, lineas))).get(0);
    }

    /** Al cobrar una reserva en caja, sus platos pasan a cocina como una comanda más. */
    @Transactional
    public Pedido crearDesdeReserva(Reserva reserva, List<com.kingtokky.domain.model.reserva.ReservaDetalle> items, Usuario cobrador) {
        List<Linea> lineas = items.stream()
                .map(d -> new Linea(d.getProducto(), d.getCantidad(), d.getPrecioUnitario(), null)).toList();
        return guardar(CanalPedido.RESERVA, reserva.getCliente(), cobrador, reserva, lineas);
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevo) {
        Pedido p = buscar(id);
        if (!ReglaEstadoPedido.puedePasar(p.getEstado(), nuevo)) {
            throw ApiException.reglaDeNegocio("Un pedido " + etiqueta(p.getEstado()) + " no puede pasar a " + etiqueta(nuevo));
        }
        LocalDateTime ahora = LocalDateTime.now();
        switch (nuevo) {
            case LISTO -> p.setListoEn(ahora);
            case ENTREGADO -> p.setEntregadoEn(ahora);
            case CANCELADO -> p.setCanceladoEn(ahora);
            default -> { }
        }
        p.setEstado(nuevo);
        eventos.publishEvent(new EventoOperativo(EventoOperativo.CANAL_COMANDAS, nuevo == EstadoPedido.LISTO ? "pedido-listo" : "pedido-actualizado", resumenEvento(p)));
        eventos.publishEvent(new EventoOperativo(EventoOperativo.CANAL_PUBLICO, "turnos-actualizados", Map.of("numeroTurno", p.getNumeroTurno())));
        return aRespuestas(List.of(p)).get(0);
    }

    @Transactional
    public PedidoResponse cancelar(Long id, String motivo) {
        Pedido p = buscar(id);
        if (p.getEstado() != EstadoPedido.EN_PREPARACION) {
            throw ApiException.reglaDeNegocio("Solo se puede anular una comanda que está en preparación");
        }
        String razon = motivo == null || motivo.isBlank() ? "Anulada desde despacho" : motivo.trim();
        p.setMotivoCancelacion(razon);
        PedidoResponse respuesta = cambiarEstado(id, EstadoPedido.CANCELADO);
        auditoria.registrar(TipoEvento.ANULACION_COMANDA, "Despacho", "Anuló la comanda #" + p.getNumeroTurno() + ": " + razon, NivelBitacora.ALERTA);
        return respuesta;
    }

    /** Suena el timbre para avisar al cliente que su pedido está listo. */
    public PedidoResponse timbre(Long id) {
        Pedido p = buscar(id);
        if (p.getEstado() != EstadoPedido.LISTO) {
            throw ApiException.reglaDeNegocio("Solo se puede tocar el timbre para pedidos listos");
        }
        eventos.publishEvent(new EventoOperativo(EventoOperativo.CANAL_COMANDAS, "timbre", resumenEvento(p)));
        eventos.publishEvent(new EventoOperativo(EventoOperativo.CANAL_PUBLICO, "timbre", Map.of("numeroTurno", p.getNumeroTurno())));
        return aRespuestas(List.of(p)).get(0);
    }

    // ------------------------------------------------------------------ internos

    private Pedido guardar(CanalPedido canal, Usuario cliente, Usuario registrador, Reserva reserva, List<Linea> lineas) {
        LocalDate hoy = LocalDate.now();
        Pedido p = new Pedido();
        p.setFechaTurno(hoy);
        p.setNumeroTurno(siguienteTurno(hoy));
        p.setCanal(canal);
        p.setCliente(cliente);
        p.setRegistradoPor(registrador);
        p.setReserva(reserva);
        p.setTotal(BigDecimal.ZERO);
        pedidos.save(p);

        List<DetallePedido> guardados = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Linea l : lineas) {
            DetallePedido d = new DetallePedido();
            d.setPedido(p);
            d.setProducto(l.producto());
            d.setCantidad(l.cantidad());
            d.setPrecioUnitario(l.precio());
            d.setSubtotal(l.precio().multiply(BigDecimal.valueOf(l.cantidad())));
            d.setObservaciones(l.observaciones() == null || l.observaciones().isBlank() ? null : l.observaciones().trim());
            guardados.add(detalles.save(d));
            total = total.add(d.getSubtotal());
        }
        p.setTotal(total);
        inventario.consumirPorPedido(p, guardados);
        eventos.publishEvent(new EventoOperativo(EventoOperativo.CANAL_COMANDAS, "pedido-creado", resumenEvento(p)));
        eventos.publishEvent(new EventoOperativo(EventoOperativo.CANAL_PUBLICO, "turnos-actualizados", Map.of("numeroTurno", p.getNumeroTurno())));
        return p;
    }

    /** Número de turno que reinicia cada día; la fila del día se bloquea para que dos cajas no repitan número. */
    private int siguienteTurno(LocalDate fecha) {
        ContadorTurno contador = contadores.buscarParaActualizar(fecha).orElseGet(() -> {
            ContadorTurno nuevo = new ContadorTurno();
            nuevo.setFecha(fecha);
            nuevo.setUltimo(0);
            return contadores.saveAndFlush(nuevo);
        });
        contador.setUltimo(contador.getUltimo() + 1);
        return contador.getUltimo();
    }

    private Pedido buscar(Long id) {
        return pedidos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Pedido no encontrado"));
    }

    private Map<String, Object> resumenEvento(Pedido p) {
        Map<String, Object> m = new HashMap<>();
        m.put("idPedido", p.getId());
        m.put("numeroTurno", p.getNumeroTurno());
        m.put("estado", p.getEstado().name());
        return m;
    }

    private String etiqueta(EstadoPedido e) {
        return e.name().toLowerCase().replace('_', ' ');
    }

    private List<PedidoResponse> aRespuestas(List<Pedido> lista) {
        if (lista.isEmpty()) return List.of();
        Map<Long, List<DetallePedido>> porPedido = detalles.findByPedido_IdIn(lista.stream().map(Pedido::getId).toList()).stream()
                .collect(Collectors.groupingBy(d -> d.getPedido().getId()));
        LocalDateTime ahora = LocalDateTime.now();
        return lista.stream().map(p -> {
            boolean despachado = p.getListoEn() != null;
            long minutos = Duration.between(p.getCreadoEn(), despachado ? p.getListoEn() : ahora).toMinutes();
            List<DetallePedidoResponse> items = porPedido.getOrDefault(p.getId(), List.of()).stream()
                    .map(d -> new DetallePedidoResponse(d.getProducto().getId(), d.getProducto().getNombre(), d.getCantidad(),
                            d.getPrecioUnitario(), d.getSubtotal(), d.getObservaciones())).toList();
            return new PedidoResponse(p.getId(), p.getNumeroTurno(), p.getFechaTurno(), p.getEstado(), p.getCanal(), p.getTotal(),
                    p.getCreadoEn(), p.getCreadoEn(), p.getListoEn(), p.getEntregadoEn(), minutos,
                    p.getCliente() == null ? null : p.getCliente().getNombreCompleto(),
                    p.getReserva() == null ? null : p.getReserva().getCodigo(), items);
        }).toList();
    }
}
