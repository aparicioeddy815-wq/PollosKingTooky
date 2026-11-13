# King Tooky · Frontend

Sitio web y panel de gestión de **King Tooky** (pollo frito y hamburguesa), hecho con
**Angular 22 + PrimeNG 22**, TypeScript y SCSS. Se conecta al backend Spring Boot incluido en
`PollosKingTooky`.

## Cómo ejecutarlo

1. Levanta el backend (por defecto en `http://localhost:8080`).
2. En este proyecto:

```bash
npm install
npm start          # http://localhost:4200
```

En WebStorm: abre la carpeta, deja que corra `npm install` y ejecuta el script **start**.

El backend ya permite CORS desde `http://localhost:4200`. Si usas otra URL o puerto,
cámbialo en `src/environments/environment.development.ts` (`apiUrl`).

## Licencia de PrimeNG (importante)

PrimeNG 22 es parte de **PrimeUI** y pide una clave de licencia. Es **gratuita** con la
*Community License* para equipos pequeños y proyectos personales o estudiantiles:
https://primeui.dev/licenses/community

Sin clave, todo funciona pero aparece un aviso rojo "Invalid PrimeUI License" abajo a la
derecha. Para quitarlo, pega tu clave en `licenciaPrimeUi` dentro de
`src/environments/environment.ts` y `environment.development.ts`.

## Usuarios de prueba

Son los que crea el backend al arrancar (`DataInitializer`); revisa esa clase para ver
usuarios y contraseñas. Cada rol ve solo las secciones para las que tiene permiso.

## Estructura

```
src/
├── environments/          URL del backend y licencia
├── styles/                SCSS global: tokens de la paleta, mixins, base, ajustes PrimeNG
└── app/
    ├── core/              Lo que usa toda la app
    │   ├── models/        Interfaces que reflejan los DTO del backend
    │   ├── services/      Un servicio por área del backend (auth, pedidos, reservas…)
    │   ├── interceptors/  Token JWT y renovación automática de sesión
    │   ├── guards/        Protección de rutas por sesión y por permiso
    │   ├── theme/         Preset de PrimeNG con la paleta oficial + textos en español
    │   └── utils/         Errores, fechas y etiquetas
    ├── layout/            Navbar (con logo), footer, layout del panel y de “mi cuenta”
    ├── shared/            Piezas reutilizables (tarjeta de producto, etiquetas de estado…)
    └── pages/
        ├── publico/       Inicio (carrusel), menú, turnos, nosotros, ubicación
        ├── acceso/        Ingresar, registrarse, recuperar contraseña, perfil
        ├── cuenta/        Cliente: reservas, menú del salón, quejas, evaluación
        └── panel/         Personal: resumen, pedidos, nuevo pedido, productos, reservas, usuarios
```

Cada vista es un componente independiente con sus tres archivos (`.ts`, `.html`, `.scss`).

## Paleta

Primary `#D32F1E` · Secondary `#F47C18` · Tertiary `#E5A93B` · Neutral `#241715`, con sus
escalas tonales en `src/styles/_tokens.scss`. Tipografías: Epilogue (títulos) y
Plus Jakarta Sans (texto), instaladas por npm y sin depender de Google Fonts.

## Scripts

| Comando         | Qué hace                         |
|-----------------|----------------------------------|
| `npm start`     | Servidor de desarrollo           |
| `npm run build` | Compilación de producción (`dist/`) |
