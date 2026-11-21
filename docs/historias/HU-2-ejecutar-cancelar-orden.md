### [REQ-02] Actualizar presupuesto por ejecución o cancelación de orden de servicio
- **Prioridad:** Alta

- **Actor:** Prestador / Profesional de prevención

- **Historia / Necesidad:** Como **Profesional de prevención**, quiero que el presupuesto de la **Empresa afiliada** se actualice automáticamente cuando el **Prestador** ejecute en campo una **Orden de servicio** o cuando yo ejecute `cancelarOrdenServicio()`, para mantener reflejados en tiempo real el **Saldo ejecutado**, el **Saldo comprometido** y el **Saldo disponible**.

- **Reglas específicas:**
  - Aplica **RN-02 (Indivisibilidad de la orden)**: el 100% del valor de la **Orden de servicio** pasa a estado *Ejecutada* o *Cancelada*, sin movimientos parciales.
  - Aplica **RN-05 (Tarifas inmutables en ejecución)**: el monto que pasa a **Saldo ejecutado** es exactamente el mismo valor que fue reservado en el **Saldo comprometido** al planear la orden.
  - Aplica **RN-06 (No reembolso tras ejecución)**: una vez la **Orden de servicio** pasa a *Ejecutada*, el dinero queda consumido de forma definitiva y no puede cancelarse ni volver al **Saldo disponible**.
  - Aplica **RN-08 (Separación entre ejecución y evaluación)**: el **Prestador** únicamente registra la finalización operativa de la orden en campo; únicamente el **Profesional de prevención** puede ejecutar `cancelarOrdenServicio()` mientras la orden esté en estado *Planeada*.
  - **Idempotencia:** Solicitudes repetidas de ejecución o cancelación sobre una misma **Orden de servicio** no deben duplicar los movimientos financieros.
  - **Integración:** Al completarse la ejecución o cancelación, el sistema debe emitir un evento de dominio notificando el consumo o la liberación del presupuesto.

- **Criterios de Aceptación:**
  - **Escenario 1 (Camino feliz — Ejecución de la orden en campo):**
    - **DADO** que una **Empresa afiliada** tiene un **Saldo disponible** de $3.500.000, un **Saldo comprometido** de $6.500.000 y un **Saldo ejecutado** de $1.000.000
    - **Y** existe una **Orden de servicio** en estado *Planeada* por valor de $2.500.000 asignada a un **Prestador**
    - **CUANDO** el **Prestador** registra la finalización operativa de la **Orden de servicio**
    - **ENTONCES** la **Orden de servicio** pasa a estado *Ejecutada*, el **Saldo comprometido** disminuye a $4.000.000, el **Saldo ejecutado** aumenta a $3.500.000, el **Saldo disponible** permanece en $3.500.000 y se publica el evento de consumo de presupuesto.
  - **Escenario 2 (Camino feliz — Cancelación de orden planeada):**
    - **DADO** que una **Empresa afiliada** tiene un **Saldo disponible** de $3.500.000 y un **Saldo comprometido** de $6.500.000
    - **Y** existe una **Orden de servicio** en estado *Planeada* por valor de $2.500.000
    - **CUANDO** el **Profesional de prevención** ejecuta `cancelarOrdenServicio()` sobre dicha orden
    - **ENTONCES** la **Orden de servicio** pasa a estado *Cancelada*, el **Saldo comprometido** disminuye a $4.000.000, el **Saldo disponible** aumenta a $6.000.000 y se publica el evento de liberación de presupuesto.
  - **Escenario 3 (Validación / Rechazo — Cancelar una orden ya ejecutada):**
    - **DADO** que una **Orden de servicio** ya se encuentra en estado *Ejecutada*
    - **CUANDO** el **Profesional de prevención** intenta ejecutar `cancelarOrdenServicio()` sobre esa orden
    - **ENTONCES** el sistema bloquea la operación, muestra un mensaje claro indicando que una **Orden de servicio** ejecutada no puede cancelarse ni reembolsarse (**RN-06**) y mantiene intactos los saldos.
  - **Escenario 4 (Control de duplicados — Idempotencia en cambio de estado):**
    - **DADO** que una **Orden de servicio** por $2.500.000 ya fue marcada como *Ejecutada* (o *Cancelada*)
    - **CUANDO** vuelve a llegar la misma petición de ejecución (o cancelación) por un reintento de red
    - **ENTONCES** el sistema ignora el reprocesamiento financiero, mantiene el estado actual de la orden y no vuelve a sumar ni restar los $2.500.000 en los saldos.