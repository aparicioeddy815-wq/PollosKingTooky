import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';

// Escala primaria de PrimeNG construida con el rojo de marca (#D32F1E).
export const KingTookyPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#ffedea',
      100: '#ffdad4',
      200: '#ffb4a8',
      300: '#ff8a77',
      400: '#ee5a45',
      500: '#d32f1e',
      600: '#bf2214',
      700: '#af1106',
      800: '#920400',
      900: '#680200',
      950: '#410100',
    },
    colorScheme: {
      light: {
        surface: {
          0: '#ffffff',
          50: '#fff8f6',
          100: '#ffedea',
          200: '#fce3df',
          300: '#f6ddd9',
          400: '#d9c1bd',
          500: '#bda6a3',
          600: '#86736f',
          700: '#6c5a57',
          800: '#544340',
          900: '#3c2d2a',
          950: '#241715',
        },
      },
    },
  },
});
