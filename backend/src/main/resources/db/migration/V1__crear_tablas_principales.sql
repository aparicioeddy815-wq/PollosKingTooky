CREATE TABLE producto (
                          id_producto SERIAL PRIMARY KEY,
                          nombre VARCHAR(255) NOT NULL,
                          descripcion TEXT,
                          precio DECIMAL(10, 2) NOT NULL,
                          disponible BOOLEAN NOT NULL
);

CREATE TABLE insumo (
                        id_insumo SERIAL PRIMARY KEY,
                        nombre VARCHAR(255) NOT NULL,
                        unidad_medida VARCHAR(50) NOT NULL,
                        stock_actual DECIMAL(10, 2) NOT NULL,
                        stock_minimo DECIMAL(10, 2) NOT NULL,
                        estado VARCHAR(50) NOT NULL
);

CREATE TABLE guarnicion (
                            id_guarnicion SERIAL PRIMARY KEY,
                            nombre VARCHAR(255) NOT NULL,
                            stock_disponible DECIMAL(10, 2) NOT NULL,
                            stock_minimo DECIMAL(10, 2) NOT NULL,
                            estado VARCHAR(50) NOT NULL
);

CREATE TABLE usuario (
                         id_usuario SERIAL PRIMARY KEY,
                         id_rol INT NOT NULL,
                         nombre_usuario VARCHAR(255) NOT NULL,
                         contrasena VARCHAR(255) NOT NULL,
                         estado VARCHAR(50) NOT NULL
);

CREATE TABLE pedido (
                        id_pedido SERIAL PRIMARY KEY,
                        id_cliente INT,
                        id_turno INT,
                        fecha_hora TIMESTAMP NOT NULL,
                        estado VARCHAR(50) NOT NULL,
                        tipo_pedido VARCHAR(100) NOT NULL,
                        total DECIMAL(10, 2) NOT NULL
);