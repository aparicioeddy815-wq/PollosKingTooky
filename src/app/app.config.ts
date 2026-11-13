import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import localeEs from '@angular/common/locales/es';
import { ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { providePrimeNG } from 'primeng/config';
import { environment } from '../environments/environment';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { KingTookyPreset } from './core/theme/king-tooky-preset';
import { TRADUCCION_ES } from './core/theme/primeng-es';

registerLocaleData(localeEs);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding(), withInMemoryScrolling({ scrollPositionRestoration: 'top' })),
    provideHttpClient(withInterceptors([authInterceptor])),
    providePrimeNG({
      theme: { preset: KingTookyPreset, options: { darkModeSelector: false } },
      translation: TRADUCCION_ES,
      license: environment.licenciaPrimeUi || undefined,
    }),
    MessageService,
    ConfirmationService,
    { provide: LOCALE_ID, useValue: 'es' },
  ],
};
