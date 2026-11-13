package com.kingtokky.application.dto;

import com.kingtokky.domain.model.personal.EstadoEmpleado;
import com.kingtokky.domain.model.personal.Turno;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class PersonalDtos {

    private PersonalDtos() {}

    public record ResumenPersonalResponse(long totalEmpleados, long empleadosActivos, long suspendidos, long perfilesCreados,
                                          Turno turnoActual, long activosEnTurnoActual, long totalPuestos, long puestosOcupados,
                                          double ocupacionPorcentaje) {}

    public record EmpleadoResponse(Long idEmpleado, String codigo, String nombreCompleto, String cargo, Turno turno, BigDecimal salario,
                                   EstadoEmpleado estado, LocalDate fechaIngreso, String telefono, Long idUsuario, String nombreUsuario,
                                   String rolCodigo, String rol, Long idEstacion, String estacion) {}

    public record PuestoResponse(int numero, boolean ocupado, String cargo, String empleado) {}

    public record EstacionResponse(Long idEstacion, String nombre, int puestosTotales, int ocupados, double porcentaje, List<PuestoResponse> puestos) {}

    public record RolResponse(Long idRol, String codigo, String nombre, String descripcion, String nivelSeguridad, long usuariosActivos,
                              List<String> permisos) {}

    public record PermisoResponse(String codigo, String descripcion) {}

    public record CrearPerfilRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$", message = "debe tener de 3 a 50 caracteres: letras, números, punto, guion o guion bajo") String nombreUsuario,
            @NotBlank @Email @Size(max = 255) String correo,
            @NotBlank @Size(max = 255) String nombreCompleto,
            @NotBlank String rolCodigo,
            @Size(min = 6, max = 100) String contrasena,
            Long idEmpleado) {}

    public record PerfilCreadoResponse(Long idUsuario, String nombreUsuario, String rolCodigo, String contrasenaTemporal) {}

    public record AsignarEmpleadoRequest(@NotBlank @Size(max = 255) String nombreCompleto, @NotBlank @Size(max = 60) String cargo,
                                         @NotNull Turno turno, @NotNull @DecimalMin("0.0") BigDecimal salario, Long idEstacion,
                                         @Size(max = 30) String telefono) {}

    public record ActualizarEmpleadoRequest(@Size(max = 60) String cargo, Turno turno, @DecimalMin("0.0") BigDecimal salario, Long idEstacion) {}

    public record CambiarRolRequest(@NotBlank String rolCodigo) {}

    public record MotivoRequest(@Size(max = 300) String motivo) {}

    public record PermisosRolRequest(@NotEmpty Set<String> permisos) {}

    /** Forma de "usuario" que ya consume la pantalla de usuarios del frontend (estado 1 = activo, 0 = inactivo). */
    public record UsuarioResponse(Long idUsuario, String nombreUsuario, String nombreCompleto, String correo, Long idRol, String rol,
                                  int estado, LocalDateTime fechaCreacion, LocalDateTime ultimoAcceso, String turno) {}
}
