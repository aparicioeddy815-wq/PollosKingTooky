package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.PersonalDtos.ActualizarEmpleadoRequest;
import com.kingtokky.application.dto.PersonalDtos.AsignarEmpleadoRequest;
import com.kingtokky.application.dto.PersonalDtos.CambiarRolRequest;
import com.kingtokky.application.dto.PersonalDtos.CrearPerfilRequest;
import com.kingtokky.application.dto.PersonalDtos.EmpleadoResponse;
import com.kingtokky.application.dto.PersonalDtos.EstacionResponse;
import com.kingtokky.application.dto.PersonalDtos.MotivoRequest;
import com.kingtokky.application.dto.PersonalDtos.PerfilCreadoResponse;
import com.kingtokky.application.dto.PersonalDtos.PermisoResponse;
import com.kingtokky.application.dto.PersonalDtos.PermisosRolRequest;
import com.kingtokky.application.dto.PersonalDtos.ResumenPersonalResponse;
import com.kingtokky.application.dto.PersonalDtos.RolResponse;
import com.kingtokky.application.dto.PersonalDtos.UsuarioResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.application.service.ExportacionService;
import com.kingtokky.application.service.PersonalService;
import com.kingtokky.domain.model.personal.Turno;
import com.kingtokky.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Roles y personal", description = "Personal, estaciones operativas, perfiles de acceso, roles y permisos")
public class PersonalController {

    private final PersonalService personal;
    private final ExportacionService exportacion;

    public PersonalController(PersonalService personal, ExportacionService exportacion) {
        this.personal = personal;
        this.exportacion = exportacion;
    }

    // ------------------------------------------------------------------ personal
    @GetMapping("/personal/resumen")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    @Operation(summary = "Tarjetas: total de empleados, perfiles creados, turno actual y ocupación de puestos")
    public ResumenPersonalResponse resumen() {
        return personal.resumen();
    }

    @GetMapping("/personal")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    @Operation(summary = "Personal registrado; filtros por texto, rol (ROL-ADMIN, ROL-GERENTE, ROL-DESPACHO) y turno (MANANA, TARDE)")
    public PaginaResponse<EmpleadoResponse> listar(@RequestParam(required = false) String q, @RequestParam(required = false) String rol,
                                                   @RequestParam(required = false) Turno turno, @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return personal.listar(q, rol, turno, page, size);
    }

    @GetMapping("/personal/estaciones")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    @Operation(summary = "Estaciones operativas: puestos totales, ocupados (%) y quién ocupa cada puesto")
    public List<EstacionResponse> estaciones() {
        return personal.estaciones();
    }

    @PostMapping("/personal/perfiles")
    @PreAuthorize("hasAuthority('PERSONAL_GESTIONAR')")
    @Operation(summary = "Crea el perfil de acceso de un colaborador; si no se envía contraseña se genera una temporal")
    public ResponseEntity<PerfilCreadoResponse> crearPerfil(@Valid @RequestBody CrearPerfilRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personal.crearPerfil(req));
    }

    @PostMapping("/personal/empleados")
    @PreAuthorize("hasAuthority('PERSONAL_GESTIONAR')")
    @Operation(summary = "Asigna (registra) un empleado nuevo")
    public ResponseEntity<EmpleadoResponse> asignar(@Valid @RequestBody AsignarEmpleadoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personal.asignarEmpleado(req));
    }

    @PutMapping("/personal/{id}")
    @PreAuthorize("hasAuthority('PERSONAL_GESTIONAR')")
    public EmpleadoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarEmpleadoRequest req) {
        return personal.actualizarEmpleado(id, req);
    }

    @PutMapping("/personal/{id}/rol")
    @PreAuthorize("hasAuthority('PERSONAL_GESTIONAR')")
    @Operation(summary = "Editar rol: los cambios de nivel 1 y 2 quedan como eventos críticos en la bitácora")
    public EmpleadoResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody CambiarRolRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return personal.cambiarRol(id, req.rolCodigo(), u.idUsuario());
    }

    @PatchMapping("/personal/{id}/suspender")
    @PreAuthorize("hasAuthority('PERSONAL_GESTIONAR')")
    public EmpleadoResponse suspender(@PathVariable Long id, @RequestBody(required = false) MotivoRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return personal.suspender(id, req == null ? null : req.motivo(), u.idUsuario());
    }

    @PatchMapping("/personal/{id}/reactivar")
    @PreAuthorize("hasAuthority('PERSONAL_GESTIONAR')")
    public EmpleadoResponse reactivar(@PathVariable Long id) {
        return personal.reactivar(id);
    }

    @GetMapping("/personal/exportar")
    @PreAuthorize("hasAuthority('PERSONAL_LEER')")
    @Operation(summary = "Nómina del personal en csv, xlsx o pdf")
    public ResponseEntity<org.springframework.core.io.Resource> exportar(@RequestParam(defaultValue = "xlsx") String formato, @RequestParam(required = false) String rol) {
        List<List<Object>> filas = personal.listarParaNomina(rol).stream().map(e -> List.<Object>of(e.codigo(), e.nombreCompleto(), e.cargo(), e.turno().name(),
                e.salario(), e.estado().name(), e.fechaIngreso().toString(), e.rol() == null ? "Sin perfil" : e.rol(), e.estacion() == null ? "" : e.estacion())).toList();
        return Descargas.de(exportacion.exportar(formato, "nomina", "Nómina del personal", List.of("Código", "Colaborador", "Cargo", "Turno", "Salario (Bs)",
                "Estado", "Ingreso", "Rol", "Estación"), filas));
    }

    // ------------------------------------------------------------------ roles y permisos
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROL_LEER')")
    @Operation(summary = "Roles con su nivel de seguridad, usuarios activos y permisos")
    public List<RolResponse> roles() {
        return personal.listarRoles();
    }

    @GetMapping("/roles/permisos")
    @PreAuthorize("hasAuthority('ROL_LEER')")
    public List<PermisoResponse> permisos() {
        return personal.listarPermisos();
    }

    @GetMapping("/roles/{id}/usuarios")
    @PreAuthorize("hasAuthority('ROL_LEER')")
    @Operation(summary = "Usuarios que tienen el rol")
    public List<UsuarioResponse> usuariosDeRol(@PathVariable Long id) {
        return personal.usuariosDeRol(id);
    }

    @PutMapping("/roles/{id}/permisos")
    @PreAuthorize("hasAuthority('ROL_EDITAR')")
    @Operation(summary = "Reemplaza los permisos de un rol (evento crítico en la bitácora)")
    public RolResponse editarPermisos(@PathVariable Long id, @Valid @RequestBody PermisosRolRequest req) {
        return personal.editarPermisosDeRol(id, req.permisos());
    }

    // ------------------------------------------------------------------ usuarios (pantalla genérica del frontend)
    @GetMapping("/usuarios")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    @Operation(summary = "Usuarios del sistema; estado 1 = activo, 0 = inactivo")
    public PaginaResponse<UsuarioResponse> usuarios(@RequestParam(required = false) String q, @RequestParam(required = false) Integer estado,
                                                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return personal.listarUsuarios(q, estado, page, size);
    }

    @PatchMapping("/usuarios/{id}/activar")
    @PreAuthorize("hasAuthority('USUARIO_ESCRIBIR')")
    public UsuarioResponse activar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado u) {
        return personal.cambiarEstadoUsuario(id, true, u.idUsuario());
    }

    @PatchMapping("/usuarios/{id}/desactivar")
    @PreAuthorize("hasAuthority('USUARIO_ESCRIBIR')")
    public UsuarioResponse desactivar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado u) {
        return personal.cambiarEstadoUsuario(id, false, u.idUsuario());
    }
}
