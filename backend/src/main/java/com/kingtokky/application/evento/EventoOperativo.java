package com.kingtokky.application.evento;

/** Evento que debe llegar en tiempo real a las pantallas conectadas (comandas, turnos, menú). */
public record EventoOperativo(String canal, String nombre, Object datos) {

    public static final String CANAL_COMANDAS = "comandas";
    public static final String CANAL_PUBLICO = "publico";
}
