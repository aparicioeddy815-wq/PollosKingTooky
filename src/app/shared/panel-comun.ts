export const ESTILOS_PANEL = `
  .encabezado { display: flex; flex-wrap: wrap; align-items: flex-start; justify-content: space-between; gap: 1rem; }
  .acciones { display: flex; flex-wrap: wrap; align-items: center; gap: 0.5rem; }
  .tarjetas { display: grid; gap: 1rem; margin-bottom: 1.5rem; grid-template-columns: repeat(auto-fit, minmax(190px, 1fr)); }
  .tarjeta { display: grid; gap: 0.3rem; padding: 1.15rem 1.3rem; border-radius: var(--kt-radio); background: var(--kt-superficie); }
  .tarjeta span { font-size: 0.88rem; font-weight: 600; color: var(--kt-texto-suave); }
  .tarjeta strong { font-family: var(--kt-fuente-titulo); font-size: clamp(1.4rem, 2.2vw, 1.8rem); font-weight: 800; line-height: 1.1; }
  .tarjeta small { color: var(--kt-texto-tenue); }
  .tarjeta.alerta strong { color: var(--kt-primary-40); }
  .rejilla { display: grid; gap: 1.5rem; margin-bottom: 2rem; }
  @media (min-width: 960px) {
    .rejilla { grid-template-columns: 1fr 1fr; align-items: start; }
    .ancho { grid-column: 1 / -1; }
  }
  .bloque { min-width: 0; padding: 1.5rem; border-radius: var(--kt-radio-grande); background: var(--kt-superficie); }
  .bloque h2 { margin-bottom: 1rem; font-size: 1.15rem; }
  .separado { margin-bottom: 2rem; }
  .filtros { display: flex; flex-wrap: wrap; align-items: center; gap: 0.75rem; margin-bottom: 1rem; }
  .sub { font-size: 0.82rem; color: var(--kt-texto-tenue); }
  .vacio { padding: 2rem; text-align: center; color: var(--kt-texto-tenue); }
  .formulario { display: grid; gap: 1rem; }
  .campo { display: grid; gap: 0.35rem; min-width: 0; }
  .campo label { font-weight: 600; font-size: 0.9rem; }
  .fila { display: grid; gap: 1rem; grid-template-columns: 1fr 1fr; }
  .error { color: var(--kt-primary-40); }
  .pista { height: 8px; overflow: hidden; border-radius: 999px; background: var(--kt-superficie-alta); }
  .relleno { display: block; height: 100%; border-radius: 999px; background: var(--kt-secondary-40); transition: width 0.4s ease; }
  .paginador { display: flex; align-items: center; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem; color: var(--kt-texto-suave); font-size: 0.9rem; }
  .chip { display: inline-block; padding: 0.15rem 0.6rem; border-radius: 999px; background: var(--kt-superficie-alta); font-size: 0.8rem; font-weight: 600; }
  .chip.critica { background: var(--kt-primary-90); color: var(--kt-primary-20); }
  .chips { display: flex; flex-wrap: wrap; gap: 0.4rem; margin-top: 0.4rem; }
  .lista { display: grid; gap: 0.75rem; margin: 0; padding: 0; list-style: none; }
  .lista li { display: grid; gap: 0.3rem; padding: 0.9rem 1rem; border-radius: var(--kt-radio-chico); background: var(--kt-fondo); }
  .cabecera-item { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 0.5rem; }
`;

export type SeveridadTag = 'success' | 'info' | 'warn' | 'danger' | 'secondary';

export const TURNOS = [
  { etiqueta: 'Mañana', valor: 'MANANA' },
  { etiqueta: 'Tarde', valor: 'TARDE' },
];

export const MODALIDADES = [
  { etiqueta: 'Presencial', valor: 'PRESENCIAL' },
  { etiqueta: 'Virtual', valor: 'VIRTUAL' },
  { etiqueta: 'Telefónica', valor: 'TELEFONICA' },
];

export function textoTurno(turno: string | null | undefined): string {
  return turno === 'MANANA' ? 'Mañana' : turno === 'TARDE' ? 'Tarde' : '—';
}

const ESTADO_POSTULANTE: Record<string, { texto: string; severidad: SeveridadTag }> = {
  POSTULADO: { texto: 'Postulado', severidad: 'secondary' },
  ENTREVISTA_AGENDADA: { texto: 'Entrevista agendada', severidad: 'info' },
  EN_EVALUACION: { texto: 'En evaluación', severidad: 'warn' },
  APROBADO_GERENTE: { texto: 'Visto bueno gerencial', severidad: 'success' },
  EN_REVISION_ANTECEDENTES: { texto: 'Revisión de antecedentes', severidad: 'warn' },
  CONTRATADO: { texto: 'Contratado', severidad: 'success' },
  DESCARTADO: { texto: 'Descartado', severidad: 'danger' },
};

export function infoEstadoPostulante(estado: string): { texto: string; severidad: SeveridadTag } {
  return ESTADO_POSTULANTE[estado] ?? { texto: estado, severidad: 'secondary' };
}

/** Agrega la extensión al nombre de un archivo según el tipo que devolvió el servidor. */
export function nombreConExtension(base: string, blob: Blob): string {
  const tipo = blob.type.toLowerCase();
  const extension = tipo.includes('pdf')
    ? '.pdf'
    : tipo.includes('png')
      ? '.png'
      : tipo.includes('jpeg') || tipo.includes('jpg')
        ? '.jpg'
        : tipo.includes('wordprocessingml')
          ? '.docx'
          : tipo.includes('msword')
            ? '.doc'
            : tipo.includes('spreadsheetml')
              ? '.xlsx'
              : tipo.includes('html')
                ? '.html'
                : '';
  return `${base}${extension}`;
}
