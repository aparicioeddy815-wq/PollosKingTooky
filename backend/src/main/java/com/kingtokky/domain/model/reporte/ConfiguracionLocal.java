package com.kingtokky.domain.model.reporte;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion_local")
public class ConfiguracionLocal {

    @Id
    @Column(name = "clave", length = 60)
    private String clave;

    @Column(name = "valor", nullable = false)
    private String valor;

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}
