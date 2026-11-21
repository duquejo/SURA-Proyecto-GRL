### [REQ-01] Planear orden de servicio y reservar presupuesto
- **Prioridad:** Alta

- **Actor:** Profesional de prevención

- **Historia / Necesidad:** Como **Profesional de prevención**, quiero ejecutar `planearOrdenServicio()` asignando uno o varios **Servicios de prevención** y un **Prestador** dentro del **Plan de trabajo** de una **Empresa afiliada**, para reservar el valor total en el **Saldo comprometido** y garantizar que no se planeen actividades sin disponibilidad presupuestal.

- **Reglas específicas:**
  - Aplica **RN-01 (Tope presupuestal)** y **RN-02 (Indivisibilidad de la orden)**: se valida y bloquea el 100% del valor del paquete de servicios.
  - Aplica **RN-05 (Tarifas inmutables)**: el valor comprometido queda fijado al momento de planear la orden.
  - **Idempotencia:** Cada solicitud debe incluir un identificador único de operación para evitar reservas duplicadas ante reintentos de red.
  - **Concurrencia:** La validación y actualización de saldos debe ejecutarse de forma atómica para impedir que dos reservas simultáneas dejen el **Saldo disponible** en negativo.
  - **Integración:** Al confirmarse la reserva, el sistema debe emitir un evento de dominio notificando el compromiso del presupuesto.

- **Criterios de Aceptación:**
  - **Escenario 1 (Camino feliz — Saldo suficiente):**
    - **DADO** que una **Empresa afiliada** tiene un **Presupuesto anual** de $10.000.000, un **Saldo disponible** de $6.000.000 y un **Saldo comprometido** de $4.000.000
    - **CUANDO** el **Profesional de prevención** planea una **Orden de servicio** por valor de $2.500.000 con uno o más **Servicios de prevención** y un **Prestador** asignado
    - **ENTONCES** la **Orden de servicio** queda registrada en estado *Planeada*, el **Saldo disponible** disminuye a $3.500.000, el **Saldo comprometido** aumenta a $6.500.000 y se publica el evento de reserva de presupuesto.
  - **Escenario 2 (Validación / Rechazo — Saldo insuficiente):**
    - **DADO** que una **Empresa afiliada** tiene un **Saldo disponible** de $1.500.000
    - **CUANDO** el **Profesional de prevención** intenta planear una **Orden de servicio** por valor de $2.000.000
    - **ENTONCES** el sistema bloquea la operación, muestra un mensaje claro indicando que el valor supera el **Saldo disponible** actual ($1.500.000) y mantiene intactos los saldos sin crear la orden.
  - **Escenario 3 (Control de duplicados — Idempotencia):**
    - **DADO** que una petición de `planearOrdenServicio()` con ID de operación `OP-101` ya fue procesada exitosamente
    - **CUANDO** la misma petición con ID `OP-101` vuelve a llegar al sistema por un reintento de red
    - **ENTONCES** el sistema devuelve el resultado de la orden ya creada sin descontar nuevamente el **Saldo disponible** ni duplicar la **Orden de servicio**.
  - **Escenario 4 (Concurrencia sobre el mismo presupuesto):**
    - **DADO** que una **Empresa afiliada** tiene un **Saldo disponible** de $3.000.000
    - **CUANDO** dos **Profesionales de prevención** intentan planear simultáneamente dos **Órdenes de servicio** de $2.000.000 cada una para esa misma empresa
    - **ENTONCES** el sistema procesa exitosamente la primera orden (dejando $1.000.000 de **Saldo disponible**) y rechaza la segunda por saldo insuficiente, garantizando que el saldo nunca quede en negativo.