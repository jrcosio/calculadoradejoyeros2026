# Specification Quality Checklist: Herramientas — precio de metales y peso de chapas

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: 2026-08-25
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

- Las cuatro decisiones que cambiaban el alcance (una sola feature, consulta directa al
  proveedor como prototipo, precio medio como cifra principal, dos decimales en la chapa)
  se resolvieron con el autor antes de redactar la spec y quedan en Assumptions; no quedan
  marcadores de clarificación.
- Los mockups de chapas llevan cifras incoherentes con la fórmula (3,13 g y densidad
  15,55): se descartan a favor del documento técnico (FR-020), mismo criterio que la base
  de soldaduras en la 006.
- El contrato del proveedor se ha comprobado sobre la respuesta real publicada en su web;
  el nombre del parámetro y la unidad de cada metal se confirman con la credencial real al
  empezar la implementación (Assumptions).
- Validación: los 16 puntos pasan. Ningún nombre de clase, librería ni tecnología en la
  spec; «hora local», «caché» y «onza troy» son conceptos de negocio.
