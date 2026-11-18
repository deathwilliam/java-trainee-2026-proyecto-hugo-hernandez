# FINCOM — API REST de Liquidación de Comisiones


**Video de demostración (15 minutos):** [Ver presentación de FINCOM en YouTube](https://www.youtube.com/watch?v=rOm9mxBmL3s)

## 1. Descripción

FINCOM es una API REST desarrollada con Java y Spring Boot para calcular, validar y liquidar comisiones de agentes financieros.

Su objetivo es hacer más confiable el proceso de liquidación, evitando duplicidades, excluyendo operaciones anuladas y manteniendo la consistencia de los registros financieros.

## 2. Tecnologías

- Java 21 (versión de compilación)
- Spring Boot 4.1.2
- Spring Security y JWT
- Spring Data JPA y Hibernate
- PostgreSQL 17
- Maven
- JUnit 5, Mockito y MockMvc
- Swagger / OpenAPI
- Docker y Docker Compose

## 3. Modelo de datos

FINCOM utiliza 12 entidades principales:

1. Usuario
2. Rol
3. UsuarioRol
4. Agente
5. Cliente
6. TipoServicio
7. TarifaComision
8. Operacion
9. Liquidacion
10. DetalleLiquidacion
11. OrdenPago
12. BitacoraOperacion

Las relaciones permiten identificar al agente, el cliente, las operaciones financieras y las comisiones incluidas en cada liquidación.

## 4. Funcionalidades principales

- Autenticación mediante JWT y contraseñas protegidas con BCrypt.
- Autorización basada en tres roles.
- Registro y consulta de operaciones financieras.
- Confirmación y anulación de operaciones.
- Administración de tarifas de comisión.
- Validación de vigencia de tarifas.
- Liquidación mensual de comisiones por agente.
- Prevención de liquidaciones duplicadas.
- Exclusión de operaciones anuladas o no elegibles.
- Registro de detalles de liquidación y órdenes de pago.
- Bitácora de resultados de liquidación.
- Transacciones y rollback ante fallos.
- Manejo estructurado de errores HTTP.

## 5. Roles y seguridad

La API utiliza Spring Security, JWT y autorización por roles.

| Rol | Permisos principales |
|---|---|
| ADMIN | Consultar operaciones, administrar funciones autorizadas y ejecutar liquidaciones. |
| ASESOR | Consultar, registrar y modificar operaciones vinculadas a su agente. Consultar sus liquidaciones. |
| CLIENTE | Consultar únicamente sus propias operaciones. No puede registrar ni modificar operaciones. |

El identificador del usuario autenticado se obtiene del JWT, evitando utilizar arbitrariamente un identificador enviado por el cliente para autorizar consultas.

### Respuestas de seguridad

- **401 Unauthorized:** solicitud sin autenticación válida.
- **403 Forbidden:** usuario autenticado sin permisos suficientes.
- **404 Not Found:** recurso inexistente o no accesible mediante las consultas restringidas.

Los errores de autenticación y autorización se presentan mediante respuestas estructuradas `ProblemDetail`.

## 6. Ejecución con Docker Compose

### Requisitos

- Docker Desktop
- Docker Compose

### Configuración

Desde la carpeta `proyecto/api`, preparar un archivo `.env` con las variables necesarias para PostgreSQL y JWT.

Si está disponible el archivo `.env.ejemplo`, utilizarlo como referencia.

La clave de JWT debe ser suficientemente segura para el algoritmo configurado.

**No subir `.env` ni contraseñas privadas a GitHub.**

### Iniciar los servicios

```powershell
docker compose up -d --build
```

### Comprobar su estado

```powershell
docker compose ps
```

### Consultar registros

```powershell
docker compose logs -f api
```

### Detener los servicios

```powershell
docker compose down
```

No utilizar `docker compose down -v` salvo que se desee eliminar deliberadamente los volúmenes de datos.

### Direcciones locales

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

## 7. Endpoints principales

### Autenticación

`POST /api/auth/login`

### Operaciones financieras

- `GET /api/operaciones`
- `GET /api/operaciones/{id}`
- `POST /api/operaciones`
- `PATCH /api/operaciones/{id}`
- `DELETE /api/operaciones/{id}`

### Tarifas de comisión

- `GET /api/tarifas`
- `GET /api/tarifas/{id}`
- `POST /api/tarifas`
- `DELETE /api/tarifas/{id}`

### Liquidaciones

- `GET /api/liquidaciones`
- `GET /api/liquidaciones/{id}`
- `POST /api/liquidaciones`

Las operaciones disponibles dependen del rol autenticado.

## 8. Credenciales de demostración

Las siguientes cuentas corresponden exclusivamente al entorno académico de prueba.

| Rol | Correo | Contraseña |
|---|---|---|
| ADMIN | admin@fincom.test | FincomAdmin2026! |
| ASESOR | asesor@fincom.test | FincomDemo2026! |
| CLIENTE | cliente@fincom.test | FincomLector2026! |

Las contraseñas se almacenan como hashes BCrypt en PostgreSQL.

Estas credenciales no deben utilizarse en producción.

### Ejemplo de inicio de sesión

**POST `/api/auth/login`**

```json
{
  "correo": "admin@fincom.test",
  "clave": "FincomAdmin2026!"
}
```

La autenticación correcta devuelve un token JWT.

Las solicitudes a endpoints protegidos deben incluir:

```text
Authorization: Bearer <TOKEN_JWT>
```

## 9. Ejemplo de liquidación

La operación principal de FINCOM consiste en liquidar las comisiones de un agente financiero correspondientes a un mes.

**POST `/api/liquidaciones`**

Requiere el rol `ADMIN`.

```json
{
  "agenteId": 1,
  "anio": 2026,
  "mes": 10
}
```

Una liquidación creada correctamente devuelve `201 Created`.

Reglas principales:

- El agente debe existir y estar activo.
- Solo se consideran operaciones elegibles del período.
- Las operaciones anuladas no generan comisiones.
- Las tarifas deben cumplir las reglas de vigencia.
- No se permite liquidar dos veces el mismo agente y período.
- Se generan los detalles y la orden de pago.
- Los fallos transaccionales deben revertir los cambios correspondientes.
- Se registra información de auditoría del proceso.

## 10. Pruebas automatizadas

FINCOM dispone de pruebas con JUnit 5, Mockito, MockMvc y Spring Boot.

Se verifican:

- Reglas de negocio para liquidaciones.
- Prevención de duplicidades.
- Rollback transaccional.
- Validación de tarifas.
- Seguridad HTTP.
- Autenticación JWT.
- Autorización `401` y `403` mediante MockMvc.
- Consultas restringidas de liquidaciones.

### Configuración de la base de pruebas

Se utiliza una base PostgreSQL independiente llamada `fincom_test`.

En el entorno local utilizado para verificar el proyecto, PostgreSQL está expuesto en el puerto `5435`.

Desde PowerShell:

```powershell
$env:DB_TEST_URL = "jdbc:postgresql://localhost:5435/fincom_test"
.\mvnw.cmd test
```

La base de pruebas debe existir y estar disponible antes de ejecutar Maven.

### Última ejecución verificada

**10 de noviembre de 2026**

- 18 pruebas ejecutadas.
- 0 fallos.
- 0 errores.
- 0 pruebas omitidas.
- `BUILD SUCCESS`.

## 11. Consideraciones técnicas

- Se utiliza `@Transactional` para proteger la integridad de las liquidaciones.
- Las reglas financieras impiden incluir operaciones anuladas y duplicadas.
- El usuario ejecutor se identifica a partir del JWT.
- Los datos de demostración se incorporan mediante `import.sql`.
- La configuración académica `ddl-auto=create` recrea las tablas al iniciar la aplicación.
- Esta configuración es apropiada para demostraciones y pruebas, pero no para conservar datos de producción.
- Las credenciales privadas deben gestionarse mediante variables de entorno.

## 12. Proyecto académico

Proyecto integrador de Java y Spring Boot.

Programa de formación Backend — 2026.

