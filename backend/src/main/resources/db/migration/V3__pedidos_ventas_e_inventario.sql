CREATE TABLE promocion (
                           id_promocion SERIAL PRIMARY KEY,
                           nombre VARCHAR(255) NOT NULL,
                           porcentaje_descuento DECIMAL(5, 2) NOT NULL CHECK (porcentaje_descuento > 0 AND porcentaje_descuento <= 100),
                           fecha_inicio DATE,
                           fecha_fin DATE,
                           estado BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE detalle_pedido (
                                id_detalle SERIAL PRIMARY KEY,
                                id_pedido INT NOT NULL REFERENCES pedido (id_pedido) ON DELETE CASCADE,
                                id_producto INT NOT NULL REFERENCES producto (id_producto),
                                cantidad INT NOT NULL CHECK (cantidad > 0),
                                precio_unitario DECIMAL(10, 2) NOT NULL CHECK (precio_unitario >= 0),
                                subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0),
                                observaciones TEXT
);

CREATE TABLE detalle_pedido_guarnicion (
                                           id_detalle INT NOT NULL REFERENCES detalle_pedido (id_detalle) ON DELETE CASCADE,
                                           id_guarnicion INT NOT NULL REFERENCES guarnicion (id_guarnicion),
                                           cantidad INT NOT NULL DEFAULT 1 CHECK (cantidad > 0),
                                           PRIMARY KEY (id_detalle, id_guarnicion)
);

CREATE TABLE pago (
                      id_pago SERIAL PRIMARY KEY,
                      id_pedido INT NOT NULL UNIQUE REFERENCES pedido (id_pedido),
                      id_promocion INT REFERENCES promocion (id_promocion) ON DELETE SET NULL,
                      monto_total DECIMAL(10, 2) NOT NULL CHECK (monto_total >= 0),
                      descuento DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (descuento >= 0),
                      metodo_pago VARCHAR(50) NOT NULL,
                      fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cierre_caja (
                             id_cierre SERIAL PRIMARY KEY,
                             id_usuario INT NOT NULL REFERENCES usuario (id_usuario),
                             fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             monto_inicial DECIMAL(10, 2) NOT NULL CHECK (monto_inicial >= 0),
                             monto_final DECIMAL(10, 2) NOT NULL CHECK (monto_final >= 0),
                             diferencia DECIMAL(10, 2) NOT NULL DEFAULT 0
);

ALTER TABLE pedido
    ALTER COLUMN estado SET DEFAULT 'PENDIENTE',
    ADD CONSTRAINT chk_pedido_estado
        CHECK (estado IN ('PENDIENTE', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO')) NOT VALID;

ALTER TABLE insumo
    ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'INSUMO'
        CHECK (tipo IN ('INSUMO', 'MENAJE', 'UTENSILIO'));

CREATE TABLE proveedor (
                           id_proveedor SERIAL PRIMARY KEY,
                           nombre VARCHAR(255) NOT NULL,
                           telefono VARCHAR(30),
                           correo VARCHAR(255),
                           direccion VARCHAR(255),
                           estado VARCHAR(50) NOT NULL DEFAULT 'activo'
);

CREATE TABLE producto_insumo (
                                 id_producto INT NOT NULL REFERENCES producto (id_producto) ON DELETE CASCADE,
                                 id_insumo INT NOT NULL REFERENCES insumo (id_insumo),
                                 cantidad_requerida DECIMAL(10, 3) NOT NULL CHECK (cantidad_requerida > 0),
                                 PRIMARY KEY (id_producto, id_insumo)
);

CREATE TABLE movimiento_inventario (
                                       id_movimiento SERIAL PRIMARY KEY,
                                       id_insumo INT NOT NULL REFERENCES insumo (id_insumo),
                                       id_usuario INT REFERENCES usuario (id_usuario) ON DELETE SET NULL,
                                       id_proveedor INT REFERENCES proveedor (id_proveedor) ON DELETE SET NULL,
                                       id_pedido INT REFERENCES pedido (id_pedido) ON DELETE SET NULL,
                                       tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA', 'SALIDA', 'MERMA', 'AJUSTE')),
                                       cantidad DECIMAL(10, 2) NOT NULL CHECK (cantidad > 0),
                                       stock_resultante DECIMAL(10, 2),
                                       motivo VARCHAR(255),
                                       fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE merma (
                       id_merma SERIAL PRIMARY KEY,
                       id_insumo INT NOT NULL REFERENCES insumo (id_insumo),
                       id_usuario INT REFERENCES usuario (id_usuario) ON DELETE SET NULL,
                       cantidad DECIMAL(10, 2) NOT NULL CHECK (cantidad > 0),
                       motivo VARCHAR(255) NOT NULL,
                       fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE control_inventario (
                                    id_control SERIAL PRIMARY KEY,
                                    id_usuario INT NOT NULL REFERENCES usuario (id_usuario),
                                    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('SEMANAL', 'QUINCENAL')),
                                    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
                                    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTO' CHECK (estado IN ('ABIERTO', 'CERRADO')),
                                    observaciones TEXT
);

CREATE TABLE detalle_control_inventario (
                                            id_detalle_control SERIAL PRIMARY KEY,
                                            id_control INT NOT NULL REFERENCES control_inventario (id_control) ON DELETE CASCADE,
                                            id_insumo INT NOT NULL REFERENCES insumo (id_insumo),
                                            stock_sistema DECIMAL(10, 2) NOT NULL,
                                            stock_fisico DECIMAL(10, 2) NOT NULL CHECK (stock_fisico >= 0),
                                            diferencia DECIMAL(10, 2) NOT NULL,
                                            UNIQUE (id_control, id_insumo)
);

CREATE TABLE alerta_stock (
                              id_alerta SERIAL PRIMARY KEY,
                              id_insumo INT REFERENCES insumo (id_insumo) ON DELETE CASCADE,
                              id_guarnicion INT REFERENCES guarnicion (id_guarnicion) ON DELETE CASCADE,
                              stock_al_generar DECIMAL(10, 2) NOT NULL,
                              stock_minimo DECIMAL(10, 2) NOT NULL,
                              fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA', 'ATENDIDA')),
                              CHECK (id_insumo IS NOT NULL OR id_guarnicion IS NOT NULL)
);

CREATE TABLE permiso (
                         id_permiso SERIAL PRIMARY KEY,
                         codigo VARCHAR(100) NOT NULL UNIQUE,
                         descripcion VARCHAR(255)
);

CREATE TABLE rol_permiso (
                             id_rol INT NOT NULL REFERENCES rol (id_rol) ON DELETE CASCADE,
                             id_permiso INT NOT NULL REFERENCES permiso (id_permiso) ON DELETE CASCADE,
                             PRIMARY KEY (id_rol, id_permiso)
);

CREATE TABLE auditoria (
                           id_auditoria BIGSERIAL PRIMARY KEY,
                           id_usuario INT REFERENCES usuario (id_usuario) ON DELETE SET NULL,
                           accion VARCHAR(100) NOT NULL,
                           entidad VARCHAR(100),
                           id_entidad VARCHAR(50),
                           detalle TEXT,
                           fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pedido_estado ON pedido (estado);
CREATE INDEX idx_detalle_pedido_pedido ON detalle_pedido (id_pedido);
CREATE INDEX idx_movimiento_insumo ON movimiento_inventario (id_insumo, fecha_hora);
CREATE INDEX idx_alerta_estado ON alerta_stock (estado);
CREATE INDEX idx_auditoria_fecha ON auditoria (fecha_hora);