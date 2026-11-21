# Requerimientos de Negocio: Plataforma de control de presupuesto de prevención

## 1. Objetivos y métricas de éxito (KPIs)
- **Objetivo de la entrega:** Constituir un Mínimo Producto Viable (MVP) que centralice en la nube el control presupuestal de prevención, reemplazando las hojas de cálculo mediante la trazabilidad en tiempo real del ciclo de vida de las "Órdenes de servicio".
- **Criterios de éxito:**
  - 0% de "Órdenes de servicio" planeadas por encima del "Saldo disponible" de una "Empresa afiliada".
  - 100% de liberación inmediata del "Saldo comprometido" hacia el "Saldo disponible" al ejecutar la cancelación de una "Orden de servicio".
  - Consulta instantánea del desglose financiero ("Presupuesto anual", "Saldo disponible", "Saldo comprometido" y "Saldo ejecutado") por empresa y por grupo de prevención, sin consolidación manual.
  - 100% de las "Órdenes de servicio" ejecutadas cuentan con trazabilidad hasta su "Evaluación de riesgo" y eventual acción de "Reajustar plan de trabajo".

## 2. Alcance (scope)
- **Dentro-del-alcance (Incluido):**
  - Gestión de visibilidad y aislamiento por grupos de "Profesionales de prevención" para cada "Líder en prevención".
  - Ejecución de "Asignar presupuesto anual" por "Empresa afiliada" y consulta de saldos en tiempo real.
  - Gestión del ciclo de vida de las "Órdenes de servicio" dentro de un "Plan de trabajo": solicitud por el "Representante SST", planeación (reserva), ejecución en campo por el "Prestador" o cancelación (liberación).
  - Registro de "Evaluación de riesgo" por el "Profesional de prevención" y flujo para "Reajustar plan de trabajo" cuando el riesgo no disminuya.
- **Fuera-de-alcance (Excluido explícitamente de esta fase):**
  - Carga masiva o migración histórica de documentos Excel a la plataforma.
  - Desglose o gestión granular por ítems individuales dentro de una "Orden de servicio" (se trata como paquete indivisible).
  - Cancelaciones o ejecuciones parciales de una "Orden de servicio", así como cualquier tipo de reembolso sobre el "Saldo ejecutado".
  - Facturación electrónica, órdenes de compra contables o pagos bancarios reales a los "Prestadores".

## 3. Catálogo de requerimientos funcionales (historias de usuario)
El detalle de reglas específicas y criterios de aceptación en Gherkin se encuentra desacoplado en el directorio `./historias/`:
| ID | Historia de Usuario | Actor | Reglas Vinculadas | Prioridad |
| :--- | :--- | :--- | :--- | :--- |
| **HU-1** | [Planear orden de servicio y reservar presupuesto](./historias/HU-1-reservar-presupuesto.md) | Profesional de prevención | RN-01, RN-02, RN-05 | Alta |
| **HU-2** | [Actualizar presupuesto por ejecución o cancelación](./historias/HU-2-ejecutar-cancelar-orden.md) | Prestador / Profesional de prevención | RN-02, RN-05, RN-06, RN-08 | Alta |
| **HU-3** | [Consultar estado presupuestal por empresa afiliada](./historias/HU-3-consultar-saldos-empresa.md) | Líder en prevención | RN-03, RN-09 | Alta |

## 4. Casos borde y excepciones de negocio
- **CB-01 (Duplicados e idempotencia):** Si una misma petición de `planearOrdenServicio()`, ejecución en campo o `cancelarOrdenServicio()` llega más de una vez (por reintentos de red o doble clic), el sistema debe reconocer el identificador único de la operación, devolver el resultado ya procesado y evitar duplicar la **Orden de servicio** o alterar dos veces los saldos.
- **CB-02 (Manejo de excepciones de negocio):** Ante cualquier incumplimiento de las reglas de negocio *RN-01* a *RN-10*, el sistema debe abortar la transacción y retornar un mensaje claro en lenguaje de negocio que indique exactamente cuál restricción se infringió (sin exponer errores técnicos internos ni trazas de base de datos).
- **CB-03 (Privilegios de acceso y aislamiento por grupo):**
  - Si un usuario intenta ejecutar una acción fuera de su rol, por ejemplo, un **Profesional de prevención** intentando `asignarPresupuestoAnual()` según *RN-10*, o un **Prestador** intentando registrar una **Evaluación de riesgo** según *RN-08*, el sistema debe rechazar la operación por falta de permisos.
  - Si un **Líder en prevención** intenta consultar o gestionar una **Empresa afiliada** o **Plan de trabajo** fuera de su grupo asignado *RN-09*, el sistema debe denegar el acceso sin revelar la existencia ni los saldos de dicha empresa.
- **CB-04 (Colisión por concurrencia sobre el saldo):** Si dos **Profesionales de prevención** ejecutan `planearOrdenServicio()` simultáneamente sobre una misma **Empresa afiliada** y la suma de ambas órdenes supera el **Saldo disponible**, el sistema debe procesar la primera petición que asegure el bloqueo y rechazar la segunda indicando saldo insuficiente, evitando que el **Saldo disponible** quede negativo.
- **CB-05 (Conflicto de estado entre ejecución y cancelación):** Si un **Prestador** intenta registrar como *Ejecutada* una **Orden de servicio** que segundos antes pasó a *Cancelada* por el **Profesional de prevención** (o viceversa), el sistema debe bloquear la segunda acción informando que el estado actual de la orden ya no admite esa transición, manteniendo intactos los saldos.
- **CB-06 (Operación sobre empresa sin presupuesto asignado):** Si un **Profesional de prevención** intenta ejecutar `planearOrdenServicio()` para una **Empresa afiliada** a la cual el **Líder en prevención** aún no le ha ejecutado `asignarPresupuestoAnual()` en la vigencia actual, el sistema debe rechazar la planeación indicando que la empresa no cuenta con un **Presupuesto anual** activo.

## 5. Requerimientos no funcionales (Nivel negocio)
- **Volumen esperado:** Hasta 250 usuarios concurrentes y 7500 transacciones al día.
- **Disponibilidad:** 99.5% - horario crítico de lunes a sábado de 7:00 a.m. a 10:00 p.m.
- **Seguridad y control de accesos:** Implementar control de acceso basado en roles (**RBAC** — *Role-Based Access Control*) para restringir acciones por perfil (**Empresa afiliada / Representante SST**, **Prestador**, **Profesional de prevención**, **Líder en prevención**) y garantizar el aislamiento de datos por grupo (**RN-09**).
- **Auditoría y Trazabilidad:** Toda operación que cree o modifique un **Presupuesto anual**, una **Orden de servicio** o una **Evaluación de riesgo** debe registrar de forma inmutable el usuario responsable, el rol, la fecha/hora exacta y los valores anterior y nuevo.
- **Consistencia y Rendimiento:** 
  - Garantizar consistencia transaccional estricta en las actualizaciones de saldos ante operaciones concurrentes (cero tolerancia a **Saldo disponible** negativo).
  - Tiempo de respuesta inferior a 2 segundos en operaciones críticas (`planearOrdenServicio()`, `cancelarOrdenServicio()` y consulta de saldos) y entrega garantizada de eventos de dominio hacia los módulos externos (Planeación, Órdenes de servicio y Reportes).