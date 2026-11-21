### [REQ-03] Consultar estado presupuestal por empresa afiliada
- **Prioridad:** Alta

- **Actor:** Líder en prevención

- **Historia / Necesidad:** Como **Líder en prevención**, quiero consultar el desglose del presupuesto de cada **Empresa afiliada** vinculada a mi grupo (mostrando **Presupuesto anual**, **Saldo comprometido**, **Saldo ejecutado** y **Saldo disponible**), para supervisar en tiempo real la ejecución financiera de mi equipo sin depender de hojas de cálculo.

- **Reglas específicas:**
  - Aplica **RN-09 (Aislamiento por grupo de prevención)**: el **Líder en prevención** únicamente puede visualizar las **Empresas afiliadas** gestionadas por los **Profesionales de prevención** de su grupo asignado, sin visibilidad sobre grupos de otros líderes.
  - **Consistencia matemática del saldo:** Para cada **Empresa afiliada** en la vigencia en curso, los valores mostrados deben cumplir estrictamente la fórmula: `[Saldo disponible] = [Presupuesto anual] - [Saldo comprometido] - [Saldo ejecutado]`.
  - **Visibilidad por vigencia (RN-03):** La consulta refleja por defecto los saldos correspondientes al año fiscal vigente.

- **Criterios de Aceptación:**
  - **Escenario 1 (Camino feliz — Consulta de saldos de empresas del grupo):**
    - **DADO** que el **Líder en prevención** supervisa el "Grupo Norte", al cual pertenece la **Empresa afiliada** "Textiles S.A."
    - **Y** "Textiles S.A." tiene un **Presupuesto anual** de $20.000.000, un **Saldo comprometido** de $7.000.000 y un **Saldo ejecutado** de $5.000.000
    - **CUANDO** el **Líder en prevención** accede a la vista de presupuestos de sus empresas
    - **ENTONCES** el sistema muestra para "Textiles S.A." el desglose exacto: **Presupuesto anual** ($20.000.000), **Saldo comprometido** ($7.000.000), **Saldo ejecutado** ($5.000.000) y **Saldo disponible** ($8.000.000).
  - **Escenario 2 (Control de acceso — aislamiento entre grupos de prevención):**
    - **DADO** que la **Empresa afiliada** "Minera del Sur" pertenece al "Grupo Sur", asignado a otro **Líder en prevención**
    - **CUANDO** el **Líder en prevención** del "Grupo Norte" consulta su listado de empresas o intenta consultar directamente el detalle presupuestal de "Minera del Sur"
    - **ENTONCES** el sistema excluye a "Minera del Sur" de sus resultados y rechaza el acceso directo a sus saldos (**RN-09**).
  - **Escenario 3 (Caso borde — Empresa afiliada sin presupuesto asignado aún):**
    - **DADO** que una **Empresa afiliada** del grupo del **Líder en prevención** aún no tiene ejecutada la acción `asignarPresupuestoAnual()` para el año vigente
    - **CUANDO** el **Líder en prevención** consulta el saldo de dicha empresa
    - **ENTONCES** el sistema la lista mostrando $0 en **Presupuesto anual**, **Saldo comprometido**, **Saldo ejecutado** y **Saldo disponible**, indicando que está pendiente de asignación.