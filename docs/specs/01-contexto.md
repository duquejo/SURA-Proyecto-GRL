# Contexto del Proyecto: Plataforma de control de presupuesto de prevención

## 1. Visión y Problema
- **Problema central:** Cada empresa afiliada tiene un presupuesto anual de prevención. Con este presupuesto se pagan los servicios que recibe durante el año. Hoy el presupuesto se controla en hojas de cálculo y eso genera tres problemas: Se planean servicios sin saldo suficiente, nadie sabe con certeza cuánto queda de cada empresa y cuando un servicio se cancela, el dinero no vuelve a quedar disponible. 
- **Propuesta de valor:** Construir una primera versión del control de presupuesto de Prevención, permitiendo a cada empresa afiliada recibir un plan de trabajo con servicios de prevención. Capacitaciones, inspecciones de seguridad, mediciones en los puestos de trabajo, asesorías y acompañamiento en sus programas de seguridad y salud en el trabajo.
- **Estado actual (As-Is):** Hoy el presupuesto se controla en hojas de cálculo, no hay control de los servicios, es inminente la necesidad de tener un sistema que permita gestionar amigablemente los servicios.

## 2. Actores y Perfiles de Usuario
| Rol | Descripción | Objetivo principal en el sistema | Frecuencia de uso |
| :--- | :--- | :--- | :--- |
| **Empresa afiliada / Representante SST** | Recibe los servicios, es gestionado por su respectivo representante de seguridad y salud en el trabajo |  Consulta su plan y solicita servicios | Ocasional |
| **Prestador** | Empresa o consultor externo contratado por la ARL | Ejecuta las "Órdenes de servicio" en campo bajo tarifas pactadas | Diario |
| **Profesional de prevención** | Trabaja en la aseguradora | Arma el plan de cada empresa y planea los servicios | Diario |
| **Líder en prevención** | Supervisor de un grupo de "Profesionales de prevención" | Asigna y supervisa el presupuesto anual de las "Empresas afiliadas" de su grupo, así como a los "Profesionales de prevención" a cargo. | Diario |

## 3. Glosario de Dominio (Lenguaje Ubicuo)
| Término Oficial | Definición exacta de negocio | Qué *no* es | Nombre en Código / BD |
| :--- | :--- | :--- | :--- |
| **Empresa afiliada** | Organización cliente de la ARL dueña del presupuesto anual. | Evitar "Empresa cliente". No es el usuario "Representante SST" (la persona que entra al sistema). | `EmpresaAfiliada` |
| **Servicio de prevención** | Actividad estándar de SST (capacitación, inspección, medición, asesoría o acompañamiento) que compone una "Orden de servicio". | No tiene estado ni presupuesto independiente por fuera de su "Orden de servicio". | `ServicioPrevencion` |
| **Orden de servicio** | Paquete formal y valorizado que agrupa uno o varios "Servicios de prevención" programados para una "Empresa afiliada" y asignados a un "Prestador". Se gestiona como una unidad indivisible. | No admite ejecuciones ni cancelaciones parciales; es la unidad que bloquea el "Saldo comprometido". | `OrdenServicio` |
| **Plan de trabajo** | Conjunto de "Órdenes de servicio" proyectadas para una "Empresa afiliada" dentro de un año. | No es un contrato ni una factura; es la hoja de ruta anual. | `PlanTrabajo` |
| **Presupuesto anual** | Monto total asignado a una "Empresa Afiliada" para financiar sus servicios durante el año. | No es saldo acumulable de años anteriores. | `PresupuestoAnual` |
| **Saldo disponible** | Dinero libre para planear nuevos servicios (Presupuesto Anual - Saldo Comprometido - Saldo Ejecutado). | No incluye el dinero de servicios ya planeados. | `SaldoDisponible` |
| **Saldo comprometido** | Dinero reservado en el momento en que se planea un servicio, impidiendo gastarlo dos veces. | Aún no se ha pagado al prestador, pero ya no está disponible. | `SaldoComprometido` |
| **Saldo ejecutado** | Dinero descontado de forma definitiva cuando el prestador completa el servicio en campo. | No es reembolsable ni vuelve al "Saldo Disponible". | `SaldoEjecutado` |
| **Evaluación de riesgo** | Registro realizado por el "Profesional de prevención" tras ejecutarse una "Orden de servicio", donde verifica si el nivel de riesgo de la "Empresa afiliada" disminuyó según lo esperado. |No la diligencia el "Prestador" ni califica su desempeño; mide el impacto preventivo en la empresa. | `EvaluacionRiesgo` |
| **Planear orden de servicio** | Acción del "Profesional de prevención" de crear, valorar y agendar una "Orden de servicio" dentro del "Plan de trabajo". | No es la simple solicitud del cliente; bloquea el valor total del paquete pasando dinero de "Saldo disponible" a "Saldo comprometido". | `planearOrdenServicio()` |
| **Cancelar orden de servicio** | Anulación de una "Orden de servicio" planeada que *aún no* se ha ejecutado. | Libera en su totalidad el "Saldo comprometido" y lo devuelve al "Saldo disponible". | `cancelarOrdenServicio()` |
| **Reajustar plan de trabajo** | Acción de devolver el "Plan de trabajo" a etapa de planeación cuando una "Evaluación de riesgo" determina que el riesgo no bajó, habilitando la creación de nuevas "Órdenes de servicio". | No revierte el "Saldo ejecutado" de las órdenes ya realizadas. | `reajustarPlanTrabajo()` |
| **Asignar presupuesto anual** | Acción del "Líder en prevención" de establecer o ajustar el monto del "Presupuesto anual" de una "Empresa afiliada" de su grupo para una vigencia. | No es una transferencia bancaria ni una factura; define el techo financiero del Plan de trabajo. | `asignarPresupuestoAnual()` |

## 4. Reglas de Negocio Transversales
- **RN-01 (Tope presupuestal):** El sistema debe bloquear y lanzar un error si un "Profesional de prevención" intenta "Planear orden de servicio" cuyo valor supere el "Saldo disponible" de la "Empresa afiliada".
- **RN-02 (Indivisibilidad de la orden):** Dado que la "Orden de servicio" es un paquete indivisible, no se permiten cancelaciones ni ejecuciones parciales. Pasa completa a estado *Ejecutada* o *Cancelada*, afectando el 100% de su valor en el presupuesto.
- **RN-03 (Cierre de vigencia):** Al 31 de diciembre de cada año, el "Saldo disponible" no utilizado caduca. No es acumulable ni transferible al "Presupuesto anual" del año siguiente.
- **RN-04 (Órdenes de servicio en transición anual):** Toda "Orden de servicio" que llegue al 31 de diciembre en estado *Planeada* (no ejecutada) será cancelada automáticamente por el sistema, liberando el "Saldo comprometido".
- **RN-05 (Tarifas inmutables en ejecución):** El valor descontado en el "Saldo ejecutado" debe ser exactamente el mismo valor que se reservó en el "Saldo comprometido" al momento de planear la orden. No se permiten reajustes de precio posteriores a la planeación.
- **RN-06 (No reembolso tras ejecución):** Al registrar el resultado de una "Orden de servicio" ejecutada, su valor pasa de forma definitiva de "Saldo comprometido" a "Saldo ejecutado". Si la "Evaluación de riesgo" indica que el riesgo no bajó y se retrocede a reajustar el "Plan de trabajo", el dinero de esa orden no vuelve al "Saldo disponible".
- **RN-07 (Retroceso por efectividad en riesgo):** Si tras ejecutar una "Orden de servicio" se registra que el riesgo de la "Empresa afiliada" no disminuyó, el sistema debe permitir regresar el "Plan de trabajo" al paso de planeación para que el "Profesional de prevención" agregue nuevas "Órdenes de servicio" (usando el "Saldo disponible" restante o cancelando otras "Órdenes de servicio" futuras que aún estén en estado Planeada).
- **RN-08 (Separación entre ejecución y evaluación):** El "Prestador" únicamente registra la finalización operativa de la "Orden de servicio" en campo. Posteriormente, el "Profesional de prevención" es el único rol autorizado para registrar la "Evaluación de riesgo" y decidir si el ciclo cierra o si se debe "Reajustar plan de trabajo".
- **RN-09 (Aislamiento por grupo de prevención):** Un "Líder en prevención" únicamente puede visualizar y gestionar a los "Profesionales de prevención" pertenecientes a su grupo asignado, así como las "Empresas afiliadas" y "Planes de trabajo" vinculados a dicho equipo. No tiene visibilidad sobre grupos de otros líderes.
- **RN-10 (Administración del presupuesto anual):** El "Líder en prevención" es el único rol autorizado para ejecutar "Asignar presupuesto anual" sobre las "Empresas afiliadas" de su grupo.

## 5. Restricciones y Supuestos
- **Concurrencia:** Dos "Profesionales de prevención" pueden reservar al mismo tiempo sobre la misma empresa. El saldo *nunca* puede quedar negativo.
- **Duplicados:** Si la misma solicitud de reserva llega dos veces (por ejemplo, por un reintento de red), no se debe reservar dos veces.
- **Integración:** Otras áreas de la plataforma (Planeación, Órdenes de servicio, Reportes) necesitan *enterarse* cuando se reserva, consume o libera presupuesto.
- **Datos:** Usa datos ficticios.