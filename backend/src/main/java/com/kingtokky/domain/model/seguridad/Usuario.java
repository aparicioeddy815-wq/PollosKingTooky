package com.kingtokky.domain.model.seguridad;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @Column(name = "correo", nullable = false, unique = true, length = 255)
    private String correo;

    @Column(name = "contrasena_hash", length = 100)
    private String contrasenaHash;

    @Column(name = "nombre_completo", nullable = false, length = 255)
    private String nombreCompleto;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "proveedor", nullable = false, length = 30)
    private ProveedorAcceso proveedor = ProveedorAcceso.LOCAL;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso = LocalDate.now();

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "total_accesos", nullable = false)
    private Integer totalAccesos = 0;

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }

    public ProveedorAcceso getProveedor() { return proveedor; }
    public void setProveedor(ProveedorAcceso proveedor) { this.proveedor = proveedor; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    public Integer getTotalAccesos() { return totalAccesos; }
    public void setTotalAccesos(Integer totalAccesos) { this.totalAccesos = totalAccesos; }
}
