-- Kardex, mermas, alertas y controles de inventario de los últimos 60 días.
-- El stock de cada insumo (tabla insumo) es el valor "actual"; el kardex se reconstruye hacia atrás para que cuadre exactamente.

CREATE TEMP TABLE t_prov (id_insumo int, id_proveedor int);
INSERT INTO t_prov VALUES (1,1),(2,1),(3,1),(4,1),(5,1),(6,1),(7,2),(8,2),(9,3),(10,3),(11,3),(12,3),(13,3),(14,3),(15,4),(16,4),(17,4),(18,5),(19,5),(20,5),(21,4),(22,4),(23,4),(24,5),(25,4),(26,5),(27,5),(28,5),(29,5),(30,5),(31,5),(32,5),(33,5),(34,5),(35,5),(36,6),(37,6),(38,6),(39,6),(40,6),(41,6),(42,6),(43,6),(44,6),(45,6);

-- 1) Consumo diario por receta (días anteriores acumulados; hoy, pedido por pedido)
CREATE TEMP TABLE t_cons AS
SELECT p.fecha_turno AS dia, r.id_insumo, SUM(r.cantidad * d.cantidad) AS qty
FROM pedido p JOIN detalle_pedido d ON d.id_pedido = p.id_pedido JOIN receta_insumo r ON r.id_producto = d.id_producto
WHERE p.estado <> 'CANCELADO' AND p.fecha_turno < CURRENT_DATE
GROUP BY 1, 2;

-- insumos sin receta pero de uso diario
INSERT INTO t_cons
SELECT d::date, i.id_insumo, ROUND((CASE i.codigo WHEN 'INS-SERV' THEN 1.5 WHEN 'INS-BDEL' THEN 9 ELSE 0.9 END * (0.7 + random()*0.6))::numeric, 3)
FROM generate_series(CURRENT_DATE - 60, CURRENT_DATE - 1, interval '1 day') d, insumo i WHERE i.codigo IN ('INS-SERV','INS-BDEL');

INSERT INTO movimiento_inventario (id_insumo, tipo, variacion, stock_resultante, fecha_hora, motivo)
SELECT id_insumo, 'CONSUMO', -ROUND(qty, 3), 0, dia + time '23:00', 'Consumo diario acumulado (pedidos del día)' FROM t_cons;

INSERT INTO movimiento_inventario (id_insumo, tipo, variacion, stock_resultante, fecha_hora, id_pedido, motivo)
SELECT r.id_insumo, 'CONSUMO', -ROUND(SUM(r.cantidad * d.cantidad), 3), 0, p.creado_en, p.id_pedido, 'Consumo del pedido #' || p.numero_turno
FROM pedido p JOIN detalle_pedido d ON d.id_pedido = p.id_pedido JOIN receta_insumo r ON r.id_producto = d.id_producto
WHERE p.estado <> 'CANCELADO' AND p.fecha_turno = CURRENT_DATE
GROUP BY r.id_insumo, p.id_pedido, p.numero_turno, p.creado_en;

UPDATE insumo i SET consumo_promedio_diario = COALESCE((SELECT ROUND(SUM(qty) / 14, 3) FROM t_cons c WHERE c.id_insumo = i.id_insumo AND c.dia >= CURRENT_DATE - 14), 0);

-- 2) Entradas de mercadería (lunes y jueves) cubriendo ~90 % del consumo acumulado
DO $$
DECLARE i record; d date; acc numeric; ent numeric; paso numeric; diaria boolean;
BEGIN
  PERFORM setseed(0.31);
  FOR i IN SELECT ins.id_insumo, ins.costo_unitario, ins.unidad_medida, ins.stock_actual, ins.consumo_promedio_diario, pr.id_proveedor
           FROM insumo ins JOIN t_prov pr USING (id_insumo)
           WHERE EXISTS (SELECT 1 FROM t_cons c WHERE c.id_insumo = ins.id_insumo) LOOP
    acc := 0;
    paso := CASE WHEN i.unidad_medida IN ('unid','paq') THEN 10 ELSE 2 END;
    -- los insumos con poca cobertura (menos de 6 días) se reponen a diario; el resto lunes y jueves
    diaria := i.consumo_promedio_diario > 0 AND (i.stock_actual / i.consumo_promedio_diario) < 6;
    FOR d IN SELECT generate_series(CURRENT_DATE - 60, CURRENT_DATE - 1, interval '1 day')::date LOOP
      acc := acc + COALESCE((SELECT SUM(qty) FROM t_cons WHERE id_insumo = i.id_insumo AND dia = d), 0);
      IF (extract(dow FROM d) IN (1, 4) OR (diaria AND extract(dow FROM d) <> 0)) THEN
        ent := FLOOR((acc * 0.95) / paso) * paso;
        IF ent >= paso THEN
          INSERT INTO movimiento_inventario (id_insumo, tipo, variacion, stock_resultante, fecha_hora, id_usuario, id_proveedor, costo_unitario, documento, motivo)
          VALUES (i.id_insumo, 'ENTRADA', ent, 0, d + time '08:30' + random() * interval '60 minutes', CASE WHEN random() < .5 THEN 5 ELSE 6 END, i.id_proveedor,
                  ROUND((i.costo_unitario * (0.95 + random()*0.1))::numeric, 2), 'FAC-' || lpad(floor(random()*99999)::int::text, 5, '0'), 'Reposición programada');
          acc := acc - ent;
        END IF;
      END IF;
    END LOOP;
  END LOOP;
END $$;

-- 3) Mermas
DO $$
DECLARE n int; ins record; q numeric; mot text; f timestamp; usr bigint; pick int;
BEGIN
  PERFORM setseed(0.55);
  FOR n IN 1..85 LOOP
    pick := (ARRAY[1,1,2,3,4,6,19,26,26,28,30,32,32,33,33,15,11,5,20,22])[1 + floor(random()*20)::int];
    SELECT * INTO ins FROM insumo WHERE id_insumo = pick;
    q := CASE WHEN ins.unidad_medida IN ('unid','paq') THEN 2 + floor(random()*11) ELSE ROUND((0.4 + random()*3.6)::numeric, 1) END;
    mot := (ARRAY['VENCIMIENTO','DETERIORO','DETERIORO','CAIDA','ERROR_PREPARACION','OTRO'])[1 + floor(random()*6)::int];
    f := CASE WHEN n <= 3 THEN now() - (n * interval '2 hours') ELSE (CURRENT_DATE - floor(random()*45)::int) + time '11:00' + random() * interval '10 hours' END;
    IF f > now() THEN f := now() - interval '30 minutes'; END IF;
    usr := CASE WHEN random() < .5 THEN 5 ELSE 6 END;
    INSERT INTO merma (id_insumo, cantidad, motivo, observacion, costo_total, fecha_hora, id_usuario)
    VALUES (ins.id_insumo, q, mot, CASE mot WHEN 'VENCIMIENTO' THEN 'Producto fuera de fecha' WHEN 'DETERIORO' THEN 'Mal estado al revisar' WHEN 'CAIDA' THEN 'Se cayó durante la preparación'
            WHEN 'ERROR_PREPARACION' THEN 'Preparación descartada' ELSE 'Otro motivo' END, ROUND(q * ins.costo_unitario, 2), f, usr);
    INSERT INTO movimiento_inventario (id_insumo, tipo, variacion, stock_resultante, fecha_hora, id_usuario, motivo)
    VALUES (ins.id_insumo, 'MERMA', -q, 0, f, usr, 'Merma por ' || lower(mot));
  END LOOP;
END $$;

-- 4) Controles físicos: semanal (sábados, alimentos) y quincenal (días 15 y 30, utensilios/mesas/vasos)
DO $$
DECLARE c record; ctl bigint; ins record; sis numeric; cont numeric; dif numeric; cerrado boolean; k int := 0;
BEGIN
  PERFORM setseed(0.88);
  FOR c IN SELECT d::date AS fecha, CASE WHEN extract(dow FROM d) = 6 THEN 'SEMANAL' ELSE 'QUINCENAL' END AS tipo
           FROM generate_series(CURRENT_DATE - 60, CURRENT_DATE, interval '1 day') d
           WHERE extract(dow FROM d) = 6 OR extract(day FROM d) IN (15, 30)
           UNION SELECT d::date, 'QUINCENAL' FROM generate_series(CURRENT_DATE - 60, CURRENT_DATE, interval '1 day') d
           WHERE extract(dow FROM d) = 6 AND extract(day FROM d) IN (15, 30)
           ORDER BY 1 LOOP
    cerrado := c.fecha < CURRENT_DATE;
    INSERT INTO control_inventario (tipo, fecha, estado, id_usuario, observacion, creado_en, cerrado_en)
    VALUES (c.tipo, c.fecha, CASE WHEN cerrado THEN 'CERRADO' ELSE 'ABIERTO' END, 5,
            CASE WHEN cerrado THEN 'Conteo físico completado' END, c.fecha + time '08:00', CASE WHEN cerrado THEN c.fecha + time '10:40' END)
    RETURNING id_control INTO ctl;
    k := 0;
    FOR ins IN SELECT i.* FROM insumo i JOIN categoria_insumo ci USING (id_categoria_insumo)
               WHERE ci.tipo = CASE WHEN c.tipo = 'SEMANAL' THEN 'ALIMENTO' ELSE 'UTENSILIO' END ORDER BY i.id_insumo LOOP
      k := k + 1;
      sis := CASE WHEN ins.unidad_medida IN ('unid','paq') THEN ROUND(ins.stock_actual * (0.9 + random()*0.2)) ELSE ROUND((ins.stock_actual * (0.9 + random()*0.2))::numeric, 1) END;
      IF cerrado OR k <= 12 THEN
        dif := CASE WHEN random() < .75 THEN 0 WHEN ins.unidad_medida IN ('unid','paq') THEN (floor(random()*7) - 3) ELSE ROUND(((random()*2) - 1)::numeric, 1) END;
        cont := GREATEST(0, sis + dif); dif := cont - sis;
      ELSE cont := NULL; dif := NULL; END IF;
      INSERT INTO control_inventario_detalle (id_control, id_insumo, stock_sistema, stock_contado, diferencia, ajustado)
      VALUES (ctl, ins.id_insumo, sis, cont, dif, cerrado AND dif IS NOT NULL AND dif <> 0);
      IF cerrado AND dif IS NOT NULL AND dif <> 0 THEN
        INSERT INTO movimiento_inventario (id_insumo, tipo, variacion, stock_resultante, fecha_hora, id_usuario, motivo)
        VALUES (ins.id_insumo, 'AJUSTE', dif, 0, c.fecha + time '10:40', 5, 'Ajuste por control ' || lower(c.tipo) || ' #' || ctl);
      END IF;
    END LOOP;
  END LOOP;
END $$;

-- 5) Cuadre del kardex: apertura = stock actual - suma del resto de movimientos; luego saldo acumulado por insumo
INSERT INTO movimiento_inventario (id_insumo, tipo, variacion, stock_resultante, fecha_hora, id_usuario, motivo)
SELECT i.id_insumo, 'AJUSTE', i.stock_actual - COALESCE((SELECT SUM(variacion) FROM movimiento_inventario m WHERE m.id_insumo = i.id_insumo), 0), 0,
       (CURRENT_DATE - 61) + time '07:00', 5, 'Inventario inicial del periodo'
FROM insumo i;

UPDATE movimiento_inventario m SET stock_resultante = s.saldo
FROM (SELECT id_movimiento, SUM(variacion) OVER (PARTITION BY id_insumo ORDER BY fecha_hora, id_movimiento) AS saldo FROM movimiento_inventario) s
WHERE m.id_movimiento = s.id_movimiento;

-- 6) Alertas de stock: abiertas para lo que hoy está bajo el umbral e historial atendido
INSERT INTO alerta_stock (id_insumo, stock_actual, umbral_minimo, estado, creada_en)
SELECT id_insumo, stock_actual, umbral_minimo, 'ABIERTA', now() - (random() * interval '20 hours') FROM insumo WHERE stock_actual <= umbral_minimo AND activo;
INSERT INTO alerta_stock (id_insumo, stock_actual, umbral_minimo, estado, creada_en, atendida_en)
SELECT i.id_insumo, ROUND(i.umbral_minimo * 0.8, 3), i.umbral_minimo, 'ATENDIDA', t, t + interval '1 day'
FROM (SELECT id_insumo, umbral_minimo, now() - (g * interval '2 days') - random() * interval '8 hours' AS t, row_number() OVER () rn FROM insumo, generate_series(1, 12) g WHERE codigo IN ('INS-POLLO','INS-ACEI','INS-GAS2','INS-PAPA') ) i
WHERE i.rn % 3 = 0;

SELECT setval(pg_get_serial_sequence('movimiento_inventario','id_movimiento'), (SELECT MAX(id_movimiento) FROM movimiento_inventario));
DROP TABLE t_cons; DROP TABLE t_prov;
