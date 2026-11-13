package com.kingtokky.application.service;

import com.kingtokky.application.dto.ClienteDtos.QuejaResponse;
import com.kingtokky.application.dto.QuejaGestionDtos.ResumenQuejasResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.cliente.EstadoQueja;
import com.kingtokky.domain.model.cliente.Queja;
import com.kingtokky.domain.model.cliente.TipoQueja;
import com.kingtokky.domain.repository.EvaluacionDiariaRepository;
import com.kingtokky.domain.repository.QuejaRepository;
import com.kingtokky.domain.repository.UsuarioRepository;
import com.kingtokky.presentation.exception.ApiException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bandeja del gerente: sugerencias, quejas y felicitaciones de los clientes. */
@Service
@Transactional(readOnly = true)
public class QuejaGestionService {

    private final QuejaRepository quejas;
    private final EvaluacionDiariaRepository evaluaciones;
    private final UsuarioRepository usuarios;
    private final ClienteService clienteService;
    private final PedidoService pedidoService;
    private final ConfiguracionService config;
    private final AuditoriaService auditoria;

    public QuejaGestionService(QuejaRepository quejas, EvaluacionDiariaRepository evaluaciones, UsuarioRepository usuarios,
                               ClienteService clienteService, PedidoService pedidoService, ConfiguracionService config,
                               AuditoriaService auditoria) {
        this.quejas = quejas;
        this.evaluaciones = evaluaciones;
        this.usuarios = usuarios;
        this.clienteService = clienteService;
        this.pedidoService = pedidoService;
        this.config = config;
        this.auditoria = auditoria;
    }

    public PaginaResponse<QuejaResponse> listar(TipoQueja tipo, LocalDate desde, LocalDate hasta, int pagina, int tamano) {
        var page = quejas.filtrar(tipo == null, tipo == null ? TipoQueja.RECLAMO : tipo, inicio(desde), fin(hasta),
                PageRequest.of(pagina, Math.min(tamano, 100)));
        return PaginaResponse.de(page, clienteService::aQueja);
    }

    public List<QuejaResponse> listarParaExportar(TipoQueja tipo, LocalDate desde, LocalDate hasta) {
        return quejas.filtrarTodas(tipo == null, tipo == null ? TipoQueja.RECLAMO : tipo, inicio(desde), fin(hasta))
                .stream().map(clienteService::aQueja).toList();
    }

    public ResumenQuejasResponse resumen() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime ahora = LocalDateTime.now().plusMinutes(1);
        Double promedio = evaluaciones.promedioEntre(hoy.minusDays(30), hoy);
        List<Object[]> respuestas = quejas.tiemposDeRespuesta(inicioMes, ahora);
        double horas = respuestas.isEmpty() ? 0 : respuestas.stream()
                .mapToLong(f -> Duration.between((LocalDateTime) f[0], (LocalDateTime) f[1]).toMinutes()).average().orElse(0) / 60.0;
        return new ResumenQuejasResponse(
                quejas.countByCreadoEnBetween(inicioMes, ahora),
                quejas.countByTipoAndCreadoEnBetween(TipoQueja.RECLAMO, inicioMes, ahora),
                quejas.countByTipoAndCreadoEnBetween(TipoQueja.SUGERENCIA, inicioMes, ahora),
                quejas.countByTipoAndCreadoEnBetween(TipoQueja.FELICITACION, inicioMes, ahora),
                quejas.countByEstadoNot(EstadoQueja.RESPONDIDA),
                pedidoService.tiempoPromedioDespachoMin(hoy.withDayOfMonth(1), hoy),
                config.entero("objetivo_despacho_min", 8),
                promedio == null ? 0 : Math.round(promedio / 5.0 * 1000) / 10.0,
                Math.round(horas * 10) / 10.0);
    }

    @Transactional
    public QuejaResponse responder(Long id, String respuesta, Long idGerente) {
        Queja q = buscar(id);
        q.setRespuesta(respuesta.trim());
        q.setEstado(EstadoQueja.RESPONDIDA);
        q.setRespondidoEn(LocalDateTime.now());
        q.setRespondidoPor(usuarios.getReferenceById(idGerente));
        auditoria.registrar(TipoEvento.MODIFICACION, "Quejas", "Respondió la " + q.getTipo().name().toLowerCase() + " #" + q.getId()
                + " de " + q.getCliente().getNombreCompleto(), NivelBitacora.INFO);
        return clienteService.aQueja(q);
    }

    @Transactional
    public QuejaResponse marcarEnRevision(Long id) {
        Queja q = buscar(id);
        if (q.getEstado() == EstadoQueja.RECIBIDA) q.setEstado(EstadoQueja.EN_REVISION);
        return clienteService.aQueja(q);
    }

    private Queja buscar(Long id) {
        return quejas.findById(id).orElseThrow(() -> ApiException.noEncontrado("Mensaje no encontrado"));
    }

    private LocalDateTime inicio(LocalDate desde) {
        return (desde != null ? desde : LocalDate.now().minusDays(60)).atStartOfDay();
    }

    private LocalDateTime fin(LocalDate hasta) {
        return (hasta != null ? hasta : LocalDate.now()).plusDays(1).atStartOfDay();
    }
}
