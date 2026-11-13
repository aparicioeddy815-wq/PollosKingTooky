package com.kingtokky.application.service;

import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.application.dto.PersonalDtos.ActualizarEmpleadoRequest;
import com.kingtokky.application.dto.PersonalDtos.AsignarEmpleadoRequest;
import com.kingtokky.application.dto.PersonalDtos.CrearPerfilRequest;
import com.kingtokky.application.dto.PersonalDtos.EmpleadoResponse;
import com.kingtokky.application.dto.PersonalDtos.EstacionResponse;
import com.kingtokky.application.dto.PersonalDtos.PerfilCreadoResponse;
import com.kingtokky.application.dto.PersonalDtos.PermisoResponse;
import com.kingtokky.application.dto.PersonalDtos.PuestoResponse;
import com.kingtokky.application.dto.PersonalDtos.ResumenPersonalResponse;
import com.kingtokky.application.dto.PersonalDtos.RolResponse;
import com.kingtokky.application.dto.PersonalDtos.UsuarioResponse;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.personal.Empleado;
import com.kingtokky.domain.model.personal.EstacionOperativa;
import com.kingtokky.domain.model.personal.EstadoEmpleado;
import com.kingtokky.domain.model.personal.Turno;
import com.kingtokky.domain.model.seguridad.EstadoUsuario;
import com.kingtokky.domain.model.seguridad.Permiso;
import com.kingtokky.domain.model.seguridad.Rol;
import com.kingtokky.domain.model.seguridad.Usuario;
import com.kingtokky.domain.regla.ReglasPersonal;
import com.kingtokky.domain.repository.EmpleadoRepository;
import com.kingtokky.domain.repository.EstacionOperativaRepository;
import com.kingtokky.domain.repository.PermisoRepository;
import com.kingtokky.domain.repository.RolRepository;
import com.kingtokky.domain.repository.UsuarioRepository;
import com.kingtokky.infrastructure.security.TokenUtil;
import com.kingtokky.presentation.exception.ApiException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Personal del local, estaciones operativas, perfiles de acceso y roles con sus permisos. */
@Service
@Transactional(readOnly = true)
public class PersonalService {

    private static final String ROL_ADMIN = "ROL-ADMIN";

    private final EmpleadoRepository empleados;
    private final EstacionOperativaRepository estaciones;
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PermisoRepository permisos;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;

    public PersonalService(EmpleadoRepository empleados, EstacionOperativaRepository estaciones, UsuarioRepository usuarios,
                           RolRepository roles, PermisoRepository permisos, PasswordEncoder encoder, AuditoriaService auditoria) {
        this.empleados = empleados;
        this.estaciones = estaciones;
        this.usuarios = usuarios;
        this.roles = roles;
        this.permisos = permisos;
        this.encoder = encoder;
        this.auditoria = auditoria;
    }

    // ------------------------------------------------------------------ consultas

    public ResumenPersonalResponse resumen() {
        Turno turno = ReglasPersonal.esTurnoManana(LocalTime.now()) ? Turno.MANANA : Turno.TARDE;
        List<EstacionResponse> est = estaciones();
        long totalPuestos = est.stream().mapToLong(EstacionResponse::puestosTotales).sum();
        long ocupados = est.stream().mapToLong(EstacionResponse::ocupados).sum();
        return new ResumenPersonalResponse(empleados.count(), empleados.countByEstado(EstadoEmpleado.ACTIVO), empleados.countByEstado(EstadoEmpleado.SUSPENDIDO),
                empleados.countByUsuarioIsNotNull(), turno, empleados.countByEstadoAndTurno(EstadoEmpleado.ACTIVO, turno), totalPuestos, ocupados,
                totalPuestos == 0 ? 0 : Math.round(ocupados * 1000.0 / totalPuestos) / 10.0);
    }

    public PaginaResponse<EmpleadoResponse> listar(String q, String rolCodigo, Turno turno, int pagina, int tamano) {
        String patron = q == null || q.isBlank() ? "" : "%" + q.trim().toLowerCase() + "%";
        boolean todosRoles = rolCodigo == null || rolCodigo.isBlank();
        var page = empleados.buscar(patron, todosRoles, todosRoles ? "" : rolCodigo, turno == null, turno == null ? Turno.MANANA : turno,
                PageRequest.of(pagina, Math.min(tamano, 100)));
        return PaginaResponse.de(page, this::aEmpleado);
    }

    public List<EmpleadoResponse> listarParaNomina(String rolCodigo) {
        boolean todos = rolCodigo == null || rolCodigo.isBlank();
        return empleados.listarParaNomina(todos, todos ? "" : rolCodigo).stream().map(this::aEmpleado).toList();
    }

    /** Estaciones con sus puestos: cuántos hay, cuáles están ocupados y por quién. */
    public List<EstacionResponse> estaciones() {
        List<EstacionResponse> resultado = new ArrayList<>();
        for (EstacionOperativa e : estaciones.findAllByOrderByNombreAsc()) {
            List<Empleado> activos = empleados.findByEstacion_IdAndEstado(e.getId(), EstadoEmpleado.ACTIVO);
            List<PuestoResponse> puestos = new ArrayList<>();
            for (int i = 0; i < e.getPuestosTotales(); i++) {
                if (i < activos.size()) puestos.add(new PuestoResponse(i + 1, true, activos.get(i).getCargo(), activos.get(i).getNombreCompleto()));
                else puestos.add(new PuestoResponse(i + 1, false, null, null));
            }
            int ocupados = Math.min(activos.size(), e.getPuestosTotales());
            double porcentaje = e.getPuestosTotales() == 0 ? 0 : Math.round(ocupados * 1000.0 / e.getPuestosTotales()) / 10.0;
            resultado.add(new EstacionResponse(e.getId(), e.getNombre(), e.getPuestosTotales(), ocupados, porcentaje, puestos));
        }
        return resultado;
    }

    public List<RolResponse> listarRoles() {
        return roles.findAll().stream().sorted((a, b) -> a.getNivelSeguridad().compareTo(b.getNivelSeguridad())).map(this::aRol).toList();
    }

    public List<PermisoResponse> listarPermisos() {
        return permisos.findAllByOrderByCodigoAsc().stream().map(p -> new PermisoResponse(p.getCodigo(), p.getDescripcion())).toList();
    }

    public List<UsuarioResponse> usuariosDeRol(Long idRol) {
        Rol rol = roles.findById(idRol).orElseThrow(() -> ApiException.noEncontrado("Rol no encontrado"));
        return usuarios.findByRol_Codigo(rol.getCodigo()).stream().map(this::aUsuario).toList();
    }

    public PaginaResponse<UsuarioResponse> listarUsuarios(String q, Integer estado, int pagina, int tamano) {
        String patron = q == null || q.isBlank() ? "" : "%" + q.trim().toLowerCase() + "%";
        EstadoUsuario filtro = estado == null ? EstadoUsuario.ACTIVO : (estado == 1 ? EstadoUsuario.ACTIVO : EstadoUsuario.SUSPENDIDO);
        return PaginaResponse.de(usuarios.buscar(patron, estado == null, filtro, PageRequest.of(pagina, Math.min(tamano, 100))), this::aUsuario);
    }

    // ------------------------------------------------------------------ altas y cambios

    /** Crea el perfil de acceso de un colaborador. Si no se indica contraseña se genera una temporal. */
    @Transactional
    public PerfilCreadoResponse crearPerfil(CrearPerfilRequest req) {
        if (usuarios.existsByNombreUsuarioIgnoreCase(req.nombreUsuario())) throw ApiException.conflicto("El nombre de usuario ya está en uso");
        if (usuarios.existsByCorreoIgnoreCase(req.correo())) throw ApiException.conflicto("Ya existe una cuenta con ese correo");
        Rol rol = rol(req.rolCodigo());
        if ("ROL-CLIENTE".equals(rol.getCodigo())) throw ApiException.solicitudInvalida("Los perfiles de cliente se crean desde el registro público");
        boolean generada = req.contrasena() == null || req.contrasena().isBlank();
        String contrasena = generada ? TokenUtil.generar().substring(0, 10) : req.contrasena();

        Usuario u = new Usuario();
        u.setNombreUsuario(req.nombreUsuario().trim());
        u.setCorreo(req.correo().trim().toLowerCase());
        u.setNombreCompleto(req.nombreCompleto().trim());
        u.setRol(rol);
        u.setContrasenaHash(encoder.encode(contrasena));
        usuarios.save(u);
        if (req.idEmpleado() != null) {
            Empleado e = empleado(req.idEmpleado());
            if (e.getUsuario() != null) throw ApiException.conflicto("El empleado ya tiene un perfil de acceso");
            e.setUsuario(u);
        }
        auditoria.registrar(TipoEvento.CREACION, "Roles y personal", "Creó el perfil \"" + u.getNombreUsuario() + "\" con rol " + rol.getNombre(), nivelPara(rol));
        return new PerfilCreadoResponse(u.getIdUsuario(), u.getNombreUsuario(), rol.getCodigo(), generada ? contrasena : null);
    }

    @Transactional
    public EmpleadoResponse asignarEmpleado(AsignarEmpleadoRequest req) {
        Empleado e = new Empleado();
        e.setCodigo(siguienteCodigo());
        e.setNombreCompleto(req.nombreCompleto().trim());
        e.setCargo(req.cargo().trim());
        e.setTurno(req.turno());
        e.setSalario(req.salario());
        e.setTelefono(req.telefono());
        e.setEstacion(req.idEstacion() == null ? null : estaciones.findById(req.idEstacion()).orElseThrow(() -> ApiException.noEncontrado("Estación no encontrada")));
        empleados.save(e);
        auditoria.registrar(TipoEvento.CREACION, "Roles y personal", "Registró al empleado " + e.getNombreCompleto() + " (" + e.getCodigo() + ") como " + e.getCargo(), NivelBitacora.INFO);
        return aEmpleado(e);
    }

    @Transactional
    public EmpleadoResponse actualizarEmpleado(Long id, ActualizarEmpleadoRequest req) {
        Empleado e = empleado(id);
        if (req.cargo() != null && !req.cargo().isBlank()) e.setCargo(req.cargo().trim());
        if (req.turno() != null) e.setTurno(req.turno());
        if (req.salario() != null) e.setSalario(req.salario());
        if (req.idEstacion() != null) e.setEstacion(estaciones.findById(req.idEstacion()).orElseThrow(() -> ApiException.noEncontrado("Estación no encontrada")));
        auditoria.registrar(TipoEvento.MODIFICACION, "Roles y personal", "Actualizó los datos de " + e.getNombreCompleto() + " (" + e.getCodigo() + ")", NivelBitacora.INFO);
        return aEmpleado(e);
    }

    /** Editar rol: los cambios que tocan los niveles 1 y 2 quedan como eventos críticos en la bitácora. */
    @Transactional
    public EmpleadoResponse cambiarRol(Long idEmpleado, String rolCodigo, Long idActor) {
        Empleado e = empleado(idEmpleado);
        Usuario u = e.getUsuario();
        if (u == null) throw ApiException.reglaDeNegocio("El empleado todavía no tiene un perfil de acceso. Crea el perfil primero");
        Rol nuevo = rol(rolCodigo);
        Rol anterior = u.getRol();
        if (anterior.getCodigo().equals(nuevo.getCodigo())) return aEmpleado(e);
        if (ROL_ADMIN.equals(anterior.getCodigo()) && usuarios.countByRol_CodigoAndEstado(ROL_ADMIN, EstadoUsuario.ACTIVO) <= 1) {
            throw ApiException.reglaDeNegocio("No puedes quitar el rol al último administrador activo");
        }
        if (u.getIdUsuario().equals(idActor) && ROL_ADMIN.equals(anterior.getCodigo())) {
            throw ApiException.reglaDeNegocio("No puedes quitarte a ti mismo el rol de administrador");
        }
        u.setRol(nuevo);
        boolean sensible = esSensible(anterior) || esSensible(nuevo);
        auditoria.registrar(TipoEvento.PERMISOS, "Roles y personal", "Cambió el rol de " + e.getNombreCompleto() + " de " + anterior.getNombre() + " a " + nuevo.getNombre(),
                sensible ? NivelBitacora.CRITICO : NivelBitacora.ALERTA);
        return aEmpleado(e);
    }

    @Transactional
    public EmpleadoResponse suspender(Long idEmpleado, String motivo, Long idActor) {
        Empleado e = empleado(idEmpleado);
        if (e.getUsuario() != null && e.getUsuario().getIdUsuario().equals(idActor)) throw ApiException.reglaDeNegocio("No puedes suspenderte a ti mismo");
        if (e.getUsuario() != null && ROL_ADMIN.equals(e.getUsuario().getRol().getCodigo())
                && usuarios.countByRol_CodigoAndEstado(ROL_ADMIN, EstadoUsuario.ACTIVO) <= 1) {
            throw ApiException.reglaDeNegocio("No puedes suspender al último administrador activo");
        }
        e.setEstado(EstadoEmpleado.SUSPENDIDO);
        if (e.getUsuario() != null) e.getUsuario().setEstado(EstadoUsuario.SUSPENDIDO);
        auditoria.registrar(TipoEvento.SUSPENSION, "Roles y personal", "Suspendió a " + e.getNombreCompleto() + " (" + e.getCodigo() + ")"
                + (motivo == null || motivo.isBlank() ? "" : ": " + motivo.trim()), NivelBitacora.ALERTA);
        return aEmpleado(e);
    }

    @Transactional
    public EmpleadoResponse reactivar(Long idEmpleado) {
        Empleado e = empleado(idEmpleado);
        e.setEstado(EstadoEmpleado.ACTIVO);
        if (e.getUsuario() != null) e.getUsuario().setEstado(EstadoUsuario.ACTIVO);
        auditoria.registrar(TipoEvento.SUSPENSION, "Roles y personal", "Reactivó a " + e.getNombreCompleto() + " (" + e.getCodigo() + ")", NivelBitacora.ALERTA);
        return aEmpleado(e);
    }

    /** Activa o desactiva un usuario (pantalla "Usuarios" del frontend). */
    @Transactional
    public UsuarioResponse cambiarEstadoUsuario(Long idUsuario, boolean activo, Long idActor) {
        Usuario u = usuarios.findById(idUsuario).orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
        if (!activo && u.getIdUsuario().equals(idActor)) throw ApiException.reglaDeNegocio("No puedes desactivar tu propia cuenta");
        if (!activo && ROL_ADMIN.equals(u.getRol().getCodigo()) && usuarios.countByRol_CodigoAndEstado(ROL_ADMIN, EstadoUsuario.ACTIVO) <= 1) {
            throw ApiException.reglaDeNegocio("No puedes desactivar al último administrador activo");
        }
        u.setEstado(activo ? EstadoUsuario.ACTIVO : EstadoUsuario.SUSPENDIDO);
        empleados.findByUsuario_IdUsuario(idUsuario).ifPresent(e -> e.setEstado(activo ? EstadoEmpleado.ACTIVO : EstadoEmpleado.SUSPENDIDO));
        auditoria.registrar(TipoEvento.SUSPENSION, "Usuarios", (activo ? "Activó" : "Desactivó") + " la cuenta de " + u.getNombreCompleto(), NivelBitacora.ALERTA);
        return aUsuario(u);
    }

    /** Edita los permisos de un rol. El rol de administrador no puede perder los permisos que lo mantienen gobernable. */
    @Transactional
    public RolResponse editarPermisosDeRol(Long idRol, Set<String> codigos) {
        Rol rol = roles.findById(idRol).orElseThrow(() -> ApiException.noEncontrado("Rol no encontrado"));
        List<Permiso> nuevos = permisos.findByCodigoIn(codigos);
        if (nuevos.size() != codigos.size()) throw ApiException.solicitudInvalida("Algún permiso indicado no existe");
        if (ROL_ADMIN.equals(rol.getCodigo()) && !codigos.containsAll(Set.of("ROL_EDITAR", "PANEL_ADMIN", "PERSONAL_GESTIONAR"))) {
            throw ApiException.reglaDeNegocio("El rol de administrador debe conservar PANEL_ADMIN, PERSONAL_GESTIONAR y ROL_EDITAR");
        }
        Set<String> antes = rol.getPermisos().stream().map(Permiso::getCodigo).collect(Collectors.toCollection(TreeSet::new));
        rol.getPermisos().clear();
        rol.getPermisos().addAll(nuevos);
        Set<String> agregados = new TreeSet<>(codigos);
        agregados.removeAll(antes);
        Set<String> quitados = new TreeSet<>(antes);
        quitados.removeAll(codigos);
        auditoria.registrar(TipoEvento.PERMISOS, "Roles y personal", "Modificó permisos del rol " + rol.getCodigo()
                + (agregados.isEmpty() ? "" : ": agregó " + String.join(", ", agregados))
                + (quitados.isEmpty() ? "" : (agregados.isEmpty() ? ": " : "; ") + "quitó " + String.join(", ", quitados)), NivelBitacora.CRITICO);
        return aRol(rol);
    }

    // ------------------------------------------------------------------ internos

    private Rol rol(String codigo) {
        return roles.findByCodigo(codigo).orElseThrow(() -> ApiException.solicitudInvalida("El rol \"" + codigo + "\" no existe"));
    }

    private Empleado empleado(Long id) {
        return empleados.findById(id).orElseThrow(() -> ApiException.noEncontrado("Empleado no encontrado"));
    }

    private boolean esSensible(Rol r) {
        return "NIVEL_1".equals(r.getNivelSeguridad()) || "NIVEL_2".equals(r.getNivelSeguridad());
    }

    private NivelBitacora nivelPara(Rol r) {
        return esSensible(r) ? NivelBitacora.CRITICO : NivelBitacora.INFO;
    }

    private String siguienteCodigo() {
        String ultimo = empleados.ultimoCodigo();
        int n = 100;
        if (ultimo != null && ultimo.matches("EMP-\\d+")) n = Integer.parseInt(ultimo.substring(4));
        return String.format("EMP-%04d", n + 1);
    }

    private EmpleadoResponse aEmpleado(Empleado e) {
        Usuario u = e.getUsuario();
        EstacionOperativa est = e.getEstacion();
        return new EmpleadoResponse(e.getId(), e.getCodigo(), e.getNombreCompleto(), e.getCargo(), e.getTurno(), e.getSalario(), e.getEstado(),
                e.getFechaIngreso(), e.getTelefono(), u == null ? null : u.getIdUsuario(), u == null ? null : u.getNombreUsuario(),
                u == null ? null : u.getRol().getCodigo(), u == null ? null : u.getRol().getNombre(),
                est == null ? null : est.getId(), est == null ? null : est.getNombre());
    }

    private RolResponse aRol(Rol r) {
        return new RolResponse(r.getIdRol(), r.getCodigo(), r.getNombre(), r.getDescripcion(), r.getNivelSeguridad(),
                usuarios.countByRol_CodigoAndEstado(r.getCodigo(), EstadoUsuario.ACTIVO),
                r.getPermisos().stream().map(Permiso::getCodigo).sorted().toList());
    }

    private UsuarioResponse aUsuario(Usuario u) {
        String turno = empleados.findByUsuario_IdUsuario(u.getIdUsuario()).map(e -> e.getTurno().name()).orElse(null);
        return new UsuarioResponse(u.getIdUsuario(), u.getNombreUsuario(), u.getNombreCompleto(), u.getCorreo(), u.getRol().getIdRol(),
                u.getRol().getNombre(), u.getEstado() == EstadoUsuario.ACTIVO ? 1 : 0, u.getCreadoEn(), u.getUltimoAcceso(), turno);
    }
}
