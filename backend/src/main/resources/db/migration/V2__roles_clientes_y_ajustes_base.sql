CREATE TABLE rol (
                     id_rol SERIAL PRIMARY KEY,
                     nombre VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO rol (id_rol, nombre) VALUES
                                     (1, 'ADMINISTRADOR'),
                                     (2, 'GERENTE'),
                                     (3, 'DESPACHO'),
                                     (4, 'CAJERO');

SELECT setval(pg_get_serial_sequence('rol', 'id_rol'), 4);

ALTER TABLE usuario ALTER COLUMN estado DROP DEFAULT;

ALTER TABLE usuario
ALTER COLUMN estado TYPE INTEGER
    USING CASE WHEN estado IN ('activo', '1') THEN 1 ELSE 0 END;

ALTER TABLE usuario
    ALTER COLUMN estado SET DEFAULT 1,
    ADD CONSTRAINT chk_usuario_estado CHECK (estado IN (0, 1)),
    ADD CONSTRAINT uq_usuario_nombre_usuario UNIQUE (nombre_usuario),
    ADD CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol),
    ADD COLUMN nombre_completo VARCHAR(255),
    ADD COLUMN fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE cliente (
                         id_cliente SERIAL PRIMARY KEY,
                         nombre     VARCHAR(255) NOT NULL,
                         nit_ci     VARCHAR(30),
                         telefono   VARCHAR(30),
                         correo     VARCHAR(255)
);

ALTER TABLE pedido DROP CONSTRAINT IF EXISTS fk_pedido_usuario;

UPDATE pedido SET id_cliente = NULL WHERE id_cliente IS NOT NULL;

ALTER TABLE pedido RENAME COLUMN id_turno TO numero_turno;

ALTER TABLE pedido
    ALTER COLUMN fecha_hora SET DEFAULT CURRENT_TIMESTAMP,
ALTER COLUMN estado SET DEFAULT 'pendiente',
    ALTER COLUMN total SET DEFAULT 0.00,
    ADD CONSTRAINT chk_pedido_total CHECK (total >= 0),
    ADD COLUMN id_usuario INT,
    ADD COLUMN fecha_turno DATE NOT NULL DEFAULT CURRENT_DATE,
    ADD CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente) ON DELETE SET NULL,
    ADD CONSTRAINT fk_pedido_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE SET NULL,
    ADD CONSTRAINT uq_pedido_turno_dia UNIQUE (fecha_turno, numero_turno);

ALTER TABLE producto
    ALTER COLUMN disponible SET DEFAULT TRUE,
    ADD CONSTRAINT chk_producto_precio CHECK (precio >= 0);

ALTER TABLE insumo
    ALTER COLUMN stock_actual SET DEFAULT 0,
ALTER COLUMN stock_minimo SET DEFAULT 0,
    ALTER COLUMN estado SET DEFAULT 'activo',
    ADD CONSTRAINT chk_insumo_stock_actual CHECK (stock_actual >= 0),
    ADD CONSTRAINT chk_insumo_stock_minimo CHECK (stock_minimo >= 0);

ALTER TABLE guarnicion
    ALTER COLUMN stock_disponible SET DEFAULT 0,
ALTER COLUMN stock_minimo SET DEFAULT 0,
    ALTER COLUMN estado SET DEFAULT 'activo',
    ADD CONSTRAINT chk_guarnicion_stock_disp CHECK (stock_disponible >= 0),
    ADD CONSTRAINT chk_guarnicion_stock_min CHECK (stock_minimo >= 0);