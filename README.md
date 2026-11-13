# King Tokky · Backend del sistema web

Backend profesional para el restaurante **King Tokky** (pollo frito y hamburguesas), hecho con **Java 21 + Spring Boot 3.3**,
**PostgreSQL 16**, JWT, Swagger y Docker. Está construido a partir del *documento de funcionamiento* y del *cronograma* del proyecto,
y expone una API lista para conectarse con el frontend Angular (contratos compatibles con el frontend ya entregado).

```
king-tokky/
├── docker-compose.yml      ← levanta base de datos, backend y bandeja de correos de prueba
├── init/                   ← scripts SQL que PostgreSQL ejecuta solo la primera vez (esquema + datos)
│   ├── 01-esquema.sql              38 tablas, claves foráneas, restricciones e índices
│   ├── 02-datos-base.sql           permisos, roles, configuración del local, categorías, proveedores
│   ├── 03-usuarios-y-personal.sql  8 colaboradores con acceso + 62 clientes + 26 empleados
│   ├── 04-menu-e-insumos.sql       34 productos, 45 insumos, 176 líneas de receta, favoritos
│   ├── 05-pedidos-y-reservas.sql   60 días de pedidos (≈3.100) y 160 reservas
│   ├── 06-movimientos-de-inventario.sql  kardex, entradas, mermas, alertas y controles físicos
│   ├── 07-gerencia-y-auditoria.sql quejas, evaluaciones, entrevistas, bitácora, reportes, respaldos
│   └── rollback/rollback.sql       deshace el esquema (no se ejecuta solo)
├── backend/                ← proyecto Maven (Spring Boot) con su Dockerfile
├── postman/                ← colección con los 130 endpoints
├── docs/ENDPOINTS.md       ← tabla de endpoints con el permiso de cada uno
├── .env.example            ← variables opcionales (todo funciona sin crear .env)
└── README.md
```

## 1. Ejecutarlo con Docker

Requisitos: Docker Desktop (o Docker Engine + Compose). Nada más.

```bash
docker compose up --build
```

La primera vez tarda unos minutos (descarga imágenes y compila el backend). Cuando veas `Started KingTokkyApplication`:

| Qué | Dónde |
|---|---|
| API y Swagger (documentación interactiva) | http://localhost:8080/swagger-ui.html |
| Estado del backend | http://localhost:8080/actuator/health |
| Bandeja de correos de prueba (recuperar contraseña, reservas) | http://localhost:8025 |
| PostgreSQL | `localhost:5432` · base `king_tokky` · usuario/clave `kingtokky` |
| pgAdmin (opcional) | `docker compose --profile tools up` → http://localhost:5050 (`admin@kingtokky.bo` / `admin`) |

**Volver a empezar con los datos originales:** `docker compose down -v` y luego `docker compose up`
(los scripts de `init/` solo corren cuando el volumen de la base está vacío).

### Cuentas de demostración

Contraseña de **todas**: `KingTokky2026*`

| Rol | Usuarios | Qué ve |
|---|---|---|
| Administrador (4 socios) | `admin`, `socio2`, `socio3`, `socio4` | Dashboard de dueños, bitácora, personal y roles, entrevistados, reportes, respaldos |
| Gerente (2) | `gerente`, `gerente2` | Dashboard operativo, inventario, quejas, entrevistas, menú y precios, reservas |
| Despacho (2) | `despacho`, `despacho2` | Comandas en vivo, nuevo pedido, cobro de reservas en caja, cierres de caja |
| Cliente (62) | `cliente` (Valeria Mendoza) y otros | Reservas, menú del salón, favoritos, quejas, evaluación diaria, perfil |

Los pasos para probar rápido en Swagger: `POST /api/auth/login` → copiar `accessToken` → botón **Authorize** → probar cualquier endpoint.

## 2. Arquitectura (capas del cronograma)

```
backend/src/main/java/com/kingtokky/
├── domain/            Núcleo del negocio, sin depender de Spring Web
│   ├── model/         37 entidades JPA y 26 enums, por módulo (seguridad, personal, menu, inventario, pedido, reserva…)
│   ├── regla/         Reglas puras y probadas: estados del pedido, reservas, proyección de stock, calendario de controles
│   ├── port/          Puertos de salida: correo, almacenamiento de archivos, verificación de Google
│   └── repository/    Repositorios Spring Data (consultas JPQL)
├── application/       Casos de uso
│   ├── service/       20 servicios: Auth, Pedido, Reserva, Inventario, ControlInventario, Entrevista, Personal, Dashboard, Reporte, Respaldo…
│   ├── dto/           Entradas y salidas de la API (records con validaciones)
│   └── evento/        Eventos de tiempo real
├── infrastructure/    Adaptadores
│   ├── security/      JWT, filtro, configuración de Spring Security, CORS
│   ├── audit/         Interceptor de auditoría (registra cada cambio en la bitácora)
│   ├── sse/           Server-Sent Events (comandas, turnos, menú)
│   ├── storage/ mail/ google/ export/ (CSV, Excel, PDF)   scheduler/ (tareas programadas)
└── presentation/      Controladores REST (17) y manejo uniforme de errores
```

Decisiones de diseño: capas separadas con puertos y adaptadores para lo externo, `open-in-view` desactivado, DTOs en `record`,
transacciones en la capa de servicio, permisos con `@PreAuthorize("hasAuthority('…')")` y auditoría automática.

## 3. Roles y permisos

Hay 4 roles (`ROL-ADMIN`, `ROL-GERENTE`, `ROL-DESPACHO`, `ROL-CLIENTE`) y 27 permisos. El administrador puede editar los permisos de un
rol desde la API (`PUT /api/roles/{id}/permisos`); cada cambio de nivel 1 o 2 queda como evento **crítico** en la bitácora.

| Permiso | Admin | Gerente | Despacho | Cliente |
|---|:-:|:-:|:-:|:-:|
| `PANEL_ADMIN` / `PANEL_GERENTE` | ✔ / – | – / ✔ | – | – |
| `PEDIDO_LEER`, `PEDIDO_CREAR`, `PEDIDO_ESTADO` | leer | leer | ✔ | – |
| `PRODUCTO_LEER` / `PRODUCTO_ESCRIBIR` | ✔ / ✔ | ✔ / ✔ | ✔ / – | – |
| `RESERVA_GESTIONAR` (cobrar en caja) | – | ✔ | ✔ | – |
| `INVENTARIO_LEER` / `INVENTARIO_ESCRIBIR` | ✔ / – | ✔ / ✔ | – | – |
| `QUEJA_GESTIONAR`, `ENTREVISTA_GESTIONAR` | – | ✔ | – | – |
| `ENTREVISTA_DECIDIR` (contratar / descartar) | ✔ | – | – | – |
| `PERSONAL_*`, `ROL_*`, `USUARIO_*` | gestión total | solo lectura | – | – |
| `BITACORA_*`, `REPORTE_*`, `RESPALDO_GESTIONAR` | ✔ | – | – | – |
| `CAJA_CIERRE` | ✔ | ✔ | ✔ | – |
| `CLIENTE_PORTAL`, `RESERVA_CREAR` | – | – | – | ✔ |

## 4. Cobertura del cronograma

| Fase / Sprint | Hitos | Dónde está |
|---|---|---|
| **F2** Diseño de BD | Modelo relacional, claves, índices | `init/01-esquema.sql` (38 tablas) |
| **F3 · S1** Núcleo y seguridad | Arquitectura limpia, Swagger, CORS | paquetes `domain/application/infrastructure/presentation`; `/swagger-ui.html` |
| | Flyway con rollback y datos semilla | `init/` es la base (V1); Flyway toma el relevo desde `V2__indices_de_consulta.sql`; `init/rollback/rollback.sql` |
| | Login por usuario o correo, registro, BCrypt, JWT, refresh, «Recordarme» | `AuthController`/`AuthService` (acceso 30 min; refresh 1 día o 30 con «Recordarme»; bloqueo tras 5 intentos fallidos) |
| | Roles y permisos | matriz de la sección 3; el token trae los permisos para el menú lateral dinámico |
| | Perfil personal | `/api/perfil` (nombre, correo, foto, fecha de ingreso, último acceso, frecuencia semanal de visitas) |
| | Bitácora con interceptor | `AuditoriaInterceptor` + eventos explícitos de los servicios |
| **F4 · S2** Inventario y personal | Gestión de personal | `/api/personal` (perfil, asignar empleado, editar rol, suspender/reactivar, nómina CSV/Excel/PDF) |
| | Recuperación de contraseña | enlace por correo, vence en 60 min, uso único (los correos de prueba llegan a Mailpit) |
| | Consulta y exportación de la bitácora | `/api/bitacora` con filtros, resumen del día y exportación CSV/PDF |
| | Inventario, entradas y mermas, kardex | `/api/inventario/**` |
| | Alertas y proyección | tarea cada 15 min; déficit proyectado y superávit seguro a 7 días |
| | Controles semanal y quincenal | sábados (alimentos) y días 15 y 30 (utensilios, mesas y vasos); se abren solos a las 07:00 |
| | Disponibilidad y precios del menú | `/api/productos` (Disponible/Agotado con un clic, publicación en PedidosYA, ajuste de precio auditado) |
| **F5 · S3** Despacho y entrevistas | SSE | `/api/stream/comandas` (con token) y `/api/publico/eventos` |
| | Páginas públicas | `/api/publico/inicio`, `/menu`, `/horarios`, `/ubicacion`, `/sobre-nosotros`, `/turnos` |
| | Comandas, estados, turno diario | `EN_PREPARACION → LISTO → ENTREGADO`, turno que reinicia cada día con bloqueo de concurrencia |
| | Timbre y aviso sonoro | eventos `pedido-listo` y `timbre` para que el frontend suene |
| | Consumo de insumos por pedido | descuento por receta ligado al número de pedido y al kardex |
| | Entrevistas (gerente) | agenda, postulantes, observaciones, archivos y CV (`/api/entrevistas`, `/api/postulantes`) |
| **F6 · S4** Cliente frecuente y gerencia | Reservas en 3 pasos | `/api/reservas/dias` → `/horarios` → `POST /api/reservas` |
| | Comprobantes | historial, detalle, recibo en PDF, cancelar y reprogramar (hasta 60 min antes) |
| | Menú de salón y favoritos | `/api/cliente/menu-salon`, me gusta, veces consumidas |
| | Ayuda, quejas y evaluación diaria | `/api/cliente/**` |
| | Gestión de quejas y dashboard del gerente | `/api/quejas`, `/api/dashboard/gerente` |
| | Inicio de sesión con Google | `POST /api/auth/google` (requiere `GOOGLE_CLIENT_ID`) |
| **F7 · S5** Administración y cierre | Dashboard del administrador | `/api/dashboard/admin` (ventas, margen, sesiones en vivo, ritmo por hora, top del menú) |
| | Entrevistados y contratación | `/api/entrevistados/**`: contratar crea al empleado; exige visto bueno y documentos completos |
| | Reportes y archivo oficial | `/api/reportes/**`: resumen mensual, Excel, PDF imprimible, informes programados |
| | Respaldo cada 12 horas | tarea 03:00 y 15:00; vuelca la base a `.sql` y lo registra en bitácora; `/api/respaldos` |
| | Pruebas | JUnit y Mockito en `backend/src/test` + colección Postman con los 130 endpoints |
| | Despliegue | `docker compose up --build` |

## 5. Reglas de negocio (y cómo se interpretaron)

* **Reservas:** el pago se hace **en caja**, no en línea. El cliente debe pasar de 5 a 10 minutos antes de su hora; si la hora llega sin pago,
  una tarea (cada minuto) la cancela como `CANCELADA_AUTO`. Se puede reservar hasta 7 días adelante, con 30 min de anticipación mínima,
  en horas cada 30 min de 11:00 a 21:00. Aforo de 40 personas por hora de reloj (configurable en `configuracion_local.aforo_reservas`).
  Máximo 3 reservas pendientes por cliente. Al **cobrar**, los platos pasan a cocina como comanda del canal `RESERVA`.
* **Menú:** el menú público muestra solo lo publicado en PedidosYA e indica si cada plato está disponible. El menú del salón añade los platos exclusivos del local.
* **Pedidos:** un producto agotado no se puede registrar; el número de turno es correlativo por día. Solo se puede anular una comanda **en preparación**.
* **Stock:** `CRITICO` si el stock ≤ umbral, `BAJO` si ≤ umbral × 1,2, `NORMAL` en otro caso. La proyección usa el consumo promedio diario real (últimos 14 días).
* **Tiempo de despacho:** promedio entre la creación y el «listo» de la comanda, comparado con el objetivo de `configuracion_local.objetivo_despacho_min` (8 min).
* **Seguridad del personal:** no se puede quitar el rol, suspender ni desactivar al último administrador activo ni a uno mismo.

> **Datos de ejemplo que debes reemplazar:** la dirección, coordenadas, WhatsApp, correo de contacto y la historia del local
> (tabla `configuracion_local`) son textos de muestra; el documento no los traía. Edítalos en `init/02-datos-base.sql` o directamente en la tabla.
> Los nombres de personas, clientes y postulantes son ficticios.

## 6. Conexión con el frontend Angular

* **URL base:** `http://localhost:8080` (en el frontend: `src/environments/environment*.ts → apiUrl`). CORS ya permite `http://localhost:4200`.
* **Autenticación:** enviar `Authorization: Bearer <accessToken>`. Al recibir `401`, llamar a `POST /api/auth/refresh` (el interceptor del frontend ya lo hace).
* **Errores:** siempre `{ timestamp, status, error, mensaje, ruta, detalles[] }`.
* **Páginas:** `{ contenido, pagina, tamano, totalElementos, totalPaginas }`, con `?page=0&size=20`.
* **Fechas:** `yyyy-MM-dd`; horas `HH:mm`; fecha y hora `yyyy-MM-ddTHH:mm:ss`. Dinero en bolivianos (número).
* **Tiempo real (SSE):** `new EventSource('http://localhost:8080/api/stream/comandas?access_token=' + token)` (el navegador no permite cabeceras en `EventSource`).
  Eventos: `pedido-creado`, `pedido-listo`, `pedido-actualizado`, `timbre`. Público, sin token: `/api/publico/eventos` → `turnos-actualizados`, `timbre`, `menu-actualizado`.
* **Descargas** (recibos, reportes, bitácora, respaldos): usar `responseType: 'blob'`; el nombre sugerido llega en `Content-Disposition` (ya expuesto por CORS).
* **Subidas:** `multipart/form-data` — foto de perfil (`foto`) y documentos de postulantes (`tipo` + `archivo`), máximo 5 MB.

**Compatibilidad con el frontend entregado:** las rutas y campos de inicio de sesión, perfil, sitio público, productos, pedidos, reservas, portal del cliente,
usuarios y dashboard del gerente coinciden con los modelos de ese frontend. Diferencias a tener en cuenta:

1. Los estados de pedido ahora son `EN_PREPARACION`, `LISTO`, `ENTREGADO`, `CANCELADO` (ya no existe `PENDIENTE`: el botón «Preparar» sobra).
2. El rol llega como `ADMINISTRADOR`, `GERENTE`, `DESPACHO` o `CLIENTE` (y `rolCodigo` con el código completo).
3. El menú lateral dinámico se arma con `permisos`; el administrador entra primero a *Pedidos* porque no tiene `PANEL_GERENTE`.
4. Pantallas **nuevas** que el backend ya soporta y el frontend aún no tiene: inventario y controles, mermas, quejas del gerente, entrevistas y entrevistados,
   personal y roles, bitácora, dashboard del administrador, reportes, respaldos, caja, y el asistente de reservas de 3 pasos (`/dias`, `/horarios`).

## 7. Configuración

Todo tiene un valor por defecto; para cambiarlo, copia `.env.example` a `.env`.

| Variable | Para qué | Por defecto |
|---|---|---|
| `JWT_SECRETO` | Clave de firma de tokens (≥ 32 caracteres) | clave de desarrollo — **cámbiala en producción** |
| `GOOGLE_CLIENT_ID` | Activa «Iniciar sesión con Google» | vacío (el endpoint responde 503) |
| `FRONTEND_URL` / `CORS_ORIGENES` | Enlaces de correos y orígenes permitidos | `http://localhost:4200` |
| `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_PORT` | Base de datos | `king_tokky`, `kingtokky`, `kingtokky`, `5432` |

Parámetros de negocio (reservas, proyección de inventario, objetivo de despacho) en `backend/src/main/resources/application.yml` y en la tabla `configuracion_local`.

## 8. Pruebas

```bash
cd backend
mvn test                 # reglas de dominio, JWT, exportador y servicios con Mockito
```
La colección `postman/King-Tokky.postman_collection.json` incluye los 130 endpoints con ejemplos; el login guarda el token automáticamente.

## 9. Estado de la verificación (transparencia)

Se comprobó en el entorno de desarrollo:

* ✔ Los 7 scripts de `init/` se ejecutan **de corrido sobre un PostgreSQL 16 real, sin errores**, en el mismo orden y modo que usa Docker (`ON_ERROR_STOP`).
  Se revisaron los volúmenes de datos, la coherencia del kardex (saldo final = stock actual, sin saldos negativos) y las cifras de dashboard.
* ✔ Las 284 columnas mapeadas por las entidades JPA existen en la base (Hibernate corre en modo `validate`).
* ✔ Las reglas de dominio se ejecutaron con su batería de casos (todas correctas); el código Java pasó el análisis de `javac` sin errores de sintaxis ni de tipos propios.
* ✔ Los 130 endpoints de la colección Postman y de `docs/ENDPOINTS.md` se generaron leyendo los controladores.

**No se pudo** en ese entorno compilar con Maven ni arrancar Spring Boot (no había acceso a Maven Central), así que el primer
`docker compose up --build` es la primera compilación completa con las dependencias reales. Si apareciera algún error de compilación o de arranque,
revisa `docker compose logs backend`: el mensaje indica el archivo y la línea exactos.
