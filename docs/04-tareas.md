# Plan de Implementación: Control Presupuestal de Prevención (`04-tareas.md`)

Desglose técnico para construir el microservicio bajo **Arquitectura Hexagonal (Scaffold Clean Architecture)** y desplegarlo en **Azure AKS**, cubriendo las historias **HU-1**, **HU-2** y **HU-3**.

---

## Fase 1: Base Hexagonal y Persistencia Core

- [ ] **1.1. Inicializar Scaffold Gradle:** Crear estructura multimódulo (`domain/model`, `domain/usecase`, `infrastructure/entry-points`, `infrastructure/driven-adapters`, `applications/app-service`) con Java 21, Spring Boot 3.3 y WebFlux.
- [ ] **1.2. Modelar Dominio Puro (`domain/model`):** Implementar entidades `PresupuestoAnual`, `PlanTrabajo`, `OrdenServicio`, excepciones de negocio (`SaldoInsuficienteException`, `EstadoInvalidoException`) y puertos (`PresupuestoRepository`, `OrdenServicioRepository`, `IdempotenciaGateway`, `OutboxGateway`).
- [ ] **1.3. Crear Migraciones SQL (Flyway):** Escribir scripts DDL en PostgreSQL 16 para `presupuesto_anual`, `plan_trabajo`, `orden_servicio`, `idempotencia_registro` y `outbox_eventos`, incluyendo restricciones `CHECK` (`saldo_disponible >= 0` y ecuación presupuestal) y `@Version`.
- [ ] **1.4. Implementar Adaptador R2DBC (`infrastructure/driven-adapters`):** Configurar conexión reactiva a PostgreSQL, mapear repositorios implementando los puertos del dominio y validar migraciones Flyway y restricciones `CHECK` con **Testcontainers** (`postgres:16-alpine`).

---

## Fase 2: Historias de Usuario Core (`HU-1`, `HU-2`, `HU-3`)

### HU-1: Planear Orden de Servicio (`RN-01`, `RN-02`, `RN-05`, `CB-04`)
- [ ] **2.1. Caso de Uso (`domain/usecase`):** Implementar `PlanearOrdenServicioUseCase` validando que el `PlanTrabajo` esté activo, calculando `valor_total` indivisible y moviendo el monto de `saldo_disponible` a `saldo_comprometido`.
- [ ] **2.2. Bloqueo de Concurrencia (`CB-04`):** Implementar consulta `@Query` con `SELECT ... FOR UPDATE` sobre `presupuesto_anual` dentro de un `TransactionalOperator` en R2DBC.
- [ ] **2.3. Entry-Point REST (`infrastructure/entry-points`):** Exponer `POST /api/v1/planes-trabajo/{planTrabajoId}/ordenes-servicio` alineado a [openapi.yaml](./anexos/openapi.yaml) para el rol `ProfesionalPrevencion`.
- [ ] **2.4. Pruebas de Concurrencia (`CB-04`):** Validar mediante **Testcontainers** (`PostgreSQLContainer`) y `StepVerifier` que dos peticiones paralelas (`Flux.merge`) sobre el mismo presupuesto activan `SELECT ... FOR UPDATE` y nunca generan saldo negativo.


### HU-2: Ejecutar y Cancelar Orden de Servicio (`RN-04`, `RN-06`, `CB-05`)
- [ ] **2.5. Casos de Uso (`domain/usecase`):**
  - `EjecutarOrdenServicioUseCase`: Transicionar `PLANEADA` a `EJECUTADA` (irreversible) trasladando `saldo_comprometido` a `saldo_ejecutado`.
  - `CancelarOrdenServicioUseCase`: Transicionar `PLANEADA` a `CANCELADA` liberando `saldo_comprometido` hacia `saldo_disponible`.
- [ ] **2.6. Entry-Points REST (`infrastructure/entry-points`):** Exponer `PATCH /api/v1/ordenes-servicio/{ordenServicioId}/ejecutar` (`Prestador`) y `PATCH .../cancelar` (`ProfesionalPrevencion`).
- [ ] **2.7. Pruebas de Transición:** Verificar rechazo (`409 Conflict`) al intentar cancelar una orden `EJECUTADA` o modificar una `CANCELADA`.

### HU-3: Consultar Presupuesto por Empresa (`RN-03`, `RN-09`, `CB-03`)
- [ ] **2.8. Caso de Uso (`domain/usecase`):** Implementar `ConsultarPresupuestoEmpresaUseCase` filtrando por `empresaAfiliadaId` y `vigencia`.
- [ ] **2.9. Aislamiento por Grupo (`RN-09`):** Inyectar filtro obligatorio `WHERE grupo_prevencion_id = :grupoJwt` en el adaptador R2DBC para el rol `LiderPrevencion`.
- [ ] **2.10. Entry-Point REST (`infrastructure/entry-points`):** Exponer `GET /api/v1/empresas-afiliadas/{empresaAfiliadaId}/presupuesto`.

---

## Fase 3: Transversales (Idempotencia, Outbox y Errores)

- [ ] **3.1. Filtro de Idempotencia (`CB-01`):** Interceptar cabecera `X-Idempotency-Key` en peticiones `POST`/`PATCH` dentro de la misma transacción R2DBC para evitar duplicidad de movimientos.
- [ ] **3.2. Transactional Outbox y RabbitMQ:** Persistir eventos (`PresupuestoReservado`, `PresupuestoConsumido`, `PresupuestoLiberado`) en `outbox_eventos`, configurar publicador reactivo con `Reactive Commons` y validar atomicidad y publicación con **Testcontainers** (`postgres:16-alpine` + `rabbitmq:3.13-alpine`).
- [ ] **3.3. Manejador Global de Errores (`CB-02`):** Traducir excepciones de dominio a respuestas estándar **RFC 7807** (`422`, `403`, `409`) con `codigoRegla` y mensaje de negocio.
- [ ] **3.4. Seguridad JWT (Spring Security WebFlux):** Extraer *claims* (`usuarioId`, `rol`, `grupoPrevencionId`) del token validado por Apigee X.

---

## Fase 4: Infraestructura como Código (IaC) y Despliegue en Azure

- [ ] **4.1. Contenerización:** Crear `Dockerfile` *multi-stage* (JRE 21 Alpine) y configurar publicación de imágenes en **Azure Container Registry (ACR)**.
- [ ] **4.2. Aprovisionamiento Cloud (Terraform):** Definir módulos IaC para **Azure Database for PostgreSQL 16 (Flexible Server)**, **Azure Key Vault** y políticas de red en la VNet corporativa.
- [ ] **4.3. Despliegue en AKS (Helm Chart):** Configurar manifiestos Kubernetes (`Deployment`, `Service`, *Ingress*, *HPA* para 250 usuarios concurrentes, *Actuator probes* y montaje de secretos desde Key Vault).
- [ ] **4.4. Pipeline CI/CD (Azure DevOps):** Automatizar compilación Gradle, pruebas unitarias/integración, despliegue continuo en **Azure AKS**, instrumentación con **Dynatrace** y publicación del contrato `openapi.yaml` en **Apigee X**.