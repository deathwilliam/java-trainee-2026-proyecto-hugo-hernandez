# FINCOM — Sistema Seguro de Gestión y Liquidación de Comisiones Financieras

## 1. Nombre y descripción
FINCOM es una API REST para calcular y liquidar comisiones de agentes financieros por operaciones confirmadas. Aplica tarifas según la fecha de la operación, evita liquidaciones duplicadas, registra la trazabilidad y emite órdenes internas de pago. Está inspirada en los corresponsales financieros de El Salvador, pero las tarifas y los datos del ejercicio son simulados.

## 2. Quién usa el sistema

| Rol | Quién es | Qué puede hacer |
|---|---|---|
| **ADMIN** | Administración financiera | Gestiona usuarios, roles, servicios y tarifas; ejecuta liquidaciones; consulta órdenes y bitácoras. |
| **ASESOR** | Agente financiero autorizado | Registra y consulta las operaciones del agente al que está vinculado; consulta sus liquidaciones. |
| **CLIENTE** | Titular de una operación financiera | Consulta solamente sus propias operaciones. |

`usuario_rol` representa la relación M:N entre usuarios y roles. `agente.usuario_id` (único) y `cliente.usuario_id` (único, opcional) permiten comprobar la identidad y restringir el acceso a datos propios. Spring Security se incorporará en la Etapa 4.

## 3. Entidades (12 tablas)

| Entidad | Responsabilidad |
|---|---|
| `usuario` | Credenciales y estado de las cuentas que acceden a FINCOM. |
| `rol` | Catálogo de perfiles autorizados. |
| `usuario_rol` | Asignación única de roles a usuarios (tabla intermedia). |
| `cliente` | Titular de operaciones; se vincula opcionalmente a una cuenta de usuario. |
| `agente` | Operador que realiza operaciones y recibe comisiones. |
| `tipo_servicio` | Catálogo de servicios financieros permitidos. |
| `tarifa_comision` | Tarifa fija configurada por servicio y período de vigencia. |
| `operacion` | Operación con importe, estado, fecha, referencia única y tarifa utilizada. |
| `liquidacion` | Cabecera mensual por agente; `total_comisiones` es **DERIVADO** (R8). |
| `detalle_liquidacion` | Comisión individual aplicada a una operación: copia histórica del importe al liquidar (R13). |
| `orden_pago` | Orden interna 1:1 de la liquidación, sin monto duplicado; consulta su importe desde `liquidacion`. |
| `bitacora_operacion` | Quién intentó liquidar, qué acción realizó, su resultado y cuándo ocurrió. |

## 4. Operación principal: `liquidarComisiones` (`@Transactional`)

**Entrada:** `agenteId`, `anio`, `mes`, `usuarioEjecutorId`.

**Validaciones previas:** R3 (agente/usuario activos), R4 (solo operaciones CONFIRMADA del agente y período), R5 (período no liquidado), R6 (sin operaciones previamente liquidadas), R7 (tarifa vigente en la fecha de la operación), R10 (existen operaciones), R12 (período correcto) y R14 (tarifas no solapadas). El período se filtra con `fecha_operacion >= inicioMes` y `< inicioMesSiguiente`.

**Escrituras atómicas y en este orden:**
1. **`liquidacion`:** crea cabecera única para `(agente_id, anio, mes)` y calcula `total_comisiones`.
2. **`detalle_liquidacion`:** inserta un detalle por operación; copia `tarifa_comision.monto_fijo` en `monto_comision` como **valor histórico** (R13).
3. **`operacion`:** cambia cada estado de `CONFIRMADA` a `LIQUIDADA`.
4. **`orden_pago`:** crea **exactamente una** orden por liquidación (`liquidacion_id` único), con `numero_orden` único y estado `PENDIENTE`; **no almacena monto**.
5. **`bitacora_operacion`:** registra `accion = LIQUIDACION_GENERADA`, `resultado = EXITOSO`, `usuario_id` y `liquidacion_id`.

**Rollback:** si, por ejemplo, falla el paso 4, **se revierten los cinco cambios**; no queda una liquidación parcial ni operaciones marcadas como liquidadas. Después del rollback, otro servicio registra `accion = LIQUIDACION_FALLIDA`, `resultado = FALLIDO` y `liquidacion_id = NULL` en una **transacción independiente**, sin apuntar a registros revertidos.

**Salida:** `liquidacionId`, `numeroOrden`, `cantidadOperaciones`, `totalComisiones` y `estadoOrden = PENDIENTE`. Ejemplo *simulado*: 10 operaciones a $0.40 de comisión cada una generan $4.00; el importe de la orden se consulta en `liquidacion.total_comisiones`. La asociación `liquidacion` ↔ `orden_pago` es **1:1 por regla del servicio**: la creación de ambos registros forma parte de la misma transacción.

## 5. Reglas de negocio verificables

| # | Regla con nombres exactos del modelo | Dónde se cumple | Si falla |
|---|---|---|---|
| R1 | `operacion.monto > 0`. | BD: `CHECK` + `NOT NULL`. | Se rechaza el monto de operación. |
| R2 | `tarifa_comision.monto_fijo > 0` y `detalle_liquidacion.monto_comision > 0`. | BD: `CHECK` + `NOT NULL`. | Se rechaza el importe no positivo. |
| R3 | `agente.activo = true` y `usuario.activo = true` al liquidar. | Servicio. | «Agente o usuario inactivo». |
| R4 | Solo `operacion.estado = CONFIRMADA` del agente y mes pasa a `LIQUIDADA`. | Servicio. | «Operación no elegible». |
| R5 | `UNIQUE(liquidacion.agente_id, liquidacion.anio, liquidacion.mes)`. | BD: `UNIQUE`. | «Período ya liquidado»; rollback. |
| R6 | `detalle_liquidacion.operacion_id` es `UNIQUE`. | BD: `UNIQUE`. | «Operación ya liquidada»; rollback. |
| R7 | `operacion.tarifa_comision_id` identifica tarifa **activa y vigente en `operacion.fecha_operacion`**; el servicio se obtiene desde `tarifa_comision.tipo_servicio_id`. | Servicio al registrar y liquidar. | «Tarifa inválida o fuera de vigencia». |
| R8 | `liquidacion.total_comisiones = SUM(detalle_liquidacion.monto_comision)`; **DERIVADO** mantenido en la transacción. | Servicio al liquidar. | Descuadre: rollback. |
| R10 | Existe al menos una `operacion` `CONFIRMADA` elegible en el período. | Servicio. | «Sin operaciones para liquidar». |
| R11 | Una `orden_pago` nace `PENDIENTE`; solo `PENDIENTE` puede pasar a `AUTORIZADA`. Cada `liquidacion` tiene **exactamente una orden** creada atómicamente. | Servicio + `orden_pago.liquidacion_id` `UNIQUE NOT NULL`. | «Estado inválido» o rollback. |
| R12 | `liquidacion.mes BETWEEN 1 AND 12`, `anio BETWEEN 2020 AND 2100`; `vigente_hasta` nula o `>= vigente_desde`. | BD: `CHECK`; servicio. | «Período o vigencia inválidos». |
| R13 | `detalle_liquidacion.monto_comision` copia `tarifa_comision.monto_fijo` al liquidar y queda como **copia histórica**; no se recalcula si luego se modifica la tarifa. | Servicio. | Inconsistencia: rollback. |
| R14 | No se permiten intervalos de vigencia **activos** superpuestos para el mismo `tarifa_comision.tipo_servicio_id`. | Servicio al configurar la tarifa. | «Tarifas con vigencia superpuesta». |
| R15 | `bitacora_operacion.accion` ∈ {`LIQUIDACION_GENERADA`, `LIQUIDACION_FALLIDA`}; `resultado` ∈ {`EXITOSO`, `FALLIDO`}. Si falla, se registra fuera de la transacción revertida y `liquidacion_id` debe ser nulo. | Enum + servicio de auditoría. | «Acción o resultado inválidos»; no se deja FK huérfana. |

**Enums compartidos:** `EstadoOperacion` E1 = `PENDIENTE`, `CONFIRMADA`, `ANULADA`, `LIQUIDADA`; `EstadoOrdenPago` E2 = `PENDIENTE`, `AUTORIZADA`; `ResultadoOperacion` E3 = `EXITOSO`, `FALLIDO`; `AccionBitacora` E4 = `LIQUIDACION_GENERADA`, `LIQUIDACION_FALLIDA`.

## 6. Lo que FINCOM no hace
- No ejecuta transferencias de fondos reales ni se conecta con plataformas bancarias externas.
- No calcula impuestos ni retenciones tributarias.
- No revierte liquidaciones emitidas ni realiza ajustes contables retroactivos.
