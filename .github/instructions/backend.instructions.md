---
applyTo: "1000-arl-manager_ms/**"
---

# Reglas Backend: Spring WebFlux & Clean Architecture

## Aislamiento Hexagonal
- **`domain/model`:** Java 21 puro + Project Reactor (`Mono`/`Flux`). Prohibido importar Spring (`@Service`, `@Component`, `@Transactional`), R2DBC, Jackson o seguridad.
- **`domain/usecase`:** Orquestación de reglas de negocio recibiendo objetos puros (`ContextoUsuario`). Prohibido usar `ReactiveSecurityContextHolder` o anotaciones de infraestructura.
- **`infrastructure`:** Adaptadores R2DBC, handlers REST, seguridad JWT y mensajería (`Transactional Outbox` + `Reactive Commons`).

## Programación Reactiva y Consistencia Financiera
1. **No bloqueante:** Prohibido usar `.block()`, `.blockFirst()` o `.blockLast()` en código productivo.
2. **Concurrencia (`CB-04`):** Toda reserva de saldo (`HU-1`) debe abrir un `TransactionalOperator` y ejecutar `SELECT ... FOR UPDATE` sobre `presupuesto_anual`.
3. **Idempotencia y Errores:** Toda mutación (`POST`/`PATCH`) exige `X-Idempotency-Key` (`CB-01`). Toda excepción de negocio responde bajo RFC 7807 incluyendo `codigoRegla` (`CB-02`).
4. **Aislamiento RBAC (`RN-09`):** En `HU-2` validar `orden.prestadorId == contexto.usuarioId`; en `HU-3` filtrar obligatoriamente en SQL por `grupo_prevencion_id`.

## Pruebas y Comandos
- **Unitarias (`domain`):** JUnit 5 + Mockito (`mockito-junit-jupiter`, `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`, `when` y `verify`) y `StepVerifier` para flujos reactivos. Sin contexto Spring ni Docker; simula puertos y gateways con mocks en lugar de stubs manuales repetitivos.
- **Integración (`infrastructure`):** `@SpringBootTest` + **Testcontainers** (`postgres:16-alpine` y `rabbitmq:3.13-alpine`) con `@ServiceConnection`. Prohibido usar H2.
- **Estándar:** Patrón AAA (`// Arrange`, `// Act`, `// Assert`) y `TestDataBuilder` en `src/test/java/.../databuilder/`.
- **Ejecución focalizada:** `./gradlew -p 1000-arl-manager_ms test --tests "*NombreTest" -q`
- **Suite completa:** `./gradlew -p 1000-arl-manager_ms test -q`