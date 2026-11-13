package com.kingtokky.application.service;

import com.kingtokky.application.dto.ClienteDtos.AyudaResponse;
import com.kingtokky.application.dto.ClienteDtos.CategoriaSalon;
import com.kingtokky.application.dto.ClienteDtos.EvaluacionRequest;
import com.kingtokky.application.dto.ClienteDtos.EvaluacionResponse;
import com.kingtokky.application.dto.ClienteDtos.MeGustaResponse;
import com.kingtokky.application.dto.ClienteDtos.PasoAyuda;
import com.kingtokky.application.dto.ClienteDtos.ProductoSalon;
import com.kingtokky.application.dto.ClienteDtos.QuejaRequest;
import com.kingtokky.application.dto.ClienteDtos.QuejaResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.cliente.EvaluacionDiaria;
import com.kingtokky.domain.model.cliente.Queja;
import com.kingtokky.domain.model.menu.Favorito;
import com.kingtokky.domain.model.menu.Producto;
import com.kingtokky.domain.model.pedido.EstadoPedido;
import com.kingtokky.domain.model.seguridad.Usuario;
import com.kingtokky.domain.repository.DetallePedidoRepository;
import com.kingtokky.domain.repository.EvaluacionDiariaRepository;
import com.kingtokky.domain.repository.FavoritoRepository;
import com.kingtokky.domain.repository.ProductoRepository;
import com.kingtokky.domain.repository.QuejaRepository;
import com.kingtokky.domain.repository.UsuarioRepository;
import com.kingtokky.presentation.exception.ApiException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Portal del cliente frecuente: menú del salón, favoritos, quejas/sugerencias, evaluación diaria y ayuda. */
@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ProductoRepository productos;
    private final FavoritoRepository favoritos;
    private final DetallePedidoRepository detalles;
    private final QuejaRepository quejas;
    private final EvaluacionDiariaRepository evaluaciones;
    private final UsuarioRepository usuarios;
    private final ConfiguracionService config;
    private final AuditoriaService auditoria;

    public ClienteService(ProductoRepository productos, FavoritoRepository favoritos, DetallePedidoRepository detalles,
                          QuejaRepository quejas, EvaluacionDiariaRepository evaluaciones, UsuarioRepository usuarios,
                          ConfiguracionService config, AuditoriaService auditoria) {
        this.productos = productos;
        this.favoritos = favoritos;
        this.detalles = detalles;
        this.quejas = quejas;
        this.evaluaciones = evaluaciones;
        this.usuarios = usuarios;
        this.config = config;
        this.auditoria = auditoria;
    }

    /** Menú de PedidosYA más los platos exclusivos del local, con corazón y veces consumidas. */
    public List<CategoriaSalon> menuSalon(Long idUsuario) {
        Set<Long> meGusta = favoritos.findByUsuario_IdUsuario(idUsuario).stream().map(f -> f.getProducto().getId()).collect(Collectors.toSet());
        Map<Long, Long> consumo = new HashMap<>();
        for (Object[] fila : detalles.consumoDelCliente(idUsuario, EstadoPedido.CANCELADO)) {
            consumo.put(((Number) fila[0]).longValue(), ((Number) fila[1]).longValue());
        }
        Map<String, List<ProductoSalon>> porCategoria = new LinkedHashMap<>();
        Map<String, String> nombres = new LinkedHashMap<>();
        for (Producto p : productos.findByActivoTrueAndEnSalonTrueOrderByCategoria_OrdenAscNombreAsc()) {
            String codigo = p.getCategoria().getCodigo();
            nombres.putIfAbsent(codigo, p.getCategoria().getNombre());
            porCategoria.computeIfAbsent(codigo, k -> new java.util.ArrayList<>()).add(new ProductoSalon(p.getId(), p.getNombre(),
                    p.getDescripcion(), p.getPrecio(), codigo, p.getImagenUrl(), p.isDisponible(), meGusta.contains(p.getId()),
                    consumo.getOrDefault(p.getId(), 0L)));
        }
        return porCategoria.entrySet().stream().map(e -> new CategoriaSalon(e.getKey(), nombres.get(e.getKey()), e.getValue())).toList();
    }

    public List<ProductoSalon> misFavoritos(Long idUsuario) {
        return menuSalon(idUsuario).stream().flatMap(c -> c.productos().stream()).filter(ProductoSalon::meGusta).toList();
    }

    @Transactional
    public MeGustaResponse alternarMeGusta(Long idUsuario, Long idProducto) {
        var existente = favoritos.findByUsuario_IdUsuarioAndProducto_Id(idUsuario, idProducto);
        if (existente.isPresent()) {
            favoritos.delete(existente.get());
            return new MeGustaResponse(idProducto, false);
        }
        Producto p = productos.findByIdAndActivoTrue(idProducto).orElseThrow(() -> ApiException.noEncontrado("Producto no encontrado"));
        Favorito f = new Favorito();
        f.setUsuario(usuarios.getReferenceById(idUsuario));
        f.setProducto(p);
        favoritos.save(f);
        return new MeGustaResponse(idProducto, true);
    }

    // ------------------------------------------------------------------ quejas y sugerencias

    @Transactional
    public QuejaResponse crearQueja(Long idUsuario, QuejaRequest req) {
        Usuario u = usuarios.findById(idUsuario).orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
        Queja q = new Queja();
        q.setCliente(u);
        q.setTipo(req.tipo());
        q.setCategoria(req.categoria());
        q.setNumeroPedido(req.numeroPedido());
        q.setValoracion(req.valoracion());
        q.setMensaje(req.mensaje().trim());
        quejas.save(q);
        auditoria.registrar(TipoEvento.CREACION, "Quejas", "Envió una " + req.tipo().name().toLowerCase() + " (" + req.categoria().name().toLowerCase().replace('_', ' ') + ")", NivelBitacora.INFO);
        return aQueja(q);
    }

    public PaginaResponse<QuejaResponse> misQuejas(Long idUsuario, int pagina, int tamano) {
        return PaginaResponse.de(quejas.findByCliente_IdUsuarioOrderByCreadoEnDesc(idUsuario, PageRequest.of(pagina, Math.min(tamano, 50))), this::aQueja);
    }

    public QuejaResponse aQueja(Queja q) {
        return new QuejaResponse(q.getId(), q.getCliente().getIdUsuario(), q.getCliente().getNombreCompleto(), q.getTipo(), q.getCategoria(),
                q.getNumeroPedido(), q.getValoracion(), q.getMensaje(), q.getEstado(), q.getRespuesta(), q.getCreadoEn(), q.getRespondidoEn(),
                q.getRespondidoPor() == null ? null : q.getRespondidoPor().getNombreCompleto());
    }

    // ------------------------------------------------------------------ evaluación diaria

    /** Una evaluación por cliente y por día; al día siguiente el formulario vuelve a quedar vacío. */
    @Transactional
    public EvaluacionResponse evaluar(Long idUsuario, EvaluacionRequest req) {
        LocalDate hoy = LocalDate.now();
        EvaluacionDiaria e = evaluaciones.findByUsuario_IdUsuarioAndFecha(idUsuario, hoy).orElseGet(() -> {
            EvaluacionDiaria nueva = new EvaluacionDiaria();
            nueva.setUsuario(usuarios.getReferenceById(idUsuario));
            nueva.setFecha(hoy);
            return nueva;
        });
        e.setEstrellas(req.estrellas());
        e.setComentario(req.comentario() == null || req.comentario().isBlank() ? null : req.comentario().trim());
        evaluaciones.save(e);
        return new EvaluacionResponse(e.getFecha(), e.getEstrellas(), e.getComentario());
    }

    public EvaluacionResponse evaluacionDeHoy(Long idUsuario) {
        return evaluaciones.findByUsuario_IdUsuarioAndFecha(idUsuario, LocalDate.now())
                .map(e -> new EvaluacionResponse(e.getFecha(), e.getEstrellas(), e.getComentario())).orElse(null);
    }

    // ------------------------------------------------------------------ ayuda

    public AyudaResponse ayuda() {
        String whatsapp = config.texto("whatsapp_corporativo", "");
        return new AyudaResponse(whatsapp, config.texto("correo_contacto", ""),
                "Hola King Tokky, necesito ayuda con mi cuenta o mi reserva.",
                List.of(new PasoAyuda(1, "Elige la fecha", "Selecciona el día de la semana en que quieres venir."),
                        new PasoAyuda(2, "Elige la hora", "Mira los horarios con cupo disponible y escoge el tuyo."),
                        new PasoAyuda(3, "Selecciona tus platos", "Arma tu pedido anticipado; verás el total al instante."),
                        new PasoAyuda(4, "Paga en caja", "Pasa entre 5 y 10 minutos antes de tu hora a pagar y confirmar la reserva. Si no pagas a tiempo, se cancela sola."),
                        new PasoAyuda(5, "Disfruta", "Tus platos pasan a cocina cuando pagas, para que lleguen frescos a tu mesa.")));
    }
}
