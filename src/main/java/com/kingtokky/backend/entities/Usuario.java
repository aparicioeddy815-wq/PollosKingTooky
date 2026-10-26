package com.kingtokky.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    private String idUsuario;

    private String nombreCompleto;
    private String nombreUsuario;
    private String correoElectronico;
    private String contrasenaHash;
    private String fotoPerfil;
    private String idRol;
    private LocalDateTime fechaIngreso;
    private String estado;
}