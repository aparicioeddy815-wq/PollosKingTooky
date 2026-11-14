/** Formatea una fecha local como yyyy-MM-dd sin desfases de zona horaria. */
export function aFechaIso(fecha: Date): string {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

/** Formatea la hora de una fecha como HH:mm. */
export function aHoraIso(fecha: Date): string {
  const horas = String(fecha.getHours()).padStart(2, '0');
  const minutos = String(fecha.getMinutes()).padStart(2, '0');
  return `${horas}:${minutos}`;
}

/** "2026-11-13" → "13/11", para los ejes de las gráficas. */
export function etiquetaDia(fechaIso: string): string {
  const [, mes, dia] = fechaIso.split('-');
  return `${dia}/${mes}`;
}
