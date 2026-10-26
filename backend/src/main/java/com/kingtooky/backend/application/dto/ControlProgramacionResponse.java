package com.kingtooky.backend.application.dto;

import java.time.LocalDate;

public class ControlProgramacionResponse {

    private LocalDate fecha;
    private Boolean semanalCorresponde;
    private Boolean quincenalCorresponde;

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Boolean getSemanalCorresponde() { return semanalCorresponde; }
    public void setSemanalCorresponde(Boolean semanalCorresponde) { this.semanalCorresponde = semanalCorresponde; }

    public Boolean getQuincenalCorresponde() { return quincenalCorresponde; }
    public void setQuincenalCorresponde(Boolean quincenalCorresponde) { this.quincenalCorresponde = quincenalCorresponde; }
}