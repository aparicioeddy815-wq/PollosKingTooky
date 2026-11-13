-- Reservas y pedidos históricos de los últimos 60 días (fechas relativas a la fecha de creación de la base).
-- Los números de turno reinician cada día; los pedidos de reservas pagadas aparecen como canal RESERVA.

DO $$
DECLARE
  ids int[] := ARRAY[1,1,1,1,1,1,1,2,2,2,3,3,3,3,4,4,4,4,4,4,5,5,5,5,5,6,6,6,6,6,7,7,7,8,9,9,9,9,9,9,9,10,10,10,10,11,11,12,12,12,12,13,13,13,13,14,14,14,15,15,15,15,15,16,16,16,17,17,17,17,17,18,18,18,18,19,19,19,19,20,20,20,20,20,21,21,21,21,22,22,22,22,22,23,23,23,23,24,24,25,25,26,27,27,27,28,28,28,28,28,28,28,28,29,29,29,30,30,30,30,32,32,33,33,34,34];
  horas text[] := ARRAY['12:00','12:30','13:00','13:30','14:00','19:00','19:30','20:00','20:30','21:00'];
  i int; k int; n int; d date; hora time; pid int; cant int; fecha_hora timestamp; creado timestamp; est text;
  cli bigint; nom text; res bigint; tot numeric; precio numeric;
BEGIN
  PERFORM setseed(0.42);
  FOR i IN 1..160 LOOP
    d := CURRENT_DATE + (floor(random()*53)::int - 45);          -- de hace 45 días a dentro de 7 días
    hora := horas[1 + floor(random()*array_length(horas,1))::int]::time;
    fecha_hora := d + hora;
    cli := 9 + floor(random()*28)::int;
    SELECT nombre_completo INTO nom FROM usuario WHERE id_usuario = cli;
    creado := LEAST(now() - interval '2 hours', fecha_hora - interval '1 day' - random()*interval '2 days');
    IF fecha_hora < now() THEN
      est := CASE WHEN random() < .72 THEN 'COMPLETADA' WHEN random() < .45 THEN 'CANCELADA' ELSE 'CANCELADA_AUTO' END;
      IF est = 'COMPLETADA' AND fecha_hora > now() - interval '90 minutes' THEN est := 'PAGADA'; END IF;
    ELSE
      est := CASE WHEN random() < .92 THEN 'PENDIENTE_PAGO' ELSE 'CANCELADA' END;
    END IF;
    INSERT INTO reserva (codigo, id_cliente, nombre_cliente, telefono, correo, fecha_encuentro, hora, personas, notas, total, estado, limite_pago,
                         creado_en, pagada_en, cancelada_en, motivo_cancelacion, id_cobrada_por)
    VALUES ('TMP-' || i, cli, nom, '7' || (4000000 + floor(random()*3999999))::int,
            (SELECT correo FROM usuario WHERE id_usuario = cli), d, hora, 2 + floor(random()*9)::int,
            CASE WHEN random() < .25 THEN (ARRAY['Cumpleaños','Mesa cerca de la ventana','Silla para bebé','Celebración familiar','Reunión de trabajo'])[1 + floor(random()*5)::int] END,
            0, est, fecha_hora, creado,
            CASE WHEN est IN ('COMPLETADA','PAGADA') THEN fecha_hora - (5 + floor(random()*6)::int) * interval '1 minute' END,
            CASE est WHEN 'CANCELADA_AUTO' THEN fecha_hora + interval '1 minute' WHEN 'CANCELADA' THEN LEAST(now(), creado + interval '3 hours') END,
            CASE est WHEN 'CANCELADA_AUTO' THEN 'No se realizó el pago en caja a tiempo' WHEN 'CANCELADA' THEN 'Cancelada por el cliente' END,
            CASE WHEN est IN ('COMPLETADA','PAGADA') THEN CASE WHEN hora < '15:00' THEN 7 ELSE 8 END END)
    RETURNING id_reserva INTO res;
    tot := 0;
    FOR k IN 1..(2 + floor(random()*4)::int) LOOP
      pid := ids[1 + floor(random()*array_length(ids,1))::int];
      cant := 1 + floor(random()*3)::int;
      SELECT p.precio INTO precio FROM producto p WHERE id_producto = pid;
      INSERT INTO reserva_detalle (id_reserva, id_producto, cantidad, precio_unitario, subtotal) VALUES (res, pid, cant, precio, precio*cant);
      tot := tot + precio*cant;
    END LOOP;
    UPDATE reserva SET total = tot WHERE id_reserva = res;
  END LOOP;

  UPDATE reserva r SET codigo = 'RSV-' || to_char(x.creado_en, 'YYYYMMDD') || '-' || lpad(x.rn::text, 4, '0')
  FROM (SELECT id_reserva, creado_en, row_number() OVER (PARTITION BY creado_en::date ORDER BY id_reserva) rn FROM reserva) x
  WHERE r.id_reserva = x.id_reserva;
END $$;

-- ---------------------------------------------------------------- pedidos
CREATE TEMP TABLE t_ped (fecha date, ts timestamp, prep numeric, canal text, id_cliente bigint, id_reserva bigint, registrado bigint, cancela boolean);

DO $$
DECLARE
  d date; n int; j int; u numeric; secs numeric; ts timestamp; hoy timestamp := date_trunc('day', now()); ratio numeric; pk numeric; r numeric; cn text;
BEGIN
  PERFORM setseed(0.77);
  ratio := GREATEST(extract(epoch FROM (now() - interval '25 minutes' - hoy)) / (22*3600), 0.001);
  FOR d IN SELECT generate_series(CURRENT_DATE - 60, CURRENT_DATE, interval '1 day')::date LOOP
    n := CASE WHEN extract(dow FROM d) IN (5,6,0) THEN 52 + floor(random()*26)::int ELSE 30 + floor(random()*20)::int END;
    IF d = CURRENT_DATE THEN n := 36; END IF;
    FOR j IN 1..n LOOP
      u := random();
      secs := CASE WHEN u < .40 THEN 12*3600 + random()*2.5*3600 WHEN u < .85 THEN 18.5*3600 + random()*3*3600 ELSE 11*3600 + random()*11*3600 END;
      ts := d + secs * interval '1 second';
      IF d = CURRENT_DATE THEN ts := hoy + (ts - hoy) * ratio; END IF;
      pk := CASE WHEN secs BETWEEN 12.2*3600 AND 14*3600 OR secs BETWEEN 19*3600 AND 21*3600 THEN 3 ELSE 0 END;
      r := random();
      cn := CASE WHEN r < .58 THEN 'LOCAL' WHEN r < .80 THEN 'PEDIDOSYA' ELSE 'PARA_LLEVAR' END;
      INSERT INTO t_ped VALUES (d, ts, 5 + random()*7 + pk, cn,
        CASE WHEN cn = 'LOCAL' AND random() < .4 THEN 9 + floor(random()*28)::int END, NULL,
        CASE WHEN extract(hour FROM ts) < 15 THEN 7 ELSE 8 END, random() < .035);
    END LOOP;
  END LOOP;
  -- pedidos generados por reservas cobradas en caja
  INSERT INTO t_ped SELECT fecha_encuentro, pagada_en, 9, 'RESERVA', id_cliente, id_reserva, id_cobrada_por, false
  FROM reserva WHERE estado IN ('COMPLETADA','PAGADA');
  -- comandas "en vivo" para la pantalla de despacho
  INSERT INTO t_ped SELECT CURRENT_DATE, now() - (g * interval '1 minute'), 25, 'LOCAL', NULL, NULL, 7, false FROM generate_series(1, 4) g;
  INSERT INTO t_ped SELECT CURRENT_DATE, now() - (g * interval '1 minute'), 7, (ARRAY['PEDIDOSYA','LOCAL','PARA_LLEVAR'])[g - 7], NULL, NULL, 7, false FROM generate_series(8, 10) g;
END $$;

DELETE FROM t_ped WHERE ts > now();

INSERT INTO pedido (numero_turno, fecha_turno, id_cliente, id_registrado_por, canal, estado, total, creado_en, listo_en, entregado_en, cancelado_en, motivo_cancelacion, id_reserva)
SELECT row_number() OVER (PARTITION BY fecha ORDER BY ts, canal), fecha, id_cliente, registrado, canal,
  CASE WHEN cancela THEN 'CANCELADO'
       WHEN ts + (prep + 4) * interval '1 minute' < now() THEN 'ENTREGADO'
       WHEN ts + prep * interval '1 minute' < now() THEN 'LISTO' ELSE 'EN_PREPARACION' END,
  0, ts,
  CASE WHEN NOT cancela AND ts + prep * interval '1 minute' < now() THEN ts + prep * interval '1 minute' END,
  CASE WHEN NOT cancela AND ts + (prep + 4) * interval '1 minute' < now() THEN ts + (prep + 4) * interval '1 minute' END,
  CASE WHEN cancela THEN ts + interval '3 minutes' END,
  CASE WHEN cancela THEN (ARRAY['Cliente se retiró','Error al registrar el pedido','Producto agotado'])[1 + floor(random()*3)::int] END,
  id_reserva
FROM t_ped ORDER BY ts;

DO $$
DECLARE
  ids int[] := ARRAY[1,1,1,1,1,1,1,2,2,2,3,3,3,3,4,4,4,4,4,4,5,5,5,5,5,6,6,6,6,6,7,7,7,8,9,9,9,9,9,9,9,10,10,10,10,11,11,12,12,12,12,13,13,13,13,14,14,14,15,15,15,15,15,16,16,16,17,17,17,17,17,18,18,18,18,19,19,19,19,20,20,20,20,20,21,21,21,21,22,22,22,22,22,23,23,23,23,24,24,25,25,26,27,27,27,28,28,28,28,28,28,28,28,29,29,29,30,30,30,30,32,32,33,33,34,34];
  obs text[] := ARRAY['Sin ensalada','Extra salsa','Presas piernas','Bien cocido','Sin picante','Para llevar en bolsa'];
  p record; k int; pid int; cant int; precio numeric; x numeric;
BEGIN
  PERFORM setseed(0.13);
  FOR p IN SELECT id_pedido, id_reserva FROM pedido ORDER BY id_pedido LOOP
    IF p.id_reserva IS NOT NULL THEN
      INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario, subtotal)
      SELECT p.id_pedido, id_producto, cantidad, precio_unitario, subtotal FROM reserva_detalle WHERE id_reserva = p.id_reserva;
    ELSE
      FOR k IN 1..(1 + (random() < .55)::int + (random() < .30)::int + (random() < .12)::int) LOOP
        pid := ids[1 + floor(random()*array_length(ids,1))::int];
        x := random();
        cant := CASE WHEN x < .78 THEN 1 WHEN x < .93 THEN 2 ELSE 3 END;
        SELECT pr.precio INTO precio FROM producto pr WHERE id_producto = pid;
        INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario, subtotal, observaciones)
        VALUES (p.id_pedido, pid, cant, precio, precio*cant, CASE WHEN random() < .08 THEN obs[1 + floor(random()*6)::int] END);
      END LOOP;
    END IF;
  END LOOP;
END $$;

UPDATE pedido p SET total = (SELECT COALESCE(SUM(subtotal), 0) FROM detalle_pedido d WHERE d.id_pedido = p.id_pedido);
INSERT INTO contador_turno (fecha, ultimo) SELECT fecha_turno, MAX(numero_turno) FROM pedido GROUP BY fecha_turno;
DROP TABLE t_ped;
SELECT setval(pg_get_serial_sequence('pedido','id_pedido'), (SELECT MAX(id_pedido) FROM pedido));
SELECT setval(pg_get_serial_sequence('reserva','id_reserva'), (SELECT MAX(id_reserva) FROM reserva));
SELECT setval(pg_get_serial_sequence('detalle_pedido','id_detalle'), (SELECT MAX(id_detalle) FROM detalle_pedido));
