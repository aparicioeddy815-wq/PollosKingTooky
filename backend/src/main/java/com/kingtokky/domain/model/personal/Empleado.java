package com.kingtokky.domain.model.personal;

import com.kingtokky.domain.model.seguridad.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "empleado")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre_completo", nullable = false, length = 255)
    private String nombreCompleto;

    @Column(name = "cargo", nullable = false, length = 60)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(name = "turno", nullable = false, length = 30)
    private Turno turno;

    @Column(name = "salario", nullable = false, precision = 12, scale = 2)
    private BigDecimal salario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoEmpleado estado = EstadoEmpleado.ACTIVO;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso = LocalDate.now();

    @Column(name = "telefono", length = 30)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_estacion")
    private EstacionOperativa estacion;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }

    public EstadoEmpleado getEstado() { return estado; }
    public void setEstado(EstadoEmpleado estado) { this.estado = estado; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public EstacionOperativa getEstacion() { return estacion; }
    public void setEstacion(EstacionOperativa estacion) { this.estacion = estacion; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
