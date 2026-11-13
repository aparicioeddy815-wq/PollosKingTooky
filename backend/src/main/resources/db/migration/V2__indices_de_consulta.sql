-- V2: índices adicionales para las consultas de dashboards y reportes.
-- (V1 es el esquema base que crean los scripts de /init en Docker; Flyway toma el relevo desde aquí.)
CREATE INDEX IF NOT EXISTS idx_detalle_pedido_producto ON detalle_pedido (id_producto);
CREATE INDEX IF NOT EXISTS idx_pedido_canal_fecha ON pedido (canal, fecha_turno);
CREATE INDEX IF NOT EXISTS idx_queja_estado ON queja (estado);
CREATE INDEX IF NOT EXISTS idx_alerta_estado ON alerta_stock (estado);
CREATE INDEX IF NOT EXISTS idx_reserva_estado_limite ON reserva (estado, limite_pago);
