-- Datos base: permisos, roles, configuración del local, categorías y proveedores.

INSERT INTO permiso (codigo, descripcion) VALUES
  ('CLIENTE_PORTAL', 'Acceso al portal del cliente frecuente'),
  ('RESERVA_CREAR', 'Crear y consultar sus propias reservas'),
  ('PEDIDO_LEER', 'Ver comandas y pedidos'),
  ('PEDIDO_CREAR', 'Registrar pedidos nuevos'),
  ('PEDIDO_ESTADO', 'Marcar pedidos como listos, entregados o cancelados y tocar el timbre'),
  ('PRODUCTO_LEER', 'Consultar el menú y los productos'),
  ('PRODUCTO_ESCRIBIR', 'Crear productos, cambiar disponibilidad y precios'),
  ('RESERVA_GESTIONAR', 'Cobrar en caja, completar y cancelar reservas de cualquier cliente'),
  ('INVENTARIO_LEER', 'Consultar inventario, mermas y alertas'),
  ('INVENTARIO_ESCRIBIR', 'Registrar entradas, mermas y controles de inventario'),
  ('QUEJA_GESTIONAR', 'Gestionar sugerencias, quejas y felicitaciones'),
  ('ENTREVISTA_GESTIONAR', 'Agendar entrevistas, escribir observaciones y subir archivos de postulantes'),
  ('ENTREVISTA_DECIDIR', 'Contratar o descartar postulantes de forma definitiva'),
  ('PANEL_GERENTE', 'Ver el dashboard del gerente'),
  ('PANEL_ADMIN', 'Ver el dashboard del administrador'),
  ('USUARIO_LEER', 'Consultar usuarios del sistema'),
  ('USUARIO_ESCRIBIR', 'Activar o desactivar usuarios'),
  ('PERSONAL_LEER', 'Consultar el personal y las estaciones operativas'),
  ('PERSONAL_GESTIONAR', 'Crear perfiles, asignar empleados, editar roles y suspender'),
  ('ROL_LEER', 'Consultar roles y permisos'),
  ('ROL_EDITAR', 'Editar los permisos de un rol'),
  ('BITACORA_LEER', 'Consultar la bitácora de auditoría'),
  ('BITACORA_EXPORTAR', 'Exportar la bitácora a CSV o PDF'),
  ('REPORTE_LEER', 'Consultar reportes financieros y operativos'),
  ('REPORTE_EXPORTAR', 'Exportar reportes a Excel o PDF'),
  ('RESPALDO_GESTIONAR', 'Ver y ejecutar respaldos de la base de datos'),
  ('CAJA_CIERRE', 'Registrar cierres de caja');

INSERT INTO rol (id_rol, codigo, nombre, descripcion, nivel_seguridad) VALUES
  (1, 'ROL-ADMIN', 'ADMINISTRADOR', 'Dueños del local (franquicia). Supervisan ventas, personal, auditoría y reportes.', 'NIVEL_1'),
  (2, 'ROL-GERENTE', 'GERENTE', 'Gestiona inventario, quejas, entrevistas, menú y personal en el local.', 'NIVEL_2'),
  (3, 'ROL-DESPACHO', 'DESPACHO', 'Recibe tickets de caja, alista pedidos y avisa cuando están listos.', 'NIVEL_3'),
  (4, 'ROL-CLIENTE', 'CLIENTE', 'Cliente registrado: reserva, guarda favoritos y deja su opinión.', 'NIVEL_4');

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso FROM rol r JOIN permiso p ON (r.codigo, p.codigo) IN (
  ('ROL-CLIENTE', 'CLIENTE_PORTAL'),
  ('ROL-CLIENTE', 'RESERVA_CREAR'),
  ('ROL-DESPACHO', 'PEDIDO_LEER'),
  ('ROL-DESPACHO', 'PEDIDO_CREAR'),
  ('ROL-DESPACHO', 'PEDIDO_ESTADO'),
  ('ROL-DESPACHO', 'PRODUCTO_LEER'),
  ('ROL-DESPACHO', 'RESERVA_GESTIONAR'),
  ('ROL-DESPACHO', 'CAJA_CIERRE'),
  ('ROL-GERENTE', 'PANEL_GERENTE'),
  ('ROL-GERENTE', 'PEDIDO_LEER'),
  ('ROL-GERENTE', 'PRODUCTO_LEER'),
  ('ROL-GERENTE', 'PRODUCTO_ESCRIBIR'),
  ('ROL-GERENTE', 'INVENTARIO_LEER'),
  ('ROL-GERENTE', 'INVENTARIO_ESCRIBIR'),
  ('ROL-GERENTE', 'QUEJA_GESTIONAR'),
  ('ROL-GERENTE', 'ENTREVISTA_GESTIONAR'),
  ('ROL-GERENTE', 'RESERVA_GESTIONAR'),
  ('ROL-GERENTE', 'USUARIO_LEER'),
  ('ROL-GERENTE', 'PERSONAL_LEER'),
  ('ROL-GERENTE', 'ROL_LEER'),
  ('ROL-GERENTE', 'CAJA_CIERRE'),
  ('ROL-ADMIN', 'PANEL_ADMIN'),
  ('ROL-ADMIN', 'PEDIDO_LEER'),
  ('ROL-ADMIN', 'PRODUCTO_LEER'),
  ('ROL-ADMIN', 'PRODUCTO_ESCRIBIR'),
  ('ROL-ADMIN', 'INVENTARIO_LEER'),
  ('ROL-ADMIN', 'USUARIO_LEER'),
  ('ROL-ADMIN', 'USUARIO_ESCRIBIR'),
  ('ROL-ADMIN', 'PERSONAL_LEER'),
  ('ROL-ADMIN', 'PERSONAL_GESTIONAR'),
  ('ROL-ADMIN', 'ROL_LEER'),
  ('ROL-ADMIN', 'ROL_EDITAR'),
  ('ROL-ADMIN', 'BITACORA_LEER'),
  ('ROL-ADMIN', 'BITACORA_EXPORTAR'),
  ('ROL-ADMIN', 'ENTREVISTA_DECIDIR'),
  ('ROL-ADMIN', 'REPORTE_LEER'),
  ('ROL-ADMIN', 'REPORTE_EXPORTAR'),
  ('ROL-ADMIN', 'RESPALDO_GESTIONAR'),
  ('ROL-ADMIN', 'CAJA_CIERRE'));

INSERT INTO estacion_operativa (id_estacion, nombre, puestos_totales) VALUES
  (1, 'Administración', 6),
  (2, 'Cocina', 6),
  (3, 'Freidoras y parrilla', 4),
  (4, 'Caja', 3),
  (5, 'Despacho', 3),
  (6, 'Atención en salón', 6),
  (7, 'Almacén', 2);

INSERT INTO categoria_producto (id_categoria, codigo, nombre, orden, url_pedidosya) VALUES
  (1, 'COMBOS_POPULARES', 'Combos populares', 1, 'https://www.pedidosya.com.bo/restaurantes/la-paz/king-tokky-menu'),
  (2, 'POLLO_FRITO', 'Pollo frito', 2, 'https://www.pedidosya.com.bo/restaurantes/la-paz/king-tokky-menu'),
  (3, 'PIPOCAS_Y_SALCHIPAPAS', 'Pipocas y salchipapas', 3, 'https://www.pedidosya.com.bo/restaurantes/la-paz/king-tokky-menu'),
  (4, 'GUARNICIONES', 'Guarniciones', 4, 'https://www.pedidosya.com.bo/restaurantes/la-paz/king-tokky-menu'),
  (5, 'BEBIDAS', 'Bebidas', 5, 'https://www.pedidosya.com.bo/restaurantes/la-paz/king-tokky-menu');

INSERT INTO categoria_insumo (id_categoria_insumo, nombre, tipo) VALUES
  (1, 'Carnes', 'ALIMENTO'),
  (2, 'Aceites', 'ALIMENTO'),
  (3, 'Empaques y bolsas', 'ALIMENTO'),
  (4, 'Bebidas y salsas', 'ALIMENTO'),
  (5, 'Guarniciones', 'ALIMENTO'),
  (6, 'Utensilios', 'UTENSILIO'),
  (7, 'Mobiliario', 'UTENSILIO'),
  (8, 'Vajilla y vasos', 'UTENSILIO');

INSERT INTO proveedor (id_proveedor, nombre, telefono, contacto) VALUES
  (1, 'Avícola Los Andes S.R.L.', '2-2455123', 'Teresa Mamani'),
  (2, 'Distribuidora Aceites del Valle', '2-2412099', 'Luis Paredes'),
  (3, 'Empaques y Descartables El Alto', '2-2830155', 'Marco Villca'),
  (4, 'Bebidas Nacionales La Paz', '2-2611877', 'Rosario Quispe'),
  (5, 'Mercado Rodríguez · Verduras Selectas', '7-1234567', 'Doña Eulalia Choque'),
  (6, 'Menaje y Equipamiento Gastronómico Bolivia', '2-2799033', 'Iván Salinas');

INSERT INTO configuracion_local (clave, valor) VALUES
  ('nombre_local', 'King Tokky'),
  ('direccion', 'Av. 6 de Agosto #2345, esquina calle Rosendo Gutiérrez, Sopocachi, La Paz'),
  ('latitud', '-16.5112'),
  ('longitud', '-68.1264'),
  ('whatsapp_corporativo', '+591 70012345'),
  ('correo_contacto', 'contacto@kingtokky.bo'),
  ('historia', 'King Tokky nació como un pequeño local familiar en La Paz, con una idea sencilla: servir pollo frito crocante y hamburguesas jugosas a un precio justo. Lo que empezó con dos freidoras y una receta de condimentos de la casa creció gracias a los vecinos que volvían cada fin de semana.

Hoy King Tokky funciona como franquicia manejada por cuatro socios, con un equipo de cocina, despacho y atención en salón que cuida cada pedido desde que entra la comanda hasta que se grita el número en el mostrador. Mantenemos la misma receta, las mismas porciones generosas y el mismo trato cercano de siempre.

Puedes pedir en línea por PedidosYA, visitarnos en el local o reservar tu mesa con anticipación desde esta página.'),
  ('objetivo_despacho_min', '8'),
  ('aforo_reservas', '40'),
  ('minutos_pago_previo', '10'),
  ('moneda', 'Bs');

INSERT INTO horario_atencion (id_horario, dias, horario, orden) VALUES
  (1, 'Lunes a viernes', '11:00 – 22:00', 1),
  (2, 'Sábado', '11:00 – 23:00', 2),
  (3, 'Domingo y feriados', '11:00 – 21:00', 3);

SELECT setval(pg_get_serial_sequence('permiso','id_permiso'), (SELECT MAX(id_permiso) FROM permiso));
SELECT setval(pg_get_serial_sequence('rol','id_rol'), (SELECT MAX(id_rol) FROM rol));
SELECT setval(pg_get_serial_sequence('estacion_operativa','id_estacion'), (SELECT MAX(id_estacion) FROM estacion_operativa));
SELECT setval(pg_get_serial_sequence('categoria_producto','id_categoria'), (SELECT MAX(id_categoria) FROM categoria_producto));
SELECT setval(pg_get_serial_sequence('categoria_insumo','id_categoria_insumo'), (SELECT MAX(id_categoria_insumo) FROM categoria_insumo));
SELECT setval(pg_get_serial_sequence('proveedor','id_proveedor'), (SELECT MAX(id_proveedor) FROM proveedor));
SELECT setval(pg_get_serial_sequence('horario_atencion','id_horario'), (SELECT MAX(id_horario) FROM horario_atencion));
