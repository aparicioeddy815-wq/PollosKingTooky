INSERT INTO permiso (codigo, descripcion) VALUES
                                              ('USUARIO_LEER',        'Consultar usuarios'),
                                              ('USUARIO_ESCRIBIR',    'Crear, modificar, activar/desactivar y eliminar usuarios'),
                                              ('ROL_GESTIONAR',       'Consultar roles y asignar permisos'),
                                              ('AUDITORIA_LEER',      'Consultar la bitacora de auditoria'),
                                              ('INVENTARIO_LEER',     'Consultar insumos, kardex, movimientos y mermas'),
                                              ('INVENTARIO_ESCRIBIR', 'Registrar insumos, entradas y salidas'),
                                              ('MERMA_REGISTRAR',     'Registrar mermas y desperdicios'),
                                              ('CONSUMO_REGISTRAR',   'Registrar consumo de insumos al preparar pedidos'),
                                              ('CONTROL_INVENTARIO',  'Gestionar controles de inventario semanal y quincenal'),
                                              ('ALERTA_LEER',         'Consultar alertas de stock minimo'),
                                              ('PRODUCTO_LEER',       'Consultar productos del menu'),
                                              ('PRODUCTO_ESCRIBIR',   'Crear/modificar productos, recetas y disponibilidad'),
                                              ('PROVEEDOR_GESTIONAR', 'Crear/modificar proveedores')
    ON CONFLICT (codigo) DO NOTHING;

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT 1, id_permiso FROM permiso
    ON CONFLICT DO NOTHING;

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT 2, id_permiso FROM permiso
WHERE codigo IN ('INVENTARIO_LEER','INVENTARIO_ESCRIBIR','MERMA_REGISTRAR','CONSUMO_REGISTRAR','CONTROL_INVENTARIO',
                 'ALERTA_LEER','PRODUCTO_LEER','PRODUCTO_ESCRIBIR','PROVEEDOR_GESTIONAR')
    ON CONFLICT DO NOTHING;

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT 3, id_permiso FROM permiso
WHERE codigo IN ('INVENTARIO_LEER','CONSUMO_REGISTRAR','ALERTA_LEER','PRODUCTO_LEER')
    ON CONFLICT DO NOTHING;

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT 4, id_permiso FROM permiso
WHERE codigo IN ('PRODUCTO_LEER')
    ON CONFLICT DO NOTHING;

INSERT INTO proveedor (nombre, telefono, correo, direccion)
SELECT 'Avicola Sofia', '70000001', 'ventas@avicola.test', 'Zona Sur'
    WHERE NOT EXISTS (SELECT 1 FROM proveedor WHERE nombre = 'Avicola Sofia');

INSERT INTO proveedor (nombre, telefono, correo, direccion)
SELECT 'Distribuidora Andina', '70000002', 'pedidos@andina.test', 'Zona Central'
    WHERE NOT EXISTS (SELECT 1 FROM proveedor WHERE nombre = 'Distribuidora Andina');

INSERT INTO insumo (nombre, unidad_medida, stock_actual, stock_minimo, tipo)
SELECT v.nombre, v.unidad, v.stock, v.minimo, v.tipo
FROM (VALUES
          ('Pollo entero',         'kg',     50.00, 15.00, 'INSUMO'),
          ('Papa',                 'kg',     80.00, 20.00, 'INSUMO'),
          ('Aceite',               'litro',  30.00, 10.00, 'INSUMO'),
          ('Harina para apanado',  'kg',     20.00,  5.00, 'INSUMO'),
          ('Arroz',                'kg',     25.00,  8.00, 'INSUMO'),
          ('Gaseosa 2 litros',     'unidad', 40.00, 12.00, 'INSUMO'),
          ('Bolsa para llevar',    'unidad',200.00, 50.00, 'INSUMO'),
          ('Platos descartables',  'unidad',150.00, 40.00, 'MENAJE'),
          ('Cubiertos',            'unidad',100.00, 30.00, 'MENAJE'),
          ('Pinza de cocina',      'unidad',  6.00,  2.00, 'UTENSILIO')
     ) AS v(nombre, unidad, stock, minimo, tipo)
WHERE NOT EXISTS (SELECT 1 FROM insumo i WHERE i.nombre = v.nombre);

INSERT INTO producto (nombre, descripcion, precio)
SELECT v.nombre, v.descripcion, v.precio
FROM (VALUES
          ('Pollo broaster 1/4',  'Cuarto de pollo broaster con papas y arroz', 25.00),
          ('Pollo broaster 1/2',  'Medio pollo broaster con papas y arroz',     45.00),
          ('Pollo entero',        'Pollo entero broaster con papas',            85.00),
          ('Gaseosa 2 litros',    'Gaseosa familiar',                           14.00)
     ) AS v(nombre, descripcion, precio)
WHERE NOT EXISTS (SELECT 1 FROM producto p WHERE p.nombre = v.nombre);

INSERT INTO producto_insumo (id_producto, id_insumo, cantidad_requerida)
SELECT p.id_producto, i.id_insumo, r.cantidad
FROM (VALUES
          ('Pollo broaster 1/4', 'Pollo entero',        0.350),
          ('Pollo broaster 1/4', 'Papa',                0.250),
          ('Pollo broaster 1/4', 'Aceite',              0.100),
          ('Pollo broaster 1/4', 'Harina para apanado', 0.050),
          ('Pollo broaster 1/4', 'Arroz',               0.100),
          ('Pollo broaster 1/2', 'Pollo entero',        0.700),
          ('Pollo broaster 1/2', 'Papa',                0.400),
          ('Pollo broaster 1/2', 'Aceite',              0.200),
          ('Pollo broaster 1/2', 'Harina para apanado', 0.100),
          ('Pollo broaster 1/2', 'Arroz',               0.150),
          ('Pollo entero',       'Pollo entero',        1.400),
          ('Pollo entero',       'Papa',                0.700),
          ('Pollo entero',       'Aceite',              0.400),
          ('Pollo entero',       'Harina para apanado', 0.200),
          ('Gaseosa 2 litros',   'Gaseosa 2 litros',    1.000)
     ) AS r(producto, insumo, cantidad)
         JOIN producto p ON p.nombre = r.producto
         JOIN insumo   i ON i.nombre = r.insumo
    ON CONFLICT DO NOTHING;