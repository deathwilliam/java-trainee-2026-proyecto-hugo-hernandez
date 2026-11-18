
-- ============================================
-- FINCOM - CREDENCIALES DE PRUEBA
-- ============================================
-- Cuentas exclusivas para pruebas y demostraciones.
--
-- ADMIN
-- Correo: admin@fincom.test
-- Clave:
-- docker compose logs --tail=100 api | Select-String "EjecutorLiquidacionService"

-- ASESOR
-- Correo: asesor@fincom.test
-- Clave: FincomDemo2026!
--
-- CLIENTE
-- Correo: cliente@fincom.test
-- Clave: FincomLector2026!
--
-- Las contrasenas se almacenan mediante BCrypt.
-- ============================================


-- ROLES DEL DISEÑO ORIGINAL FINCOM

INSERT INTO rol (codigo, nombre, activo, creado_en)
VALUES ('ADMIN', 'Administracion financiera', TRUE, CURRENT_TIMESTAMP);

INSERT INTO rol (codigo, nombre, activo, creado_en)
VALUES ('ASESOR', 'Agente financiero autorizado', TRUE, CURRENT_TIMESTAMP);

INSERT INTO rol (codigo, nombre, activo, creado_en)
VALUES ('CLIENTE', 'Titular de operaciones financieras', TRUE, CURRENT_TIMESTAMP);


-- USUARIOS CON CONTRASENAS BCrypt
INSERT INTO usuario (correo, clave_hash, activo, creado_en)
VALUES ('asesor@fincom.test', '$2y$10$F/jNhJi0mZB/Ab3DvLP9IesjzgpQAvqgsoZ6pelIGN5Etnzs5dI4m', TRUE, CURRENT_TIMESTAMP);

INSERT INTO usuario (correo, clave_hash, activo, creado_en)
VALUES ('cliente@fincom.test', '$2y$10$zln.YWQg5ZU4cM1lcfcMie0T5nyWtpVccXVIcBzQaJjUwb7Flb57S', TRUE, CURRENT_TIMESTAMP);

-- ASIGNACION DE ROLES
INSERT INTO usuario_rol (usuario_id, rol_id, creado_en)
SELECT u.id, r.id, CURRENT_TIMESTAMP
FROM usuario u, rol r
WHERE u.correo = 'asesor@fincom.test'
  AND r.codigo = 'ASESOR';

INSERT INTO usuario_rol (usuario_id, rol_id, creado_en)
SELECT u.id, r.id, CURRENT_TIMESTAMP
FROM usuario u, rol r
WHERE u.correo = 'cliente@fincom.test'
  AND r.codigo = 'CLIENTE';


-- USUARIO ADMINISTRADOR
INSERT INTO usuario (correo, clave_hash, activo, creado_en)
VALUES (
           'admin@fincom.test',
           '$2b$10$8/I6TMk52p8MpV9WKJYiC.8yl6taZxIzR1LHgRlGLFYBPpD7Gto7u',
           TRUE,
           CURRENT_TIMESTAMP
       );

-- ASIGNACION DEL ROL ADMIN
INSERT INTO usuario_rol (usuario_id, rol_id, creado_en)
SELECT u.id, r.id, CURRENT_TIMESTAMP
FROM usuario u, rol r
WHERE u.correo = 'admin@fincom.test'
  AND r.codigo = 'ADMIN';


-- TIPO DE SERVICIO
INSERT INTO tipo_servicio (codigo, descripcion, activo, creado_en)
VALUES ('PAGO', 'Pago de servicios financieros', TRUE, CURRENT_TIMESTAMP);

-- AGENTE FINANCIERO
INSERT INTO agente (usuario_id, codigo_agente, nombre, activo, creado_en)
SELECT u.id, 'AG-001', 'Agente de prueba', TRUE, CURRENT_TIMESTAMP
FROM usuario u
WHERE u.correo = 'asesor@fincom.test';

-- CLIENTE

-- CLIENTE VINCULADO A SU USUARIO
INSERT INTO cliente (usuario_id, codigo_cliente, nombre, activo, creado_en)
SELECT u.id, 'CL-001', 'Cliente de prueba', TRUE, CURRENT_TIMESTAMP
FROM usuario u
WHERE u.correo = 'cliente@fincom.test';


-- TARIFA
INSERT INTO tarifa_comision
(tipo_servicio_id, monto_fijo, vigente_desde, vigente_hasta, activo, creado_en)
SELECT t.id, 0.50, DATE '2026-01-01', NULL, TRUE, CURRENT_TIMESTAMP
FROM tipo_servicio t
WHERE t.codigo = 'PAGO';

-- OPERACIONES FINANCIERAS
INSERT INTO operacion
(referencia, agente_id, cliente_id, tarifa_comision_id, monto, estado, fecha_operacion)
SELECT 'OP-OCT-001', a.id, c.id, t.id, 100.00, 'CONFIRMADA',
       TIMESTAMP '2026-10-05 10:00:00'
FROM agente a, cliente c, tarifa_comision t
WHERE a.codigo_agente = 'AG-001'
  AND c.codigo_cliente = 'CL-001'
  AND t.tipo_servicio_id = (SELECT id FROM tipo_servicio WHERE codigo = 'PAGO')
  AND t.activo = TRUE
  AND t.vigente_desde <= DATE '2026-10-05'
  AND (t.vigente_hasta IS NULL OR t.vigente_hasta >= DATE '2026-10-05');

INSERT INTO operacion
(referencia, agente_id, cliente_id, tarifa_comision_id, monto, estado, fecha_operacion)
SELECT 'OP-OCT-002', a.id, c.id, t.id, 200.00, 'CONFIRMADA',
       TIMESTAMP '2026-10-12 14:30:00'
FROM agente a, cliente c, tarifa_comision t
WHERE a.codigo_agente = 'AG-001'
  AND c.codigo_cliente = 'CL-001'
  AND t.tipo_servicio_id = (SELECT id FROM tipo_servicio WHERE codigo = 'PAGO')
  AND t.activo = TRUE
  AND t.vigente_desde <= DATE '2026-10-12'
  AND (t.vigente_hasta IS NULL OR t.vigente_hasta >= DATE '2026-10-12');

INSERT INTO operacion
(referencia, agente_id, cliente_id, tarifa_comision_id, monto, estado, fecha_operacion)
SELECT 'OP-OCT-003', a.id, c.id, t.id, 150.00, 'PENDIENTE',
       TIMESTAMP '2026-10-15 09:00:00'
FROM agente a, cliente c, tarifa_comision t
WHERE a.codigo_agente = 'AG-001'
  AND c.codigo_cliente = 'CL-001'
  AND t.tipo_servicio_id = (SELECT id FROM tipo_servicio WHERE codigo = 'PAGO')
  AND t.activo = TRUE
  AND t.vigente_desde <= DATE '2026-10-15'
  AND (t.vigente_hasta IS NULL OR t.vigente_hasta >= DATE '2026-10-15');

INSERT INTO operacion
(referencia, agente_id, cliente_id, tarifa_comision_id, monto, estado, fecha_operacion)
SELECT 'OP-SEP-001', a.id, c.id, t.id, 80.00, 'CONFIRMADA',
       TIMESTAMP '2026-09-20 11:00:00'
FROM agente a, cliente c, tarifa_comision t
WHERE a.codigo_agente = 'AG-001'
  AND c.codigo_cliente = 'CL-001'
  AND t.tipo_servicio_id = (SELECT id FROM tipo_servicio WHERE codigo = 'PAGO')
  AND t.activo = TRUE
  AND t.vigente_desde <= DATE '2026-09-20'
  AND (t.vigente_hasta IS NULL OR t.vigente_hasta >= DATE '2026-09-20');
