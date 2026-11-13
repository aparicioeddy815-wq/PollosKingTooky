# Endpoints de la API

Generado a partir de los controladores. La documentación interactiva está en **http://localhost:8080/swagger-ui.html**. Salvo los marcados como "público", todos requieren `Authorization: Bearer <accessToken>`.

## Archivos

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/archivos/fotos/{nombre:.+}` | público | Devuelve una foto de perfil |

## Autenticación

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/login` | público | Inicia sesión con usuario o correo y contraseña (opción «Recordarme» extiende la sesión a 30 días) |
| `POST` | `/api/auth/register` | público | Registra un cliente nuevo y abre su sesión |
| `POST` | `/api/auth/google` | público | Inicia sesión con una cuenta de Google (envía el ID token que entrega Google Identity Services) |
| `POST` | `/api/auth/refresh` | público | Cambia un refresh token por un nuevo par de tokens |
| `POST` | `/api/auth/logout` | público | Cierra la sesión revocando el refresh token |
| `POST` | `/api/auth/olvide-contrasena` | público | Envía por correo un enlace para restablecer la contraseña |
| `POST` | `/api/auth/restablecer-contrasena` | público | Define una nueva contraseña con el token recibido por correo |
| `GET` | `/api/auth/me` | sesión iniciada | Datos del usuario autenticado |

## Bitácora de auditoría

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/bitacora` | `BITACORA_LEER` | Eventos con filtros: categoria (TODOS, SEGURIDAD, ANULACIONES, PERMISOS), actor (TODOS, GERENTE, ADMIN, DESPACHO, CLIENTE), fechas y texto |
| `GET` | `/api/bitacora/resumen` | `BITACORA_LEER` | Tarjetas: eventos de hoy, accesos y sesiones, suspensiones y ajustes de stock |
| `GET` | `/api/bitacora/exportar` | `BITACORA_EXPORTAR` | Exporta los eventos filtrados a csv o pdf (hasta 5.000 registros) |

## Caja

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/caja/cierres` | `CAJA_CIERRE` | Cierres de caja de un día (hoy por defecto) |
| `POST` | `/api/caja/cierres` | `CAJA_CIERRE` | Registra un cierre de caja con las ventas acumuladas del día |

## Portal del cliente

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/cliente/menu-salon` | `CLIENTE_PORTAL` | Menú del salón: lo de PedidosYA más platos exclusivos, con corazón y veces consumidas |
| `GET` | `/api/cliente/favoritos` | `CLIENTE_PORTAL` |  |
| `POST` | `/api/cliente/favoritos/{idProducto}/me-gusta` | `CLIENTE_PORTAL` | Alterna el corazón de un producto |
| `POST` | `/api/cliente/quejas` | `CLIENTE_PORTAL` | Envía una sugerencia, queja o felicitación (con número de pedido y valoración opcionales) |
| `GET` | `/api/cliente/quejas` | `CLIENTE_PORTAL` |  |
| `POST` | `/api/cliente/evaluacion` | `CLIENTE_PORTAL` | Califica el día de 1 a 5 estrellas (una vez por día; se puede corregir) |
| `GET` | `/api/cliente/evaluacion/hoy` | `CLIENTE_PORTAL` | Mi evaluación de hoy, o vacío si todavía no califiqué |
| `GET` | `/api/cliente/ayuda` | `CLIENTE_PORTAL` | Pasos para reservar y contacto de WhatsApp corporativo |

## Dashboards

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/dashboard/gerente` | `PANEL_GERENTE` | Dashboard del gerente: ventas, pedidos, stock crítico, ventas por categoría, quejas y entrevistas |
| `GET` | `/api/dashboard/admin` | `PANEL_ADMIN` | Dashboard del administrador: ventas totales, margen, ritmo por hora, top del menú, operaciones y bitácora reciente |
| `GET` | `/api/dashboard/admin/exportar` | `PANEL_ADMIN` | Descarga el resumen diario en PDF |

## Entrevistas y selección

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/entrevistas/agenda` | `ENTREVISTA_GESTIONAR` | Agenda: entrevistas de hoy con cuenta regresiva, próximas citas y tarjetas de resumen |
| `GET` | `/api/entrevistas/resumen` | `ENTREVISTA_GESTIONAR` |  |
| `GET` | `/api/entrevistas/vacantes` | `ENTREVISTA_GESTIONAR` o `ENTREVISTA_DECIDIR` |  |
| `POST` | `/api/entrevistas` | `ENTREVISTA_GESTIONAR` | Agenda una entrevista (presencial, virtual o telefónica) |
| `PUT` | `/api/entrevistas/{id}/observaciones` | `ENTREVISTA_GESTIONAR` | Guarda observaciones; «realizada» cierra la entrevista y «aprobar» da el visto bueno gerencial |
| `GET` | `/api/postulantes` | `ENTREVISTA_GESTIONAR` o `ENTREVISTA_DECIDIR` |  |
| `GET` | `/api/postulantes/{id}` | `ENTREVISTA_GESTIONAR` o `ENTREVISTA_DECIDIR` |  |
| `POST` | `/api/postulantes` | `ENTREVISTA_GESTIONAR` |  |
| `POST` | `/api/postulantes/{id}/documentos` | `ENTREVISTA_GESTIONAR` | Sube un documento del postulante: CV, CARNET o ANTECEDENTES (PDF/PNG/JPG, máximo 5 MB; campos «tipo» y «archivo») |
| `GET` | `/api/postulantes/documentos/{idDocumento}/descargar` | `ENTREVISTA_GESTIONAR` o `ENTREVISTA_DECIDIR` |  |
| `GET` | `/api/postulantes/{id}/cv` | `ENTREVISTA_GESTIONAR` o `ENTREVISTA_DECIDIR` | Descarga el CV del postulante |
| `GET` | `/api/entrevistados/resumen` | `ENTREVISTA_DECIDIR` | Tarjetas del administrador: postulantes del ciclo, visto bueno gerencial y urgencia operativa |
| `POST` | `/api/entrevistados/{id}/contratar` | `ENTREVISTA_DECIDIR` | Aprobación definitiva: crea al empleado. Exige visto bueno del gerente y documentos completos |
| `POST` | `/api/entrevistados/{id}/descartar` | `ENTREVISTA_DECIDIR` |  |
| `GET` | `/api/entrevistados/exportar` | `ENTREVISTA_DECIDIR` | Informe de selección de personal en csv, xlsx o pdf |

## Tiempo real

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/stream/comandas` | `PEDIDO_LEER` | SSE de comandas: pedido-creado, pedido-listo, pedido-actualizado y timbre. EventSource no permite cabeceras, así que el token va en ?access_token= |

## Inventario

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/inventario` | `INVENTARIO_LEER` | Insumos con estado (CRITICO/BAJO/NORMAL), consumo de hoy y proyección de consumo vs. existencias |
| `GET` | `/api/inventario/resumen` | `INVENTARIO_LEER` | Tarjetas: alertas urgentes, abastecimiento, valoración del inventario y artículos a reponer |
| `GET` | `/api/inventario/categorias` | `INVENTARIO_LEER` |  |
| `GET` | `/api/inventario/proveedores` | `INVENTARIO_LEER` |  |
| `GET` | `/api/inventario/alertas` | `INVENTARIO_LEER` | Alertas de stock abiertas |
| `GET` | `/api/inventario/movimientos` | `INVENTARIO_LEER` | Kardex: entradas, consumos, mermas y ajustes (de un insumo o de todos) |
| `POST` | `/api/inventario/entradas` | `INVENTARIO_ESCRIBIR` | Registra una entrada de mercadería |
| `GET` | `/api/inventario/mermas` | `INVENTARIO_LEER` | Historial de mermas (últimos 30 días por defecto) |
| `POST` | `/api/inventario/mermas` | `INVENTARIO_ESCRIBIR` | Registra una merma (descuenta stock, la valora y la deja en el kardex) |
| `GET` | `/api/inventario/exportar` | `INVENTARIO_LEER` | Descarga el inventario en csv, xlsx o pdf |
| `GET` | `/api/inventario/mermas/exportar` | `INVENTARIO_LEER` |  |
| `GET` | `/api/inventario/controles` | `INVENTARIO_LEER` |  |
| `GET` | `/api/inventario/controles/programacion` | `INVENTARIO_LEER` | Próximo control semanal (sábado) y quincenal (días 15 y 30) |
| `GET` | `/api/inventario/controles/{id}` | `INVENTARIO_LEER` |  |
| `POST` | `/api/inventario/controles` | `INVENTARIO_ESCRIBIR` | Abre un control semanal (alimentos) o quincenal (utensilios, mesas y vasos) |
| `PUT` | `/api/inventario/controles/{id}/conteo` | `INVENTARIO_ESCRIBIR` | Guarda lo contado físicamente; calcula la diferencia contra el sistema |
| `POST` | `/api/inventario/controles/{id}/cerrar` | `INVENTARIO_ESCRIBIR` | Cierra el control y aplica las diferencias como ajustes de inventario |

## Despacho y comandas

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/pedidos` | `PEDIDO_LEER` | Pedidos de una fecha (hoy por defecto), opcionalmente filtrados por estado |
| `GET` | `/api/pedidos/comandas` | `PEDIDO_LEER` | Tablero de despacho: comandas en preparación y listas, con tiempo promedio de despacho |
| `GET` | `/api/pedidos/{id}` | `PEDIDO_LEER` |  |
| `POST` | `/api/pedidos` | `PEDIDO_CREAR` | Registra un pedido; asigna el siguiente número de turno del día y descuenta insumos |
| `PATCH` | `/api/pedidos/{id}/estado` | `PEDIDO_ESTADO` | Cambia el estado respetando el ciclo EN_PREPARACION → LISTO → ENTREGADO |
| `POST` | `/api/pedidos/{id}/listo` | `PEDIDO_ESTADO` | Marca el pedido como listo (dispara el aviso sonoro en las pantallas conectadas) |
| `POST` | `/api/pedidos/{id}/entregar` | `PEDIDO_ESTADO` |  |
| `POST` | `/api/pedidos/{id}/cancelar` | `PEDIDO_ESTADO` | Anula una comanda en preparación (queda registrada en la bitácora) |
| `POST` | `/api/pedidos/{id}/timbre` | `PEDIDO_ESTADO` | Toca el timbre para llamar al cliente de un pedido listo |

## Perfil

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/perfil` | sesión iniciada | Mi perfil, con fecha de ingreso, último acceso y frecuencia de visitas |
| `PUT` | `/api/perfil` | sesión iniciada | Actualiza mi nombre, correo y foto (URL) |
| `POST` | `/api/perfil/contrasena` | sesión iniciada | Cambia mi contraseña |
| `POST` | `/api/perfil/foto` | sesión iniciada | Sube mi foto de perfil (PNG, JPG o WEBP, máximo 5 MB; campo «foto») |

## Roles y personal

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/personal/resumen` | `PERSONAL_LEER` | Tarjetas: total de empleados, perfiles creados, turno actual y ocupación de puestos |
| `GET` | `/api/personal` | `PERSONAL_LEER` | Personal registrado; filtros por texto, rol (ROL-ADMIN, ROL-GERENTE, ROL-DESPACHO) y turno (MANANA, TARDE) |
| `GET` | `/api/personal/estaciones` | `PERSONAL_LEER` | Estaciones operativas: puestos totales, ocupados (%) y quién ocupa cada puesto |
| `POST` | `/api/personal/perfiles` | `PERSONAL_GESTIONAR` | Crea el perfil de acceso de un colaborador; si no se envía contraseña se genera una temporal |
| `POST` | `/api/personal/empleados` | `PERSONAL_GESTIONAR` | Asigna (registra) un empleado nuevo |
| `PUT` | `/api/personal/{id}` | `PERSONAL_GESTIONAR` |  |
| `PUT` | `/api/personal/{id}/rol` | `PERSONAL_GESTIONAR` | Editar rol: los cambios de nivel 1 y 2 quedan como eventos críticos en la bitácora |
| `PATCH` | `/api/personal/{id}/suspender` | `PERSONAL_GESTIONAR` |  |
| `PATCH` | `/api/personal/{id}/reactivar` | `PERSONAL_GESTIONAR` |  |
| `GET` | `/api/personal/exportar` | `PERSONAL_LEER` | Nómina del personal en csv, xlsx o pdf |
| `GET` | `/api/roles` | `ROL_LEER` | Roles con su nivel de seguridad, usuarios activos y permisos |
| `GET` | `/api/roles/permisos` | `ROL_LEER` |  |
| `GET` | `/api/roles/{id}/usuarios` | `ROL_LEER` | Usuarios que tienen el rol |
| `PUT` | `/api/roles/{id}/permisos` | `ROL_EDITAR` | Reemplaza los permisos de un rol (evento crítico en la bitácora) |
| `GET` | `/api/usuarios` | `USUARIO_LEER` | Usuarios del sistema; estado 1 = activo, 0 = inactivo |
| `PATCH` | `/api/usuarios/{id}/activar` | `USUARIO_ESCRIBIR` |  |
| `PATCH` | `/api/usuarios/{id}/desactivar` | `USUARIO_ESCRIBIR` |  |

## Menú y productos

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/productos` | `PRODUCTO_LEER` | Lista paginada de productos con filtros por nombre, categoría y disponibilidad |
| `GET` | `/api/productos/categorias` | `PRODUCTO_LEER` |  |
| `GET` | `/api/productos/{id}` | `PRODUCTO_LEER` |  |
| `POST` | `/api/productos` | `PRODUCTO_ESCRIBIR` |  |
| `PUT` | `/api/productos/{id}` | `PRODUCTO_ESCRIBIR` | Edita un producto; si cambia el precio queda como «ajuste de precio» en la bitácora |
| `PATCH` | `/api/productos/{id}/disponibilidad` | `PRODUCTO_ESCRIBIR` | Marca un producto como disponible o agotado |
| `PATCH` | `/api/productos/{id}/publicacion` | `PRODUCTO_ESCRIBIR` | Publica o retira un producto del menú de PedidosYA |
| `DELETE` | `/api/productos/{id}` | `PRODUCTO_ESCRIBIR` |  |

## Sitio público

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/publico/inicio` | público | Portada: producto más vendido, horarios y ubicación |
| `GET` | `/api/publico/menu` | público | Menú de PedidosYA por categorías, indicando qué está disponible hoy |
| `GET` | `/api/publico/horarios` | público |  |
| `GET` | `/api/publico/ubicacion` | público |  |
| `GET` | `/api/publico/sobre-nosotros` | público |  |
| `GET` | `/api/publico/turnos` | público | Números de turno en preparación y listos |
| `GET` | `/api/publico/eventos` | público | Flujo en tiempo real (SSE): turnos-actualizados, timbre y menu-actualizado |

## Quejas y sugerencias (gerente)

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/quejas` | `QUEJA_GESTIONAR` | Mensajes de clientes filtrados por tipo (RECLAMO, SUGERENCIA, FELICITACION) y rango de fechas |
| `GET` | `/api/quejas/resumen` | `QUEJA_GESTIONAR` | Recibidas del mes, tiempo promedio de despacho vs. objetivo y satisfacción en sala |
| `PATCH` | `/api/quejas/{id}/responder` | `QUEJA_GESTIONAR` |  |
| `PATCH` | `/api/quejas/{id}/revision` | `QUEJA_GESTIONAR` | Marca el mensaje como «en revisión» |
| `GET` | `/api/quejas/exportar` | `QUEJA_GESTIONAR` | Descarga el informe de quejas y sugerencias en csv, xlsx o pdf |

## Reportes y respaldos

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/reportes/resumen` | `REPORTE_LEER` | Reporte financiero del mes (YYYY-MM): ingresos, margen, desperdicio, canales, evolución semanal y distribución |
| `GET` | `/api/reportes/exportar/excel` | `REPORTE_EXPORTAR` | Descarga el reporte del mes en Excel |
| `GET` | `/api/reportes/imprimible` | `REPORTE_EXPORTAR` | Descarga el reporte del mes en PDF listo para imprimir |
| `GET` | `/api/reportes/archivos` | `REPORTE_LEER` | Archivo de informes oficiales (incluye los programados) |
| `GET` | `/api/reportes/archivos/resumen` | `REPORTE_LEER` |  |
| `POST` | `/api/reportes/archivos` | `REPORTE_EXPORTAR` | Genera un informe y lo archiva. tipo: VENTAS_MENSUAL, INVENTARIO, AUDITORIA o RESUMEN_DIARIO; formato: PDF o XLSX |
| `GET` | `/api/reportes/archivos/{id}/descargar` | `REPORTE_LEER` |  |
| `GET` | `/api/respaldos` | `RESPALDO_GESTIONAR` | Historial de respaldos (automáticos cada 12 horas y manuales) |
| `POST` | `/api/respaldos` | `RESPALDO_GESTIONAR` | Ejecuta un respaldo ahora mismo |
| `GET` | `/api/respaldos/{id}/descargar` | `RESPALDO_GESTIONAR` |  |

## Reservas

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/api/reservas/dias` | `RESERVA_CREAR` | Paso 1: días de la semana disponibles para reservar |
| `GET` | `/api/reservas/horarios` | `RESERVA_CREAR` | Paso 2: horas del día elegido con su cupo restante |
| `POST` | `/api/reservas` | `RESERVA_CREAR` | Paso 3: confirma la reserva con los platos elegidos (queda pendiente de pago en caja) |
| `GET` | `/api/reservas/mis` | `RESERVA_CREAR` | Mis reservas, con filtro opcional por estado |
| `GET` | `/api/reservas/mis/resumen` | `RESERVA_CREAR` | Resumen para el inicio del cliente: total de reservas, próxima visita y aviso del día |
| `PATCH` | `/api/reservas/{id}/reprogramar` | `RESERVA_CREAR` o `RESERVA_GESTIONAR` | Cambia fecha y hora (solo reservas pendientes de pago, con al menos 60 min de anticipación) |
| `PATCH` | `/api/reservas/{id}/cancelar` | `RESERVA_CREAR` o `RESERVA_GESTIONAR` |  |
| `GET` | `/api/reservas/{id}/recibo` | `RESERVA_CREAR` o `RESERVA_GESTIONAR` | Descarga el recibo de la reserva en PDF |
| `GET` | `/api/reservas/{id}` | `RESERVA_CREAR` o `RESERVA_GESTIONAR` |  |
| `GET` | `/api/reservas` | `RESERVA_GESTIONAR` | Todas las reservas del local, filtradas por estado y fecha |
| `PATCH` | `/api/reservas/{id}/pagar` | `RESERVA_GESTIONAR` | Registra el pago en caja y envía los platos a cocina como comanda |
| `PATCH` | `/api/reservas/{id}/completar` | `RESERVA_GESTIONAR` |  |
