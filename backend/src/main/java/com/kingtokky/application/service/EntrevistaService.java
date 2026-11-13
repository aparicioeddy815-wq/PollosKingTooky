package com.kingtokky.application.service;

import com.kingtokky.application.dto.EntrevistaDtos.AgendaEntrevistasResponse;
import com.kingtokky.application.dto.EntrevistaDtos.AgendarEntrevistaRequest;
import com.kingtokky.application.dto.EntrevistaDtos.ContratarRequest;
import com.kingtokky.application.dto.EntrevistaDtos.DocumentoResponse;
import com.kingtokky.application.dto.EntrevistaDtos.EntrevistaResponse;
import com.kingtokky.application.dto.EntrevistaDtos.ObservacionesRequest;
import com.kingtokky.application.dto.EntrevistaDtos.PostulanteRequest;
import com.kingtokky.application.dto.EntrevistaDtos.PostulanteResponse;
import com.kingtokky.application.dto.EntrevistaDtos.ResumenEntrevistadosResponse;
import com.kingtokky.application.dto.EntrevistaDtos.ResumenEntrevistasResponse;
import com.kingtokky.application.dto.EntrevistaDtos.VacanteResponse;
import com.kingtokky.application.dto.comun.ArchivoDescarga;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.entrevista.Entrevista;
import com.kingtokky.domain.model.entrevista.EstadoEntrevista;
import com.kingtokky.domain.model.entrevista.EstadoPostulante;
import com.kingtokky.domain.model.entrevista.EstadoVacante;
import com.kingtokky.domain.model.entrevista.Postulante;
import com.kingtokky.domain.model.entrevista.PostulanteDocumento;
import com.kingtokky.domain.model.entrevista.TipoDocumento;
import com.kingtokky.domain.model.entrevista.UrgenciaVacante;
import com.kingtokky.domain.model.entrevista.Vacante;
import com.kingtokky.domain.model.personal.Empleado;
import com.kingtokky.domain.model.personal.EstacionOperativa;
import com.kingtokky.domain.port.AlmacenArchivos;
import com.kingtokky.domain.repository.EmpleadoRepository;
import com.kingtokky.domain.repository.EntrevistaRepository;
import com.kingtokky.domain.repository.EstacionOperativaRepository;
import com.kingtokky.domain.repository.PostulanteDocumentoRepository;
import com.kingtokky.domain.repository.PostulanteRepository;
import com.kingtokky.domain.repository.UsuarioRepository;
import com.kingtokky.domain.repository.VacanteRepository;
import com.kingtokky.infrastructure.export.ExportadorPdf;
import com.kingtokky.presentation.exception.ApiException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Entrevistas y selección de personal: agenda del gerente y decisión final del administrador. */
@Service
@Transactional(readOnly = true)
public class EntrevistaService {

    private static final Set<TipoDocumento> DOCUMENTOS_REQUERIDOS = EnumSet.of(TipoDocumento.CV, TipoDocumento.CARNET, TipoDocumento.ANTECEDENTES);
    private static final Set<EstadoPostulante> ESTADOS_ACTIVOS = EnumSet.of(EstadoPostulante.POSTULADO, EstadoPostulante.ENTREVISTA_AGENDADA,
            EstadoPostulante.EN_EVALUACION, EstadoPostulante.APROBADO_GERENTE, EstadoPostulante.EN_REVISION_ANTECEDENTES);
    private static final Set<String> EXTENSIONES = Set.of("pdf", "png", "jpg", "jpeg");

    private final VacanteRepository vacantes;
    private final PostulanteRepository postulantes;
    private final EntrevistaRepository entrevistas;
    private final PostulanteDocumentoRepository documentos;
    private final EmpleadoRepository empleados;
    private final EstacionOperativaRepository estaciones;
    private final UsuarioRepository usuarios;
    private final AlmacenArchivos almacen;
    private final ExportadorPdf pdf;
    private final AuditoriaService auditoria;

    public EntrevistaService(VacanteRepository vacantes, PostulanteRepository postulantes, EntrevistaRepository entrevistas,
                             PostulanteDocumentoRepository documentos, EmpleadoRepository empleados, EstacionOperativaRepository estaciones,
                             UsuarioRepository usuarios, AlmacenArchivos almacen, ExportadorPdf pdf, AuditoriaService auditoria) {
        this.vacantes = vacantes;
        this.postulantes = postulantes;
        this.entrevistas = entrevistas;
        this.documentos = documentos;
        this.empleados = empleados;
        this.estaciones = estaciones;
        this.usuarios = usuarios;
        this.almacen = almacen;
        this.pdf = pdf;
        this.auditoria = auditoria;
    }

    // ------------------------------------------------------------------ consultas del gerente

    public List<VacanteResponse> vacantesAbiertas() {
        return vacantes.findByEstadoOrderByUrgenciaAscCargoAsc(EstadoVacante.ABIERTA).stream().map(this::aVacante).toList();
    }

    public ResumenEntrevistasResponse resumen() {
        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyFin = hoyInicio.plusDays(1);
        List<String> criticos = vacantes.findByEstadoOrderByUrgenciaAscCargoAsc(EstadoVacante.ABIERTA).stream()
                .filter(v -> v.getUrgencia() == UrgenciaVacante.CRITICA).map(Vacante::getCargo).toList();
        return new ResumenEntrevistasResponse(
                entrevistas.countByEstadoAndFechaHoraBetween(EstadoEntrevista.PROGRAMADA, hoyInicio, hoyFin),
                postulantes.countByEstado(EstadoPostulante.EN_EVALUACION),
                postulantes.countByEstadoIn(ESTADOS_ACTIVOS), vacantes.countByEstado(EstadoVacante.ABIERTA), criticos);
    }

    /** Agenda: lo de hoy con cuenta regresiva y las siguientes citas programadas. */
    public AgendaEntrevistasResponse agenda() {
        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyFin = hoyInicio.plusDays(1);
        List<EntrevistaResponse> hoy = aEntrevistas(entrevistas.findByFechaHoraBetweenOrderByFechaHoraAsc(hoyInicio, hoyFin));
        List<EntrevistaResponse> programadas = aEntrevistas(entrevistas.findByEstadoAndFechaHoraGreaterThanEqualOrderByFechaHoraAsc(EstadoEntrevista.PROGRAMADA, hoyFin));
        return new AgendaEntrevistasResponse(resumen(), hoy, programadas);
    }

    public List<PostulanteResponse> listarPostulantes(Long idVacante, EstadoPostulante estado) {
        return aPostulantes(postulantes.filtrar(idVacante == null, idVacante == null ? 0L : idVacante, estado == null,
                estado == null ? EstadoPostulante.POSTULADO : estado));
    }

    public PostulanteResponse obtenerPostulante(Long id) {
        return aPostulantes(List.of(buscar(id))).get(0);
    }

    // ------------------------------------------------------------------ acciones del gerente

    @Transactional
    public PostulanteResponse registrarPostulante(PostulanteRequest req) {
        Vacante v = vacantes.findById(req.idVacante()).orElseThrow(() -> ApiException.noEncontrado("Vacante no encontrada"));
        Postulante p = new Postulante();
        p.setNombreCompleto(req.nombreCompleto().trim());
        p.setTelefono(req.telefono().trim());
        p.setCorreo(req.correo() == null || req.correo().isBlank() ? null : req.correo().trim());
        p.setVacante(v);
        postulantes.save(p);
        auditoria.registrar(TipoEvento.CREACION, "Entrevistas", "Registró al postulante " + p.getNombreCompleto() + " para " + v.getCargo(), NivelBitacora.INFO);
        return aPostulantes(List.of(p)).get(0);
    }

    @Transactional
    public EntrevistaResponse agendar(AgendarEntrevistaRequest req, Long idGerente) {
        Postulante p = buscar(req.idPostulante());
        if (p.getEstado() == EstadoPostulante.CONTRATADO || p.getEstado() == EstadoPostulante.DESCARTADO) {
            throw ApiException.reglaDeNegocio("No se puede agendar una entrevista para un postulante " + p.getEstado().name().toLowerCase());
        }
        if (req.fechaHora().isBefore(LocalDateTime.now())) {
            throw ApiException.reglaDeNegocio("La fecha y hora de la entrevista deben ser futuras");
        }
        Entrevista e = new Entrevista();
        e.setPostulante(p);
        e.setFechaHora(req.fechaHora());
        e.setModalidad(req.modalidad());
        e.setGerente(usuarios.getReferenceById(idGerente));
        entrevistas.save(e);
        if (p.getEstado() == EstadoPostulante.POSTULADO) p.setEstado(EstadoPostulante.ENTREVISTA_AGENDADA);
        auditoria.registrar(TipoEvento.CREACION, "Entrevistas", "Agendó una entrevista " + req.modalidad().name().toLowerCase() + " con " + p.getNombreCompleto()
                + " para el " + req.fechaHora().toLocalDate() + " a las " + req.fechaHora().toLocalTime().withSecond(0).withNano(0), NivelBitacora.INFO);
        return aEntrevistas(List.of(e)).get(0);
    }

    @Transactional
    public EntrevistaResponse registrarObservaciones(Long idEntrevista, ObservacionesRequest req) {
        Entrevista e = entrevistas.findById(idEntrevista).orElseThrow(() -> ApiException.noEncontrado("Entrevista no encontrada"));
        Postulante p = e.getPostulante();
        if (req.observaciones() != null) e.setObservaciones(req.observaciones().isBlank() ? null : req.observaciones().trim());
        if (req.evaluacionGerente() != null) p.setEvaluacionGerente(req.evaluacionGerente().isBlank() ? null : req.evaluacionGerente().trim());
        if (Boolean.TRUE.equals(req.realizada())) {
            e.setEstado(EstadoEntrevista.REALIZADA);
            if (p.getEstado() == EstadoPostulante.ENTREVISTA_AGENDADA || p.getEstado() == EstadoPostulante.POSTULADO) p.setEstado(EstadoPostulante.EN_EVALUACION);
        }
        if (Boolean.TRUE.equals(req.aprobar())) {
            boolean completos = faltantes(p.getId()).isEmpty();
            p.setEstado(completos ? EstadoPostulante.APROBADO_GERENTE : EstadoPostulante.EN_REVISION_ANTECEDENTES);
            auditoria.registrar(TipoEvento.MODIFICACION, "Entrevistas", "Dio su visto bueno al postulante " + p.getNombreCompleto(), NivelBitacora.INFO);
        }
        return aEntrevistas(List.of(e)).get(0);
    }

    @Transactional
    public DocumentoResponse subirDocumento(Long idPostulante, TipoDocumento tipo, MultipartFile archivo, Long idUsuario) {
        Postulante p = buscar(idPostulante);
        String nombre = archivo.getOriginalFilename() == null ? "documento" : archivo.getOriginalFilename();
        String ext = nombre.contains(".") ? nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase() : "";
        if (archivo.isEmpty() || !EXTENSIONES.contains(ext)) {
            throw ApiException.solicitudInvalida("El archivo debe ser PDF, PNG o JPG");
        }
        try {
            PostulanteDocumento d = new PostulanteDocumento();
            d.setPostulante(p);
            d.setTipo(tipo);
            d.setNombreArchivo(nombre.length() > 200 ? nombre.substring(nombre.length() - 200) : nombre);
            d.setRuta(almacen.guardar("postulantes/" + idPostulante, nombre, archivo.getInputStream()));
            d.setTamanoBytes(archivo.getSize());
            d.setSubidoPor(usuarios.getReferenceById(idUsuario));
            documentos.save(d);
            if (p.getEstado() == EstadoPostulante.EN_REVISION_ANTECEDENTES && faltantes(p.getId()).isEmpty()) {
                p.setEstado(EstadoPostulante.APROBADO_GERENTE);
            }
            auditoria.registrar(TipoEvento.CREACION, "Entrevistas", "Subió el documento " + tipo.name() + " de " + p.getNombreCompleto(), NivelBitacora.INFO);
            return aDocumento(d);
        } catch (IOException e) {
            throw ApiException.reglaDeNegocio("No se pudo guardar el archivo. Intenta de nuevo");
        }
    }

    /** Devuelve el archivo guardado; si es un registro de ejemplo sin archivo físico, genera un PDF de muestra. */
    public ArchivoDescarga descargarDocumento(Long idDocumento) {
        PostulanteDocumento d = documentos.findById(idDocumento).orElseThrow(() -> ApiException.noEncontrado("Documento no encontrado"));
        if (almacen.existe(d.getRuta())) {
            return new ArchivoDescarga(d.getNombreArchivo(), tipoContenido(d.getNombreArchivo()), almacen.cargar(d.getRuta()));
        }
        byte[] muestra = pdf.documento(d.getTipo().name() + " · " + d.getPostulante().getNombreCompleto(),
                "Documento de muestra (el archivo original no está almacenado)",
                List.of(new ExportadorPdf.Seccion("Datos del postulante", List.of("Campo", "Valor"), List.of(
                        List.of("Nombre", d.getPostulante().getNombreCompleto()), List.of("Cargo", d.getPostulante().getVacante().getCargo()),
                        List.of("Teléfono", d.getPostulante().getTelefono())))));
        return new ArchivoDescarga(d.getNombreArchivo().endsWith(".pdf") ? d.getNombreArchivo() : d.getNombreArchivo() + ".pdf",
                "application/pdf", new ByteArrayResource(muestra));
    }

    public ArchivoDescarga descargarCv(Long idPostulante) {
        return documentos.findFirstByPostulante_IdAndTipoOrderBySubidoEnDesc(idPostulante, TipoDocumento.CV)
                .map(d -> descargarDocumento(d.getId()))
                .orElseThrow(() -> ApiException.noEncontrado("El postulante todavía no tiene CV cargado"));
    }

    // ------------------------------------------------------------------ decisión del administrador

    public ResumenEntrevistadosResponse resumenEntrevistados() {
        long criticas = vacantes.findByEstadoOrderByUrgenciaAscCargoAsc(EstadoVacante.ABIERTA).stream()
                .filter(v -> v.getUrgencia() == UrgenciaVacante.CRITICA).count();
        long vistoBueno = postulantes.countByEstadoIn(List.of(EstadoPostulante.APROBADO_GERENTE, EstadoPostulante.EN_REVISION_ANTECEDENTES));
        long contratadosMes = postulantes.filtrar(true, 0L, false, EstadoPostulante.CONTRATADO).stream()
                .filter(p -> p.getDecididoEn() != null && p.getDecididoEn().isAfter(LocalDate.now().withDayOfMonth(1).atStartOfDay())).count();
        return new ResumenEntrevistadosResponse(postulantes.countByEstadoIn(ESTADOS_ACTIVOS), vistoBueno, criticas, contratadosMes);
    }

    @Transactional
    public PostulanteResponse contratar(Long idPostulante, ContratarRequest req, Long idAdmin) {
        Postulante p = buscar(idPostulante);
        if (p.getEstado() != EstadoPostulante.APROBADO_GERENTE) {
            throw ApiException.reglaDeNegocio("Solo se puede contratar a un postulante con el visto bueno del gerente y documentos completos");
        }
        List<TipoDocumento> falta = faltantes(p.getId());
        if (!falta.isEmpty()) {
            throw ApiException.reglaDeNegocio("Faltan documentos obligatorios: " + falta);
        }
        Vacante v = p.getVacante();
        EstacionOperativa estacion = req.idEstacion() != null
                ? estaciones.findById(req.idEstacion()).orElseThrow(() -> ApiException.noEncontrado("Estación no encontrada"))
                : v.getEstacion();
        Empleado e = new Empleado();
        e.setCodigo(siguienteCodigoEmpleado());
        e.setNombreCompleto(p.getNombreCompleto());
        e.setCargo(v.getCargo());
        e.setTurno(req.turno());
        e.setSalario(req.salario());
        e.setTelefono(p.getTelefono());
        e.setEstacion(estacion);
        empleados.save(e);
        p.setEstado(EstadoPostulante.CONTRATADO);
        p.setDecididoEn(LocalDateTime.now());
        p.setDecididoPor(usuarios.getReferenceById(idAdmin));
        v.setCantidad(Math.max(0, v.getCantidad() - 1));
        if (v.getCantidad() == 0) v.setEstado(EstadoVacante.CUBIERTA);
        auditoria.registrar(TipoEvento.CONTRATACION, "Entrevistas", "Contrató definitivamente a " + p.getNombreCompleto() + " como " + v.getCargo()
                + " (" + e.getCodigo() + ")", NivelBitacora.ALERTA);
        return aPostulantes(List.of(p)).get(0);
    }

    @Transactional
    public PostulanteResponse descartar(Long idPostulante, String motivo, Long idAdmin) {
        Postulante p = buscar(idPostulante);
        if (p.getEstado() == EstadoPostulante.CONTRATADO || p.getEstado() == EstadoPostulante.DESCARTADO) {
            throw ApiException.reglaDeNegocio("Este postulante ya tiene una decisión final");
        }
        p.setEstado(EstadoPostulante.DESCARTADO);
        p.setDecididoEn(LocalDateTime.now());
        p.setDecididoPor(usuarios.getReferenceById(idAdmin));
        if (motivo != null && !motivo.isBlank()) p.setEvaluacionGerente(motivo.trim());
        auditoria.registrar(TipoEvento.CONTRATACION, "Entrevistas", "Descartó a " + p.getNombreCompleto() + (motivo == null || motivo.isBlank() ? "" : " (" + motivo.trim() + ")"), NivelBitacora.ALERTA);
        return aPostulantes(List.of(p)).get(0);
    }

    // ------------------------------------------------------------------ internos

    private Postulante buscar(Long id) {
        return postulantes.findById(id).orElseThrow(() -> ApiException.noEncontrado("Postulante no encontrado"));
    }

    private List<TipoDocumento> faltantes(Long idPostulante) {
        Set<TipoDocumento> presentes = documentos.findByPostulante_IdOrderBySubidoEnDesc(idPostulante).stream()
                .map(PostulanteDocumento::getTipo).collect(Collectors.toSet());
        return DOCUMENTOS_REQUERIDOS.stream().filter(t -> !presentes.contains(t)).sorted().toList();
    }

    private String siguienteCodigoEmpleado() {
        String ultimo = empleados.ultimoCodigo();
        int n = 100;
        if (ultimo != null && ultimo.matches("EMP-\\d+")) n = Integer.parseInt(ultimo.substring(4));
        return String.format("EMP-%04d", n + 1);
    }

    private String tipoContenido(String nombre) {
        String n = nombre.toLowerCase();
        if (n.endsWith(".pdf")) return "application/pdf";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private VacanteResponse aVacante(Vacante v) {
        return new VacanteResponse(v.getId(), v.getCargo(), v.getEstacion() == null ? null : v.getEstacion().getNombre(), v.getUrgencia(),
                v.getCantidad(), v.getEstado().name());
    }

    private DocumentoResponse aDocumento(PostulanteDocumento d) {
        return new DocumentoResponse(d.getId(), d.getTipo(), d.getNombreArchivo(), d.getTamanoBytes(), d.getSubidoEn());
    }

    private List<PostulanteResponse> aPostulantes(List<Postulante> lista) {
        if (lista.isEmpty()) return List.of();
        List<Long> ids = lista.stream().map(Postulante::getId).toList();
        Map<Long, List<PostulanteDocumento>> docs = documentos.findByPostulante_IdIn(ids).stream()
                .collect(Collectors.groupingBy(d -> d.getPostulante().getId()));
        List<PostulanteResponse> salida = new ArrayList<>();
        for (Postulante p : lista) {
            List<PostulanteDocumento> mios = docs.getOrDefault(p.getId(), List.of());
            Set<TipoDocumento> presentes = mios.stream().map(PostulanteDocumento::getTipo).collect(Collectors.toSet());
            List<TipoDocumento> falta = DOCUMENTOS_REQUERIDOS.stream().filter(t -> !presentes.contains(t)).sorted().toList();
            LocalDateTime fecha = entrevistas.findFirstByPostulante_IdOrderByFechaHoraDesc(p.getId()).map(Entrevista::getFechaHora).orElse(null);
            salida.add(new PostulanteResponse(p.getId(), p.getNombreCompleto(), p.getTelefono(), p.getCorreo(), p.getVacante().getId(),
                    p.getVacante().getCargo(), p.getVacante().getUrgencia(), p.getEstado(), p.getEvaluacionGerente(), p.getCreadoEn(),
                    falta.isEmpty(), falta, mios.stream().map(this::aDocumento).toList(), fecha));
        }
        return salida;
    }

    private List<EntrevistaResponse> aEntrevistas(List<Entrevista> lista) {
        LocalDateTime ahora = LocalDateTime.now();
        return lista.stream().map(e -> {
            Postulante p = e.getPostulante();
            boolean tieneCv = documentos.findFirstByPostulante_IdAndTipoOrderBySubidoEnDesc(p.getId(), TipoDocumento.CV).isPresent();
            Long minutos = e.getEstado() == EstadoEntrevista.PROGRAMADA ? Duration.between(ahora, e.getFechaHora()).toMinutes() : null;
            return new EntrevistaResponse(e.getId(), p.getId(), p.getNombreCompleto(), p.getTelefono(), p.getVacante().getCargo(),
                    p.getVacante().getUrgencia(), e.getFechaHora(), e.getModalidad(), e.getEstado(), e.getObservaciones(), minutos,
                    faltantes(p.getId()).isEmpty(), tieneCv, p.getEstado());
        }).toList();
    }
}
