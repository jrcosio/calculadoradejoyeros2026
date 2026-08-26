# Specification Quality Checklist: Favoritos — guardar y reabrir cálculos

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: 2026-08-26
**Feature**: [spec.md](../spec.md)

## Content Quality

- [x] No implementation details (languages, frameworks, APIs)
- [x] Focused on user value and business needs
- [x] Written for non-technical stakeholders
- [x] All mandatory sections completed

## Requirement Completeness

- [x] No [NEEDS CLARIFICATION] markers remain
- [x] Requirements are testable and unambiguous
- [x] Success criteria are measurable
- [x] Success criteria are technology-agnostic (no implementation details)
- [x] All acceptance scenarios are defined
- [x] Edge cases are identified
- [x] Scope is clearly bounded
- [x] Dependencies and assumptions identified

## Feature Readiness

- [x] All functional requirements have clear acceptance criteria
- [x] User scenarios cover primary flows
- [x] Feature meets measurable outcomes defined in Success Criteria
- [x] No implementation details leak into specification

## Notes

Los 16 puntos pasan. Detalle de la validación:

- **Sin `[NEEDS CLARIFICATION]`**: el encargo llegaba ya decidido en los cuatro puntos que
  habrían necesitado pregunta (almacén persistente, favorito editable sin alterar el guardado,
  título automático y aviso en vez de duplicado), y las decisiones quedan en `Assumptions` y en
  FR-002/FR-006/FR-008/FR-022.
- **Sin detalles de implementación**: la spec no nombra ni una librería, ni un tipo, ni un
  fichero. Habla de «pestaña Favoritos», «botón Guardar en favoritos» y «estrella» porque son
  elementos que ya existen en la app y que el joyero ve, no piezas técnicas. El almacenamiento
  aparece sólo como comportamiento observable (FR-032 a FR-035).
- **SC-009 reformulado** en la primera pasada de validación: decía «con la misma inmediatez que
  el resto de zonas principales», que no es medible. Ahora fija 50 favoritos y menos de un
  segundo.
- **FR-036 no tiene escenario de aceptación** a propósito: es una prohibición sobre telemetría,
  verificable revisando qué se emite, no algo que el joyero pueda observar en pantalla.
- **Redondeo por calculadora**: la spec lo declara como asunción y lo cierra con SC-006
  («dígito a dígito»), que es lo que impide que una tarjeta contradiga a su calculadora. Es el
  punto de la feature con más riesgo de fallo silencioso, y por eso tiene criterio de éxito
  propio en vez de quedarse en una nota.
- **Alcance acotado por escrito**: la sección «Fuera de alcance en esta feature» enumera seis
  cosas con su motivo, incluida la de precio de metales, que el encargo excluía expresamente.
