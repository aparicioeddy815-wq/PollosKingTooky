import { Pipe, PipeTransform } from '@angular/core';

const formato = new Intl.NumberFormat('es-BO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

/** Muestra un monto en bolivianos: 25 → "Bs 25,00". */
@Pipe({ name: 'bs' })
export class BolivianosPipe implements PipeTransform {
  transform(valor: number | null | undefined): string {
    return `Bs ${formato.format(valor ?? 0)}`;
  }
}
