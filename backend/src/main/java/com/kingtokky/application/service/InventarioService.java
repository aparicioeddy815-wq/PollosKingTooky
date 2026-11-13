package com.kingtokky.application.service;

import com.kingtokky.application.dto.InventarioDtos.AlertaResponse;
import com.kingtokky.application.dto.InventarioDtos.CategoriaInsumoResponse;
import com.kingtokky.application.dto.InventarioDtos.EntradaRequest;
import com.kingtokky.application.dto.InventarioDtos.InsumoResponse;
import com.kingtokky.application.dto.InventarioDtos.MermaRequest;
import com.kingtokky.application.dto.InventarioDtos.MermaResponse;
import com.kingtokky.application.dto.InventarioDtos.MovimientoResponse;
import com.kingtokky.application.dto.InventarioDtos.ProveedorResponse;
import com.kingtokky.application.dto.InventarioDtos.ResumenInventarioResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.inventario.AlertaStock;
import com.kingtokky.domain.model.inventario.EstadoAlerta;
import com.kingtokky.domain.model.inventario.Insumo;
import com.kingtokky.domain.model.inventario.Merma;
import com.kingtokky.domain.model.inventario.MovimientoInventario;
import com.kingtokky.domain.model.inventario.TipoInsumo;
import com.kingtokky.domain.model.inventario.TipoMovimiento;
import com.kingtokky.domain.model.menu.RecetaInsumo;
import com.kingtokky.domain.model.pedido.DetallePedido;
import com.kingtokky.domain.model.pedido.Pedido;
import com.kingtokky.domain.model.seguridad.Usuario;
import com.kingtokky.domain.regla.ProyeccionStock;
import com.kingtokky.domain.repository.AlertaStockRepository;
import com.kingtokky.domain.repository.CategoriaInsumoRepository;
import com.kingtokky.domain.repository.InsumoRepository;
import com.kingtokky.domain.repository.MermaRepository;
import com.kingtokky.domain.repository.MovimientoInventarioRepository;
import com.kingtokky.domain.repository.ProveedorRepository;
import com.kingtokky.domain.repository.RecetaInsumoRepository;
import com.kingtokky.domain.repository.UsuarioRepository;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import com.kingtokky.presentation.exception.ApiException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Existencias, kardex, entradas, mermas, alertas y proyección de consumo. */
@Service
@Transactional(readOnly = true)
public class InventarioService {

    private final InsumoRepository insumos;
    private final CategoriaInsumoRepository categorias;
    private final ProveedorRepository proveedores;
    private final MovimientoInventarioRepository movimientos;
    private final MermaRepository mermas;
    private final AlertaStockRepository alertas;
    private final RecetaInsumoRepository recetas;
    private final UsuarioRepository usuarios;
    private final AuditoriaService auditoria;
    private final KingTokkyProperties propiedades;

    public InventarioService(InsumoRepository insumos, CategoriaInsumoRepository categorias, ProveedorRepository proveedores,
                             MovimientoInventarioRepository movimientos, MermaRepository mermas, AlertaStockRepository alertas,
                             RecetaInsumoRepository recetas, UsuarioRepository usuarios, AuditoriaService auditoria,
                             KingTokkyProperties propiedades) {
        this.insumos = insumos;
        this.categorias = categorias;
        this.proveedores = proveedores;
        this.movimientos = movimientos;
        this.mermas = mermas;
        this.alertas = alertas;
        this.recetas = recetas;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.propiedades = propiedades;
    }

    // ------------------------------------------------------------------ consultas

    public List<InsumoResponse> listar(String q, Long idCategoria, TipoInsumo tipo, String estado) {
        String patron = q == null || q.isBlank() ? "" : "%" + q.trim().toLowerCase() + "%";
        boolean todas = idCategoria == null;
        Map<Long, BigDecimal> consumoHoy = consumoDeHoy();
        return insumos.buscar(tipo, patron, todas, todas ? 0L : idCategoria).stream()
                .map(i -> aRespuesta(i, consumoHoy.getOrDefault(i.getId(), BigDecimal.ZERO)))
                .filter(r -> estado == null || estado.isBlank() || r.estado().equalsIgnoreCase(estado))
                .toList();
    }

    public ResumenInventarioResponse resumen() {
        List<Insumo> lista = insumos.listarPorTipo(TipoInsumo.ALIMENTO);
        long criticos = 0, bajos = 0, abastecimiento = 0;
        for (Insumo i : lista) {
            ProyeccionStock.Resultado r = proyectar(i);
            if (ProyeccionStock.CRITICO.equals(r.estado())) criticos++;
            if (ProyeccionStock.BAJO.equals(r.estado())) bajos++;
            if (r.deficitProyectado().signum() > 0) abastecimiento++;
        }
        return new ResumenInventarioResponse(criticos, bajos, abastecimiento, insumos.valorInventario(TipoInsumo.ALIMENTO),
                criticos + bajos, lista.size(), propiedades.inventario().diasProyeccion());
    }

    public List<CategoriaInsumoResponse> listarCategorias(TipoInsumo tipo) {
        return categorias.findByTipoOrderByNombreAsc(tipo).stream()
                .map(c -> new CategoriaInsumoResponse(c.getId(), c.getNombre(), c.getTipo().name())).toList();
    }

    public List<ProveedorResponse> listarProveedores() {
        return proveedores.findAllByOrderByNombreAsc().stream()
                .map(p -> new ProveedorResponse(p.getId(), p.getNombre(), p.getTelefono(), p.getContacto())).toList();
    }

    public PaginaResponse<MovimientoResponse> kardex(Long idInsumo, int pagina, int tamano) {
        PageRequest pr = PageRequest.of(pagina, Math.min(tamano, 100));
        var page = idInsumo == null ? movimientos.findAllByOrderByFechaHoraDescIdDesc(pr)
                : movimientos.findByInsumo_IdOrderByFechaHoraDescIdDesc(idInsumo, pr);
        Map<Long, String> nombres = nombresDeUsuarios(page.getContent().stream().map(MovimientoInventario::getIdUsuario).collect(Collectors.toSet()));
        return PaginaResponse.de(page, m -> new MovimientoResponse(m.getId(), m.getInsumo().getId(), m.getInsumo().getNombre(),
                m.getInsumo().getUnidadMedida(), m.getTipo(), m.getVariacion(), m.getStockResultante(), m.getFechaHora(),
                m.getIdUsuario() == null ? "Sistema" : nombres.getOrDefault(m.getIdUsuario(), "—"), m.getIdPedido(),
                m.getProveedor() == null ? null : m.getProveedor().getNombre(), m.getDocumento(), m.getMotivo()));
    }

    public PaginaResponse<MermaResponse> historialMermas(LocalDate desde, LocalDate hasta, int pagina, int tamano) {
        var page = mermas.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio(desde, 30), fin(hasta), PageRequest.of(pagina, Math.min(tamano, 100)));
        Map<Long, String> nombres = nombresDeUsuarios(page.getContent().stream().map(Merma::getIdUsuario).collect(Collectors.toSet()));
        return PaginaResponse.de(page, m -> aMerma(m, nombres));
    }

    public List<MermaResponse> mermasParaExportar(LocalDate desde, LocalDate hasta) {
        List<Merma> lista = mermas.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio(desde, 30), fin(hasta));
        Map<Long, String> nombres = nombresDeUsuarios(lista.stream().map(Merma::getIdUsuario).collect(Collectors.toSet()));
        return lista.stream().map(m -> aMerma(m, nombres)).toList();
    }

    public List<AlertaResponse> alertasAbiertas() {
        return alertas.findByEstadoOrderByCreadaEnDesc(EstadoAlerta.ABIERTA).stream().map(a -> new AlertaResponse(a.getId(),
                a.getInsumo().getId(), a.getInsumo().getNombre(), a.getInsumo().getUnidadMedida(), a.getInsumo().getStockActual(),
                a.getUmbralMinimo(), a.getEstado().name(), a.getCreadaEn(),
                a.getInsumo().getNombre() + " está en " + a.getInsumo().getStockActual().stripTrailingZeros().toPlainString() + " "
                        + a.getInsumo().getUnidadMedida() + " (mínimo " + a.getUmbralMinimo().stripTrailingZeros().toPlainString() + ")")).toList();
    }

    // ------------------------------------------------------------------ operaciones

    @Transactional
    public InsumoResponse registrarEntrada(EntradaRequest req, Long idUsuario) {
        Insumo insumo = bloquear(req.idInsumo());
        BigDecimal nuevoStock = insumo.getStockActual().add(req.cantidad());
        insumo.setStockActual(nuevoStock);
        insumo.setActualizadoEn(LocalDateTime.now());
        if (req.costoUnitario() != null) insumo.setCostoUnitario(req.costoUnitario());

        MovimientoInventario m = nuevoMovimiento(insumo, TipoMovimiento.ENTRADA, req.cantidad(), idUsuario, "Entrada de mercadería");
        m.setCostoUnitario(req.costoUnitario());
        m.setDocumento(req.documento());
        if (req.idProveedor() != null) {
            m.setProveedor(proveedores.findById(req.idProveedor()).orElseThrow(() -> ApiException.noEncontrado("Proveedor no encontrado")));
        }
        movimientos.save(m);
        auditoria.registrar(TipoEvento.AJUSTE_STOCK, "Inventario", "Registró entrada de " + req.cantidad().stripTrailingZeros().toPlainString()
                + " " + insumo.getUnidadMedida() + " de " + insumo.getNombre(), NivelBitacora.INFO);
        evaluarAlerta(insumo);
        return aRespuesta(insumo, BigDecimal.ZERO);
    }

    @Transactional
    public MermaResponse registrarMerma(MermaRequest req, Long idUsuario) {
        Insumo insumo = bloquear(req.idInsumo());
        if (req.cantidad().compareTo(insumo.getStockActual()) > 0) {
            throw ApiException.reglaDeNegocio("La merma (" + req.cantidad().stripTrailingZeros().toPlainString() + ") supera el stock disponible ("
                    + insumo.getStockActual().stripTrailingZeros().toPlainString() + " " + insumo.getUnidadMedida() + ")");
        }
        insumo.setStockActual(insumo.getStockActual().subtract(req.cantidad()));
        insumo.setActualizadoEn(LocalDateTime.now());

        Merma merma = new Merma();
        merma.setInsumo(insumo);
        merma.setCantidad(req.cantidad());
        merma.setMotivo(req.motivo());
        merma.setObservacion(req.observacion());
        merma.setCostoTotal(req.cantidad().multiply(insumo.getCostoUnitario()).setScale(2, java.math.RoundingMode.HALF_UP));
        merma.setIdUsuario(idUsuario);
        mermas.save(merma);
        movimientos.save(nuevoMovimiento(insumo, TipoMovimiento.MERMA, req.cantidad().negate(), idUsuario,
                "Merma por " + req.motivo().name().toLowerCase().replace('_', ' ')));
        auditoria.registrar(TipoEvento.AJUSTE_STOCK, "Inventario", "Registró merma de " + req.cantidad().stripTrailingZeros().toPlainString() + " "
                + insumo.getUnidadMedida() + " de " + insumo.getNombre() + " (" + req.motivo().name().toLowerCase().replace('_', ' ') + ")", NivelBitacora.ALERTA);
        evaluarAlerta(insumo);
        return aMerma(merma, nombresDeUsuarios(Set.of(idUsuario)));
    }

    /** Descuenta del stock los insumos que consume una comanda según las recetas de sus productos. */
    @Transactional
    public void consumirPorPedido(Pedido pedido, List<DetallePedido> detalles) {
        Map<Long, Integer> unidades = new HashMap<>();
        detalles.forEach(d -> unidades.merge(d.getProducto().getId(), d.getCantidad(), Integer::sum));
        if (unidades.isEmpty()) return;
        Map<Long, BigDecimal> porInsumo = new TreeMap<>();   // ordenado por id para bloquear siempre en el mismo orden
        for (RecetaInsumo r : recetas.findByProducto_IdIn(unidades.keySet())) {
            BigDecimal cantidad = r.getCantidad().multiply(BigDecimal.valueOf(unidades.get(r.getProducto().getId())));
            porInsumo.merge(r.getInsumo().getId(), cantidad, BigDecimal::add);
        }
        for (Map.Entry<Long, BigDecimal> e : porInsumo.entrySet()) {
            Insumo insumo = bloquear(e.getKey());
            BigDecimal consumo = e.getValue().min(insumo.getStockActual()).setScale(3, java.math.RoundingMode.HALF_UP);
            if (consumo.signum() <= 0) continue;
            insumo.setStockActual(insumo.getStockActual().subtract(consumo));
            insumo.setActualizadoEn(LocalDateTime.now());
            MovimientoInventario m = nuevoMovimiento(insumo, TipoMovimiento.CONSUMO, consumo.negate(), null, "Consumo del pedido #" + pedido.getNumeroTurno());
            m.setIdPedido(pedido.getId());
            movimientos.save(m);
            evaluarAlerta(insumo);
        }
    }

    /** Crea o cierra alertas según el stock actual. Se invoca tras cada movimiento y de forma programada. */
    @Transactional
    public void evaluarAlerta(Insumo insumo) {
        boolean bajo = insumo.getStockActual().compareTo(insumo.getUmbralMinimo()) <= 0;
        var abierta = alertas.findFirstByInsumo_IdAndEstado(insumo.getId(), EstadoAlerta.ABIERTA);
        if (bajo && abierta.isEmpty()) {
            AlertaStock a = new AlertaStock();
            a.setInsumo(insumo);
            a.setStockActual(insumo.getStockActual());
            a.setUmbralMinimo(insumo.getUmbralMinimo());
            alertas.save(a);
            auditoria.registrarSistema(TipoEvento.AJUSTE_STOCK, "Inventario", "Alerta de stock: " + insumo.getNombre() + " quedó en "
                    + insumo.getStockActual().stripTrailingZeros().toPlainString() + " " + insumo.getUnidadMedida() + ", por debajo del mínimo", NivelBitacora.ALERTA);
        } else if (!bajo && abierta.isPresent()) {
            abierta.get().setEstado(EstadoAlerta.ATENDIDA);
            abierta.get().setAtendidaEn(LocalDateTime.now());
        }
    }

    @Transactional
    public void evaluarTodasLasAlertas() {
        insumos.listarPorTipo(TipoInsumo.ALIMENTO).forEach(this::evaluarAlerta);
    }

    /** Aplica a un insumo un ajuste (±) producto de un control físico y deja el movimiento en el kardex. */
    @Transactional
    public void ajustarPorControl(Long idInsumo, BigDecimal diferencia, Long idUsuario, String motivo) {
        Insumo insumo = bloquear(idInsumo);
        insumo.setStockActual(insumo.getStockActual().add(diferencia).max(BigDecimal.ZERO));
        insumo.setActualizadoEn(LocalDateTime.now());
        movimientos.save(nuevoMovimiento(insumo, TipoMovimiento.AJUSTE, diferencia, idUsuario, motivo));
        evaluarAlerta(insumo);
    }

    // ------------------------------------------------------------------ internos

    private Insumo bloquear(Long id) {
        return insumos.buscarParaActualizar(id).orElseThrow(() -> ApiException.noEncontrado("Insumo no encontrado"));
    }

    private MovimientoInventario nuevoMovimiento(Insumo insumo, TipoMovimiento tipo, BigDecimal variacion, Long idUsuario, String motivo) {
        MovimientoInventario m = new MovimientoInventario();
        m.setInsumo(insumo);
        m.setTipo(tipo);
        m.setVariacion(variacion);
        m.setStockResultante(insumo.getStockActual());
        m.setIdUsuario(idUsuario);
        m.setMotivo(motivo);
        return m;
    }

    private Map<Long, BigDecimal> consumoDeHoy() {
        Map<Long, BigDecimal> mapa = new HashMap<>();
        for (Object[] fila : movimientos.consumoDesde(TipoMovimiento.CONSUMO, LocalDate.now().atStartOfDay())) {
            mapa.put(((Number) fila[0]).longValue(), (BigDecimal) fila[1]);
        }
        return mapa;
    }

    private ProyeccionStock.Resultado proyectar(Insumo i) {
        return ProyeccionStock.calcular(i.getStockActual(), i.getUmbralMinimo(), i.getConsumoPromedioDiario(),
                propiedades.inventario().diasProyeccion(), propiedades.inventario().factorStockBajo());
    }

    private InsumoResponse aRespuesta(Insumo i, BigDecimal consumoHoy) {
        ProyeccionStock.Resultado r = proyectar(i);
        return new InsumoResponse(i.getId(), i.getCodigo(), i.getNombre(), i.getCategoria().getId(), i.getCategoria().getNombre(),
                i.getCategoria().getTipo().name(), i.getUnidadMedida(), i.getStockActual(), i.getUmbralMinimo(), consumoHoy,
                i.getConsumoPromedioDiario(), r.estado(), r.consumoProyectado(), r.deficitProyectado(), r.superavitSeguro(),
                i.getCostoUnitario(), i.getStockActual().multiply(i.getCostoUnitario()).setScale(2, java.math.RoundingMode.HALF_UP), i.getActualizadoEn());
    }

    private MermaResponse aMerma(Merma m, Map<Long, String> nombres) {
        return new MermaResponse(m.getId(), m.getInsumo().getId(), m.getInsumo().getNombre(), m.getInsumo().getUnidadMedida(), m.getCantidad(),
                m.getMotivo(), m.getObservacion(), m.getCostoTotal(), m.getFechaHora(),
                m.getIdUsuario() == null ? "Sistema" : nombres.getOrDefault(m.getIdUsuario(), "—"));
    }

    private Map<Long, String> nombresDeUsuarios(Set<Long> ids) {
        Set<Long> validos = ids.stream().filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        if (validos.isEmpty()) return Map.of();
        return usuarios.findAllById(validos).stream().collect(Collectors.toMap(Usuario::getIdUsuario, Usuario::getNombreCompleto));
    }

    private LocalDateTime inicio(LocalDate desde, int diasPorDefecto) {
        return (desde != null ? desde : LocalDate.now().minusDays(diasPorDefecto)).atStartOfDay();
    }

    private LocalDateTime fin(LocalDate hasta) {
        return (hasta != null ? hasta : LocalDate.now()).plusDays(1).atStartOfDay();
    }
}
