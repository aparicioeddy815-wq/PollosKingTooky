package com.kingtokky.application.service;

import com.kingtokky.application.dto.ReservaDtos.DiaReserva;
import com.kingtokky.application.dto.ReservaDtos.HoraReserva;
import com.kingtokky.application.dto.ReservaDtos.ItemReservaRequest;
import com.kingtokky.application.dto.ReservaDtos.ItemReservaResponse;
import com.kingtokky.application.dto.ReservaDtos.ProximaVisita;
import com.kingtokky.application.dto.ReservaDtos.ReprogramarRequest;
import com.kingtokky.application.dto.ReservaDtos.ReservaRequest;
import com.kingtokky.application.dto.ReservaDtos.ReservaResponse;
import com.kingtokky.application.dto.ReservaDtos.ResumenReservasResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.menu.Producto;
import com.kingtokky.domain.model.reserva.EstadoReserva;
import com.kingtokky.domain.model.reserva.Reserva;
import com.kingtokky.domain.model.reserva.ReservaDetalle;
import com.kingtokky.domain.model.seguridad.Usuario;
import com.kingtokky.domain.port.NotificadorCorreo;
import com.kingtokky.domain.regla.ReglasReserva;
import com.kingtokky.domain.repository.ProductoRepository;
import com.kingtokky.domain.repository.ReservaDetalleRepository;
import com.kingtokky.domain.repository.ReservaRepository;
import com.kingtokky.domain.repository.UsuarioRepository;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import com.kingtokky.infrastructure.export.ExportadorPdf;
import com.kingtokky.infrastructure.security.UsuarioAutenticado;
import com.kingtokky.presentation.exception.ApiException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reservas en tres pasos (fecha, hora y platos). El pago se hace en caja: se registra con {@link #pagar},
 * lo que envía la comanda a cocina; si la hora llega sin pago, la reserva se cancela sola.
 */
@Service
@Transactional(readOnly = true)
public class ReservaService {

    private static final Set<EstadoReserva> ESTADOS_QUE_OCUPAN = EnumSet.of(EstadoReserva.PENDIENTE_PAGO, EstadoReserva.PAGADA);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale ES = Locale.forLanguageTag("es-BO");

    private final ReservaRepository reservas;
    private final ReservaDetalleRepository detalles;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final PedidoService pedidoService;
    private final ConfiguracionService config;
    private final AuditoriaService auditoria;
    private final NotificadorCorreo correo;
    private final ExportadorPdf pdf;
    private final KingTokkyProperties propiedades;

    public ReservaService(ReservaRepository reservas, ReservaDetalleRepository detalles, ProductoRepository productos,
                          UsuarioRepository usuarios, PedidoService pedidoService, ConfiguracionService config,
                          AuditoriaService auditoria, NotificadorCorreo correo, ExportadorPdf pdf, KingTokkyProperties propiedades) {
        this.reservas = reservas;
        this.detalles = detalles;
        this.productos = productos;
        this.usuarios = usuarios;
        this.pedidoService = pedidoService;
        this.config = config;
        this.auditoria = auditoria;
        this.correo = correo;
        this.pdf = pdf;
        this.propiedades = propiedades;
    }

    // ------------------------------------------------------------------ asistente de reserva

    /** Paso 1: los días de la semana en los que todavía se puede reservar. */
    public List<DiaReserva> diasDisponibles() {
        LocalDate hoy = LocalDate.now();
        List<DiaReserva> dias = new ArrayList<>();
        for (int i = 0; i <= propiedades.reservas().diasMaxAnticipacion(); i++) {
            LocalDate fecha = hoy.plusDays(i);
            String nombre = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, ES);
            String etiqueta = i == 0 ? "Hoy" : i == 1 ? "Mañana" : nombre;
            dias.add(new DiaReserva(fecha, nombre, etiqueta, quedanHorasPorReservar(fecha)));
        }
        return dias;
    }

    /** Comprobación liviana (sin consultar cupos): ¿queda alguna hora futura para reservar ese día? */
    private boolean quedanHorasPorReservar(LocalDate fecha) {
        LocalDateTime minimo = LocalDateTime.now().plusMinutes(propiedades.reservas().anticipacionMinimaMin());
        return franjas().stream().anyMatch(h -> !ReglasReserva.fechaHora(fecha, h).isBefore(minimo));
    }

    /** Paso 2: horas del día elegido con su cupo restante. */
    public List<HoraReserva> horariosDelDia(LocalDate fecha) {
        validarRangoDeFecha(fecha);
        int aforo = config.entero("aforo_reservas", 40);
        LocalDateTime minimo = LocalDateTime.now().plusMinutes(propiedades.reservas().anticipacionMinimaMin());
        List<HoraReserva> horas = new ArrayList<>();
        for (LocalTime h : franjas()) {
            int ocupadas = personasEnFranja(fecha, h);
            int cupo = Math.max(0, aforo - ocupadas);
            boolean futura = !ReglasReserva.fechaHora(fecha, h).isBefore(minimo);
            horas.add(new HoraReserva(h, ReglasReserva.turno(h), cupo, futura && cupo > 0));
        }
        return horas;
    }

    // ------------------------------------------------------------------ cliente

    @Transactional
    public ReservaResponse crear(ReservaRequest req, Long idCliente) {
        Usuario cliente = usuarios.findById(idCliente).orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
        validarRangoDeFecha(req.fecha());
        validarHora(req.hora());
        LocalDateTime fechaHora = ReglasReserva.fechaHora(req.fecha(), req.hora());
        if (fechaHora.isBefore(LocalDateTime.now().plusMinutes(propiedades.reservas().anticipacionMinimaMin()))) {
            throw ApiException.reglaDeNegocio("Debes reservar con al menos " + propiedades.reservas().anticipacionMinimaMin() + " minutos de anticipación");
        }
        validarCupo(req.fecha(), req.hora(), req.personas(), 0);
        if (reservas.countByCliente_IdUsuarioAndEstado(idCliente, EstadoReserva.PENDIENTE_PAGO) >= propiedades.reservas().maxPendientesPorCliente()) {
            throw ApiException.reglaDeNegocio("Ya tienes " + propiedades.reservas().maxPendientesPorCliente()
                    + " reservas pendientes de pago. Paga o cancela alguna antes de crear otra");
        }

        Reserva r = new Reserva();
        LocalDate hoy = LocalDate.now();
        r.setCodigo(ReglasReserva.codigo(hoy, reservas.countByCodigoStartingWith(ReglasReserva.prefijoCodigo(hoy)) + 1));
        r.setCliente(cliente);
        r.setNombreCliente(req.nombreCliente().trim());
        r.setTelefono(req.telefono().trim());
        r.setCorreo(req.correo() == null || req.correo().isBlank() ? null : req.correo().trim());
        r.setFechaEncuentro(req.fecha());
        r.setHora(req.hora());
        r.setPersonas(req.personas());
        r.setNotas(req.notas() == null || req.notas().isBlank() ? null : req.notas().trim());
        r.setLimitePago(ReglasReserva.limitePago(req.fecha(), req.hora()));
        r.setTotal(BigDecimal.ZERO);
        reservas.save(r);

        List<ReservaDetalle> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ItemReservaRequest item : req.items()) {
            Producto p = productos.findByIdAndActivoTrue(item.idProducto())
                    .orElseThrow(() -> ApiException.noEncontrado("El producto " + item.idProducto() + " no existe"));
            if (!p.isDisponible()) {
                throw ApiException.reglaDeNegocio("\"" + p.getNombre() + "\" no está disponible por ahora");
            }
            ReservaDetalle d = new ReservaDetalle();
            d.setReserva(r);
            d.setProducto(p);
            d.setCantidad(item.cantidad());
            d.setPrecioUnitario(p.getPrecio());
            d.setSubtotal(p.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
            items.add(detalles.save(d));
            total = total.add(d.getSubtotal());
        }
        r.setTotal(total);
        auditoria.registrar(TipoEvento.RESERVA, "Reservas", "Creó la reserva " + r.getCodigo() + " para el " + r.getFechaEncuentro() + " a las "
                + r.getHora().format(HORA) + " (" + r.getPersonas() + " personas)", NivelBitacora.INFO);
        if (r.getCorreo() != null) {
            correo.enviar(r.getCorreo(), "Reserva " + r.getCodigo() + " registrada en King Tokky",
                    "Hola " + r.getNombreCliente() + ",\n\nTu reserva " + r.getCodigo() + " para el " + r.getFechaEncuentro() + " a las "
                            + r.getHora().format(HORA) + " fue registrada. Total: Bs " + total + ".\n\n"
                            + ReglasReserva.advertenciaPago(r.getHora()) + "\n\nKing Tokky");
        }
        return aRespuestas(List.of(r), Map.of(r.getId(), items)).get(0);
    }

    public PaginaResponse<ReservaResponse> misReservas(Long idCliente, EstadoReserva estado, int pagina, int tamano) {
        var page = reservas.misReservas(idCliente, estado == null, estado == null ? EstadoReserva.PENDIENTE_PAGO : estado,
                PageRequest.of(pagina, Math.min(tamano, 50)));
        return convertirPagina(page);
    }

    public ReservaResponse obtener(Long id, UsuarioAutenticado usuario) {
        Reserva r = buscar(id);
        verificarAcceso(r, usuario);
        return aRespuestas(List.of(r), null).get(0);
    }

    public ResumenReservasResponse resumen(Long idCliente) {
        LocalDate hoy = LocalDate.now();
        List<Reserva> proximas = reservas.proximas(idCliente, ESTADOS_QUE_OCUPAN, hoy, PageRequest.of(0, 5));
        LocalDateTime ahora = LocalDateTime.now();
        Reserva proxima = proximas.stream().filter(r -> !ReglasReserva.fechaHora(r.getFechaEncuentro(), r.getHora()).isBefore(ahora)).findFirst().orElse(null);
        String aviso = null;
        if (proxima != null && proxima.getFechaEncuentro().equals(hoy)) {
            aviso = proxima.getEstado() == EstadoReserva.PENDIENTE_PAGO
                    ? "Tienes una reserva hoy a las " + proxima.getHora().format(HORA) + ". " + ReglasReserva.advertenciaPago(proxima.getHora())
                    : "Tu reserva de hoy a las " + proxima.getHora().format(HORA) + " ya está pagada. ¡Te esperamos!";
        }
        return new ResumenReservasResponse(reservas.countByCliente_IdUsuario(idCliente),
                reservas.countByCliente_IdUsuarioAndEstado(idCliente, EstadoReserva.PENDIENTE_PAGO),
                proxima == null ? null : new ProximaVisita(proxima.getId(), proxima.getCodigo(), proxima.getFechaEncuentro(), proxima.getHora(),
                        proxima.getPersonas(), proxima.getEstado()), aviso);
    }

    @Transactional
    public ReservaResponse cancelar(Long id, UsuarioAutenticado usuario) {
        Reserva r = buscar(id);
        verificarAcceso(r, usuario);
        LocalDateTime fechaHora = ReglasReserva.fechaHora(r.getFechaEncuentro(), r.getHora());
        if (!ReglasReserva.puedeCancelar(r.getEstado(), fechaHora, LocalDateTime.now())) {
            throw ApiException.reglaDeNegocio("Esta reserva ya no se puede cancelar");
        }
        boolean porPersonal = !r.getCliente().getIdUsuario().equals(usuario.idUsuario());
        r.setEstado(EstadoReserva.CANCELADA);
        r.setCanceladaEn(LocalDateTime.now());
        r.setMotivoCancelacion(porPersonal ? "Cancelada por el personal" : "Cancelada por el cliente");
        auditoria.registrar(TipoEvento.RESERVA, "Reservas", "Canceló la reserva " + r.getCodigo(), NivelBitacora.ALERTA);
        return aRespuestas(List.of(r), null).get(0);
    }

    @Transactional
    public ReservaResponse reprogramar(Long id, ReprogramarRequest req, UsuarioAutenticado usuario) {
        Reserva r = buscar(id);
        verificarAcceso(r, usuario);
        LocalDateTime actual = ReglasReserva.fechaHora(r.getFechaEncuentro(), r.getHora());
        if (!ReglasReserva.puedeReprogramar(r.getEstado(), actual, LocalDateTime.now(), propiedades.reservas().limiteReprogramarMin())) {
            throw ApiException.reglaDeNegocio("Solo se puede reprogramar una reserva pendiente de pago, con al menos "
                    + propiedades.reservas().limiteReprogramarMin() + " minutos de anticipación");
        }
        validarRangoDeFecha(req.fecha());
        validarHora(req.hora());
        if (ReglasReserva.fechaHora(req.fecha(), req.hora()).isBefore(LocalDateTime.now().plusMinutes(propiedades.reservas().anticipacionMinimaMin()))) {
            throw ApiException.reglaDeNegocio("La nueva hora debe tener al menos " + propiedades.reservas().anticipacionMinimaMin() + " minutos de anticipación");
        }
        boolean mismaFranja = r.getFechaEncuentro().equals(req.fecha()) && r.getHora().getHour() == req.hora().getHour();
        validarCupo(req.fecha(), req.hora(), r.getPersonas(), mismaFranja ? r.getPersonas() : 0);
        r.setFechaEncuentro(req.fecha());
        r.setHora(req.hora());
        r.setLimitePago(ReglasReserva.limitePago(req.fecha(), req.hora()));
        r.setReprogramaciones(r.getReprogramaciones() + 1);
        auditoria.registrar(TipoEvento.RESERVA, "Reservas", "Reprogramó la reserva " + r.getCodigo() + " para el " + req.fecha() + " a las " + req.hora().format(HORA), NivelBitacora.INFO);
        return aRespuestas(List.of(r), null).get(0);
    }

    // ------------------------------------------------------------------ personal (caja / despacho / gerencia)

    public PaginaResponse<ReservaResponse> gestion(EstadoReserva estado, LocalDate fecha, int pagina, int tamano) {
        var page = reservas.gestion(estado == null, estado == null ? EstadoReserva.PENDIENTE_PAGO : estado,
                fecha == null, fecha == null ? LocalDate.now() : fecha, PageRequest.of(pagina, Math.min(tamano, 100)));
        return convertirPagina(page);
    }

    /** Cobro en caja: confirma la reserva y envía sus platos a cocina como comanda (canal RESERVA). */
    @Transactional
    public ReservaResponse pagar(Long id, Long idCobrador) {
        Reserva r = buscar(id);
        if (r.getEstado() != EstadoReserva.PENDIENTE_PAGO) {
            throw ApiException.reglaDeNegocio("La reserva no está pendiente de pago");
        }
        if (LocalDateTime.now().isAfter(r.getLimitePago())) {
            throw ApiException.reglaDeNegocio("La hora de la reserva ya pasó; el pago ya no se puede registrar");
        }
        Usuario cobrador = usuarios.findById(idCobrador).orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
        List<ReservaDetalle> items = detalles.findByReserva_Id(r.getId());
        r.setEstado(EstadoReserva.PAGADA);
        r.setPagadaEn(LocalDateTime.now());
        r.setCobradaPor(cobrador);
        pedidoService.crearDesdeReserva(r, items, cobrador);
        auditoria.registrar(TipoEvento.RESERVA, "Reservas", "Cobró en caja la reserva " + r.getCodigo() + " por Bs " + r.getTotal(), NivelBitacora.INFO);
        return aRespuestas(List.of(r), Map.of(r.getId(), items)).get(0);
    }

    @Transactional
    public ReservaResponse completar(Long id) {
        Reserva r = buscar(id);
        if (r.getEstado() != EstadoReserva.PAGADA) {
            throw ApiException.reglaDeNegocio("Solo se puede completar una reserva ya pagada");
        }
        r.setEstado(EstadoReserva.COMPLETADA);
        auditoria.registrar(TipoEvento.RESERVA, "Reservas", "Completó la reserva " + r.getCodigo(), NivelBitacora.INFO);
        return aRespuestas(List.of(r), null).get(0);
    }

    /** Tarea programada: cancela las reservas cuyo plazo de pago ya venció. Devuelve cuántas canceló. */
    @Transactional
    public int cancelarVencidas() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> vencidas = reservas.findByEstadoAndLimitePagoBefore(EstadoReserva.PENDIENTE_PAGO, ahora);
        for (Reserva r : vencidas) {
            r.setEstado(EstadoReserva.CANCELADA_AUTO);
            r.setCanceladaEn(ahora);
            r.setMotivoCancelacion("No se realizó el pago en caja a tiempo");
            auditoria.registrarSistema(TipoEvento.RESERVA, "Reservas", "Reserva " + r.getCodigo() + " cancelada automáticamente por falta de pago en caja", NivelBitacora.ALERTA);
        }
        return vencidas.size();
    }

    public byte[] recibo(Long id, UsuarioAutenticado usuario) {
        Reserva r = buscar(id);
        verificarAcceso(r, usuario);
        List<ReservaDetalle> items = detalles.findByReserva_Id(r.getId());
        List<List<Object>> datos = List.of(
                List.of("Código", r.getCodigo()), List.of("A nombre de", r.getNombreCliente()), List.of("Teléfono", r.getTelefono()),
                List.of("Fecha", r.getFechaEncuentro().toString()), List.of("Hora", r.getHora().format(HORA)),
                List.of("Personas", r.getPersonas()), List.of("Estado", r.getEstado().name().replace('_', ' ')),
                List.of("Total", "Bs " + r.getTotal()));
        List<List<Object>> filas = items.stream().map(d -> List.<Object>of(d.getProducto().getNombre(), d.getCantidad(),
                "Bs " + d.getPrecioUnitario(), "Bs " + d.getSubtotal())).toList();
        return pdf.documento("Recibo de reserva " + r.getCodigo(), ReglasReserva.advertenciaPago(r.getHora()), List.of(
                new ExportadorPdf.Seccion("Datos de la reserva", List.of("Campo", "Valor"), datos),
                new ExportadorPdf.Seccion("Platos reservados", List.of("Plato", "Cant.", "Precio", "Subtotal"), filas)));
    }

    // ------------------------------------------------------------------ internos

    private Reserva buscar(Long id) {
        return reservas.findById(id).orElseThrow(() -> ApiException.noEncontrado("Reserva no encontrada"));
    }

    private void verificarAcceso(Reserva r, UsuarioAutenticado usuario) {
        boolean esDueno = r.getCliente().getIdUsuario().equals(usuario.idUsuario());
        if (!esDueno && !usuario.tienePermiso("RESERVA_GESTIONAR")) {
            throw ApiException.prohibido("Esta reserva pertenece a otro cliente");
        }
    }

    private void validarRangoDeFecha(LocalDate fecha) {
        LocalDate hoy = LocalDate.now();
        if (fecha.isBefore(hoy) || fecha.isAfter(hoy.plusDays(propiedades.reservas().diasMaxAnticipacion()))) {
            throw ApiException.reglaDeNegocio("Solo se puede reservar desde hoy hasta " + propiedades.reservas().diasMaxAnticipacion() + " días adelante");
        }
    }

    private void validarHora(LocalTime hora) {
        if (!franjas().contains(hora)) {
            throw ApiException.reglaDeNegocio("La hora debe estar entre " + propiedades.reservas().horaInicio() + " y "
                    + propiedades.reservas().horaFin() + ", cada " + propiedades.reservas().pasoMinutos() + " minutos");
        }
    }

    private void validarCupo(LocalDate fecha, LocalTime hora, int personas, int personasPropias) {
        int aforo = config.entero("aforo_reservas", 40);
        int ocupadas = personasEnFranja(fecha, hora) - personasPropias;
        if (ocupadas + personas > aforo) {
            throw ApiException.reglaDeNegocio("No hay cupo suficiente para " + personas + " personas a esa hora. Cupo restante: " + Math.max(0, aforo - ocupadas));
        }
    }

    /** Personas ya reservadas dentro de la misma hora de reloj (por ejemplo, 12:00 y 12:30 comparten cupo). */
    private int personasEnFranja(LocalDate fecha, LocalTime hora) {
        LocalTime desde = LocalTime.of(hora.getHour(), 0);
        LocalTime hasta = desde.plusHours(1);
        Long total = reservas.personasEnFranja(fecha, desde, hasta, ESTADOS_QUE_OCUPAN);
        return total == null ? 0 : total.intValue();
    }

    private List<LocalTime> franjas() {
        LocalTime inicio = LocalTime.parse(propiedades.reservas().horaInicio());
        LocalTime fin = LocalTime.parse(propiedades.reservas().horaFin());
        List<LocalTime> horas = new ArrayList<>();
        for (LocalTime h = inicio; !h.isAfter(fin); h = h.plusMinutes(propiedades.reservas().pasoMinutos())) {
            horas.add(h);
        }
        return horas;
    }

    private PaginaResponse<ReservaResponse> convertirPagina(org.springframework.data.domain.Page<Reserva> page) {
        List<ReservaResponse> convertidas = aRespuestas(page.getContent(), null);
        return new PaginaResponse<>(convertidas, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private List<ReservaResponse> aRespuestas(List<Reserva> lista, Map<Long, List<ReservaDetalle>> conocidos) {
        if (lista.isEmpty()) return List.of();
        Map<Long, List<ReservaDetalle>> porReserva = conocidos != null ? conocidos
                : detalles.findByReserva_IdIn(lista.stream().map(Reserva::getId).toList()).stream()
                        .collect(Collectors.groupingBy(d -> d.getReserva().getId()));
        LocalDateTime ahora = LocalDateTime.now();
        return lista.stream().map(r -> {
            LocalDateTime fechaHora = ReglasReserva.fechaHora(r.getFechaEncuentro(), r.getHora());
            List<ItemReservaResponse> items = porReserva.getOrDefault(r.getId(), List.of()).stream()
                    .map(d -> new ItemReservaResponse(d.getProducto().getId(), d.getProducto().getNombre(), d.getCantidad(),
                            d.getPrecioUnitario(), d.getSubtotal())).toList();
            boolean pendiente = r.getEstado() == EstadoReserva.PENDIENTE_PAGO;
            return new ReservaResponse(r.getId(), r.getCodigo(), r.getNombreCliente(), r.getTelefono(), r.getCorreo(), r.getFechaEncuentro(),
                    r.getHora(), ReglasReserva.turno(r.getHora()), r.getPersonas(), r.getNotas(), r.getTotal(), r.getEstado(), r.getCreadoEn(),
                    r.getPagadaEn(), ReglasReserva.puedeCancelar(r.getEstado(), fechaHora, ahora),
                    ReglasReserva.puedeReprogramar(r.getEstado(), fechaHora, ahora, propiedades.reservas().limiteReprogramarMin()),
                    pendiente ? ReglasReserva.advertenciaPago(r.getHora()) : null, items);
        }).toList();
    }
}
