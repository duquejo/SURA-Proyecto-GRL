# Plan de Implementación: Control Presupuestal de Prevención

Desglose técnico para construir la plataforma completa (Backend WebFlux + Frontend Angular) bajo **Arquitectura Hexagonal** y desplegarla en **Azure**, alineado con la nomenclatura del plan del equipo y el flujo SDD.

---

## Fase 1: Base Hexagonal e Infraestructura Backend

- [x] **HT-001: Estructura base (Scaffold Gradle y Dominio Puro):** Inicializar estructura multimódulo (`domain/model`, `domain/usecase`, `infrastructure/entry-points`, `infrastructure/driven-adapters`, `applications/app-service`) con Java 21, Spring Boot 3.3 y WebFlux. Modelar entidades puras (`PresupuestoAnual`, `PlanTrabajo`, `OrdenServicio`, `EvaluacionRiesgo`, `ServicioPrevencion`, `ContextoUsuario`), excepciones de negocio y puertos transaccionales.
- [x] **HT-002: Asignar Presupuesto Anual (`RN-03`, `RN-09`, `RN-10`):** Implementar `AsignarPresupuestoUseCase` para fijar o ajustar el monto del `PresupuestoAnual` de una empresa, validando que el saldo no sea heredado de años anteriores. Exponer `POST /api/v1/empresas-afiliadas/{empresaAfiliadaId}/presupuestos`, restringido al `LiderPrevencion` y validando que la empresa pertenezca a su `grupoPrevencionId`.
- [x] **HT-003: Persistencia Core y Auditoría (Flyway, R2DBC y Testcontainers):** Escribir scripts DDL en PostgreSQL 16 (Flyway) para el modelo relacional y la **tabla de auditoría inmutable** (registrando usuario, rol, timestamp y valores nuevo/anterior). Incluir restricciones `CHECK` (`saldo_disponible >= 0` y ecuación presupuestal) y `@Version`. Configurar Adaptador R2DBC y validar con **Testcontainers**.
- [ ] **HT-004: Evaluación de Riesgo y Reajuste de Plan (`RN-07`, `RN-08`):** Implementar `RegistrarEvaluacionRiesgoUseCase` (vincular `EvaluacionRiesgo` a orden `EJECUTADA`) y `ReajustarPlanTrabajoUseCase`. Si el riesgo no disminuyó, devolver el `PlanTrabajo` a `EN_PLANEACION`; si disminuyó, transicionar a `CERRADO`.
- [ ] **HT-005: Seguridad JWT y Manejador Global de Errores (`CB-02`, `CB-03`):** Configurar Spring Security WebFlux para extraer *claims* (`usuarioId`, `rol`, `grupoPrevencionId`) del token JWT hacia `ContextoUsuario`. Implementar manejador global (`@RestControllerAdvice`) que traduzca errores de negocio a respuestas **RFC 7807** (`422`, `403`, `409`).
- [ ] **HT-006: IaC Base Backend (Contenerización y Terraform):** Crear `Dockerfile` *multi-stage* hacia **Azure Container Registry (ACR)**. Definir módulos Terraform para **Azure Database for PostgreSQL 16**, **Azure Key Vault** y VNet.

---

## Fase 2: Ciclo de Vida Backend y Base Frontend

- [ ] **HT-007: Planear Orden de Servicio (`RN-01`, `RN-02`, `RN-05`, `CB-06`):** Implementar `PlanearOrdenServicioUseCase`. **Rechazar operación si la empresa no tiene presupuesto activo (`CB-06`)**. Validar plan, descontar `saldo_disponible` hacia `saldo_comprometido` y **transicionar el `PlanTrabajo` a `EN_EJECUCION`** al planear la primera orden.
- [ ] **HT-008: Bloqueo Pesimista de Concurrencia (`CB-04`):** Implementar `@Query` con `SELECT ... FOR UPDATE` sobre `presupuesto_anual` en R2DBC. Validar con **Testcontainers** y `Flux.merge` que dos peticiones paralelas activan el bloqueo y no generan saldo negativo.
- [ ] **HT-009: Ejecutar/Cancelar Orden y Cierre Anual (`RN-04`, `RN-06`, `CB-05`):** Implementar `EjecutarOrdenServicioUseCase` (a `EJECUTADA`), **validando que el `usuarioId` del `Prestador` coincida con el `prestador_id` de la orden**. Implementar `CancelarOrdenServicioUseCase` (liberar saldo). Implementar **Batch/CRON 31-Dic (`RN-04`)** para cancelar órdenes remanentes. Exponer endpoints `PATCH /ejecutar` y `PATCH /cancelar`.
- [ ] **HT-010: Consultas Aisladas y Solicitudes SST (`RN-09`, `CB-03`):** Implementar `ConsultarPresupuestoEmpresaUseCase` con filtro `WHERE grupo_prevencion_id = :grupoJwt` para el `LiderPrevencion`. Implementar `ConsultarPlanTrabajoUseCase` y exponer endpoint para que el `RepresentanteSST` pueda **solicitar nuevos servicios**.
- [ ] **HT-011: Manifiestos Kubernetes (Helm Chart AKS):** Configurar `Deployment`, `Service`, *Ingress*, *HPA* para 250 usuarios, y montaje de secretos desde Key Vault mediante *Secrets Store CSI Driver*.
- [ ] **HT-022 Interfaces de Asignación y Planeación:** Generar en Angular los componentes del formulario de **Asignar Presupuesto** (`LiderPrevencion`) y el asistente/wizard para **Planear Orden de Servicio** visualizando el saldo en tiempo real (`ProfesionalPrevencion`).
- [ ] **HT-024 Base Angular y Seguridad MSAL:** Configurar workspace Angular (Standalone Components), gestor de estado con **NgRx Signal Store** y autenticación mediante **Azure MSAL (Entra ID)** para interceptar y adjuntar el token JWT en las peticiones.

---

## Fase 3: Integración, Resiliencia y Vistas Operativas

- [ ] **HT-012: Endpoint de Planeación e Integración:** Exponer `POST /api/v1/planes-trabajo/{planTrabajoId}/ordenes-servicio` integrando la orquestación y el bloqueo pesimista.
- [ ] **HT-013: Endpoint de Evaluación de Riesgo (`RN-07`, `RN-08`):** Exponer `POST /api/v1/ordenes-servicio/{ordenServicioId}/evaluaciones` conectando `RegistrarEvaluacionRiesgoUseCase` y `ReajustarPlanTrabajoUseCase`.
- [ ] **HT-014: Mensajería (Transactional Outbox y RabbitMQ):** Persistir eventos inmutables en `outbox_eventos` dentro de la transacción R2DBC. Configurar publicador con `Reactive Commons` y validar con **Testcontainers**.
- [ ] **HT-015: Filtro de Idempotencia (`CB-01`):** Interceptar `X-Idempotency-Key` en WebFlux para devolver resultado exitoso previo desde la tabla `idempotencia_registro` sin duplicar transacciones.
- [ ] **HT-016: Tests de Aislamiento y Seguridad RBAC (`RN-09`, `RN-10`):** Pruebas de integración validando restricciones por rol y rechazos `403 Forbidden` ante datos fuera del `grupoPrevencionId`.
- [ ] **HT-017: Pipeline CI/CD, Apigee X y Dynatrace:** Automatizar compilación backend en **Azure DevOps**, despliegue en AKS, instrumentación con Dynatrace y publicación en Apigee X.
- [ ] **HT-023 Interfaces de Ejecución y Riesgo:** Construir la bandeja operativa del **Prestador** (botón de ejecutar en campo) y el formulario del **Profesional** para registrar la **Evaluación de Riesgo**.
- [ ] **HT-025 Despliegue Frontend y Dashboards:** Construir los tableros de visualización de saldos y planes. Aprovisionar **Azure Static Web Apps** (o Azure CDN) vía Terraform y automatizar el pipeline de compilación y pase a producción del frontend en Azure DevOps.

---

## Fase 4: Pruebas End-to-End (Full-Stack) y Salida a Producción

- [ ] **HT-018: Pruebas E2E de Estrés y Concurrencia (`CB-04`):** Validar bajo carga (250 usuarios) que el bloqueo `SELECT FOR UPDATE` mantiene latencias menores a 2 segundos en el backend.
- [ ] **HT-019: Pruebas E2E de Ciclo Completo y Eventos AMQP:** Ejecutar el flujo integral desde la interfaz Angular autenticada (`Asignar` -> `Planear` -> `Ejecutar` -> `Evaluar` -> `Reajustar`) validando la entrega de eventos en RabbitMQ y la auditoría.
- [ ] **HT-020: Certificación E2E de Seguridad e Idempotencia (`CB-01`, `CB-03`):** Validar desde Angular interceptado por **Apigee X** el rechazo de acciones duplicadas y el aislamiento estricto de los *dashboards*.
- [ ] **HT-021: Observabilidad en Dynatrace y Go-Live (PDN):** Verificar rastreo distribuido desde la interacción en el navegador (RUM) hasta WebFlux, R2DBC y AMQP en Dynatrace, y ejecutar el despliegue final.
