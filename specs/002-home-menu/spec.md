# Feature Specification: Home (menú de inicio)

**Feature Branch**: `002-home-menu`

**Created**: 2026-08-23

**Status**: Draft

**Input**: User description: "Homescreen que es el menú de inicio de la app, según el mockup UI_Plantillas/Feature_home/ejemplo_homescreen.png, con las imágenes oro.png, plata.png, soldador.png y herramientas.png. Además crear las pantallas de cada sección a modo de mockup, solo con el nombre de la sección y el topbar que van a compartir todas."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Elegir a qué herramienta de cálculo entrar (Priority: P1)

Un joyero entra en la app y ve de un vistazo las cuatro familias de cálculo que ofrece: oro, plata, soldaduras y herramientas. Cada una con su imagen, su nombre y una frase que explica qué resuelve. Toca la que necesita y entra.

**Why this priority**: Es el reparto principal de la app. Sin este menú no hay forma de llegar a ninguna calculadora.

**Independent Test**: Se abre el menú y se comprueba que las cuatro entradas se muestran y que cada una lleva a su sección.

**Acceptance Scenarios**:

1. **Given** el menú principal, **When** el joyero lo observa, **Then** ve cuatro módulos: Aleaciones de ORO, Aleaciones de PLATA, Soldaduras de Oro y Plata, y Herramientas, en ese orden.
2. **Given** el menú principal, **When** el joyero toca un módulo, **Then** entra en la sección correspondiente.
3. **Given** una sección abierta, **When** el joyero vuelve atrás, **Then** regresa al menú principal.
4. **Given** una pantalla que no cabe entera, **When** el joyero desplaza la lista, **Then** puede alcanzar los cuatro módulos.

---

### User Story 2 - Moverse entre las zonas principales de la app (Priority: P2)

El joyero salta entre el menú, sus favoritos y los ajustes desde una barra siempre presente en esas tres zonas, sin tener que retroceder.

**Why this priority**: Estructura la navegación de toda la app, pero el valor de cálculo (US1) se entrega sin ella.

**Independent Test**: Se pulsa cada destino de la barra inferior y se comprueba que cambia de zona y que la barra marca cuál está activa.

**Acceptance Scenarios**:

1. **Given** cualquiera de las tres zonas principales, **When** el joyero mira abajo, **Then** ve tres destinos: Home, Favoritos y Ajustes, con el actual resaltado.
2. **Given** el menú principal, **When** el joyero toca Favoritos y luego Ajustes, **Then** llega a cada zona sin acumular historial de navegación.
3. **Given** una sección de módulo abierta, **When** el joyero mira abajo, **Then** la barra no está: la sección ocupa la pantalla completa.

---

### User Story 3 - Saber siempre dónde está (Priority: P2)

El joyero reconoce la app por su logo en las zonas principales, y dentro de una sección ve su nombre y una forma clara de volver.

**Why this priority**: Evita la sensación de estar atrapado, que es el fallo de navegación más caro en una app de taller donde se consulta con prisa.

**Independent Test**: Se recorre cada pantalla y se comprueba qué muestra la barra superior.

**Acceptance Scenarios**:

1. **Given** una zona principal, **When** el joyero mira arriba, **Then** ve el logo de la app centrado.
2. **Given** una sección de módulo, **When** el joyero mira arriba, **Then** ve el nombre de la sección y un control para volver.
3. **Given** cualquier pantalla, **When** el joyero toca el control de información, **Then** llega a la pantalla de información de la app.

---

### User Story 4 - Recorrer la app completa antes de que existan las calculadoras (Priority: P3)

Toda ruta de navegación llega a una pantalla real que se identifica por su nombre, aunque su contenido aún no exista.

**Why this priority**: Es andamiaje de desarrollo, no valor para el usuario final. Permite validar la navegación entera antes de construir ninguna calculadora.

**Independent Test**: Se recorren los siete destinos y ninguno queda muerto ni lleva a una pantalla en blanco sin identificar.

**Acceptance Scenarios**:

1. **Given** cualquier destino aún sin desarrollar, **When** el joyero llega a él, **Then** la pantalla muestra su nombre y la barra superior compartida.

---

### Edge Cases

- **Pantallas pequeñas o fuente del sistema muy grande**: los cuatro módulos deben seguir siendo alcanzables mediante desplazamiento.
- **Descripciones de distinta longitud**: las tarjetas no deben recortar texto ni descuadrarse entre sí.
- **La imagen de herramientas es apaisada** mientras las otras tres son cuadradas: debe mostrarse completa, sin recortar herramientas.
- **Doble pulsación rápida sobre un módulo**: no debe abrir la sección dos veces.
- **Repetir la pestaña activa** en la barra inferior: no debe apilar historial ni recargar la zona.
- **Volver atrás desde el menú principal**: cierra la app, no reabre la portada.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El menú MUST presentar cuatro módulos en este orden: Aleaciones de ORO, Aleaciones de PLATA, Soldaduras de Oro y Plata, Herramientas.
- **FR-002**: Cada módulo MUST mostrar su imagen, su título, una descripción breve de lo que resuelve y un indicador visual de que se puede entrar.
- **FR-003**: Cada módulo MUST distinguirse por un color de acento propio del metal o la utilidad que representa.
- **FR-004**: Al activar un módulo, el sistema MUST navegar a su sección.
- **FR-005**: La lista de módulos MUST poder desplazarse cuando no quepa completa en pantalla.
- **FR-006**: La aplicación MUST ofrecer una barra de navegación inferior con tres destinos: Home, Favoritos y Ajustes, indicando cuál está activo.
- **FR-007**: La barra inferior MUST mostrarse únicamente en Home, Favoritos y Ajustes, y no dentro de las secciones de módulo.
- **FR-008**: Cambiar de destino en la barra inferior MUST NOT acumular historial de navegación.
- **FR-009**: La barra superior MUST mostrar el logo de la app en las zonas principales, y el nombre de la sección junto a un control de retroceso dentro de una sección.
- **FR-010**: La barra superior MUST ofrecer un acceso a la información de la app en todas las pantallas que la usen.
- **FR-011**: Todo destino de navegación MUST llegar a una pantalla que se identifique por su nombre, aunque su contenido no esté desarrollado.
- **FR-012**: El sistema MUST registrar como telemetría la visualización de cada pantalla y el módulo elegido en el menú.
- **FR-013**: Los elementos accionables MUST respetar el tamaño táctil mínimo que fija el sistema de diseño.
- **FR-014**: Todos los textos visibles MUST poder traducirse a otro idioma sin modificar el comportamiento de las pantallas.

### Key Entities

- **Módulo de cálculo**: una de las cuatro familias de herramientas que ofrece la app. Tiene identidad propia (nombre, descripción, imagen y color de acento) y un destino asociado. No se persiste: la lista es fija en esta feature.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Desde el menú, el joyero alcanza cualquiera de los cuatro módulos con **una sola pulsación**.
- **SC-002**: Los cuatro módulos son alcanzables en pantallas desde 5" hasta 7", con desplazamiento si hace falta.
- **SC-003**: Ninguna imagen de módulo aparece recortada de forma que impida reconocer lo que representa.
- **SC-004**: El 100% de los destinos de navegación llega a una pantalla identificada; ninguno queda muerto.
- **SC-005**: Desde cualquier sección, el joyero vuelve al menú con **una sola acción**.
- **SC-006**: Saltar diez veces entre las tres zonas principales deja el historial en el mismo estado que un único salto.
- **SC-007**: El menú renderizado es reconocible como el mockup de referencia en una comparación visual lado a lado.

## Assumptions

- Los cuatro módulos son los del mockup. La design spec menciona un quinto, "Precios del Oro y la Plata", que el mockup no incluye; si debe existir, entra como feature propia.
- La barra inferior tiene tres destinos, los del mockup, y no los hasta seis que sugiere la design spec.
- Favoritos, Ajustes e Información existen como pantallas identificadas pero sin contenido: su funcionalidad se especifica en features posteriores.
- La lista de módulos es fija y está definida en la propia aplicación: no se configura ni se descarga.
- Volver atrás desde el menú principal cierra la app, coherente con la decisión de la feature 001 de sacar la portada del historial.
- La portada de la feature 001 no se modifica: sigue sin barras superior ni inferior.
