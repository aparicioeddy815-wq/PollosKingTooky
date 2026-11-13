package com.kingtokky.domain.regla;

import java.time.LocalDate;
import java.time.LocalTime;

/** Reglas simples de calendario: turno del personal y fechas de control de inventario. */
public final class ReglasPersonal {

    private ReglasPersonal() {}

    /** El turno mañana va hasta las 15:00; desde ahí es turno tarde. */
    public static boolean esTurnoManana(LocalTime hora) {
        return hora.isBefore(LocalTime.of(15, 0));
    }

    public static boolean esDiaDeControlSemanal(LocalDate fecha) {
        return fecha.getDayOfWeek() == java.time.DayOfWeek.SATURDAY;
    }

    /** Control quincenal: día 15 y día 30 (o el último día si el mes es más corto). */
    public static boolean esDiaDeControlQuincenal(LocalDate fecha) {
        int ultimo = Math.min(30, fecha.lengthOfMonth());
        return fecha.getDayOfMonth() == 15 || fecha.getDayOfMonth() == ultimo;
    }

    public static LocalDate proximoSabado(LocalDate desde) {
        LocalDate f = desde;
        while (!esDiaDeControlSemanal(f)) f = f.plusDays(1);
        return f;
    }

    public static LocalDate proximaQuincena(LocalDate desde) {
        LocalDate f = desde;
        while (!esDiaDeControlQuincenal(f)) f = f.plusDays(1);
        return f;
    }
}
