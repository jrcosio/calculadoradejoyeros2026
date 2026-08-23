# Feature Specification: Pantalla de inicio

**Feature Branch**: `001-pantalla-inicio`

**Created**: 2026-08-23

**Status**: Draft

**Input**: User description: "Pantalla de inicio (portada) de la app. Es la primera pantalla que se ve siempre al abrir la app. Muestra sobre un fondo de taller de joyería: el logo de la app, el título Calculadora de Joyeros, un ornamento decorativo, el subtítulo Precisión y cálculo para tu taller, un botón Comenzar que lleva a la pantalla home, y abajo del todo el crédito Desarrollado por José Ramón Blanco. Incluye además sustituir el tema visual de plantilla por el sistema de diseño dark luxury definido en la design spec."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Recibir al joyero con una portada de marca (Priority: P1)

Un joyero abre la app en su taller. Antes de llegar a las herramientas de cálculo ve una portada que identifica el producto: el logo, el nombre y una frase que dice para qué sirve. Desde ahí entra a la app con un único gesto.

**Why this priority**: Es la primera impresión del producto y la única pantalla que ve el 100% de los usuarios en el 100% de las sesiones. Sin ella no hay punto de entrada.

**Independent Test**: Se abre la app y se comprueba que la portada aparece con logo, título, subtítulo y botón, sin depender de ninguna otra pantalla.

**Acceptance Scenarios**:

1. **Given** la app cerrada, **When** el usuario la abre, **Then** la portada es la primera pantalla que se muestra.
2. **Given** el usuario ya ha usado la app antes, **When** vuelve a abrirla, **Then** la portada se muestra igualmente: no se salta nunca.
3. **Given** la portada visible, **When** el usuario pulsa "Comenzar", **Then** se navega a la pantalla principal.
4. **Given** el usuario ha entrado a la pantalla principal, **When** pulsa atrás, **Then** la app se cierra en lugar de volver a la portada.

---

### User Story 2 - Reconocer la identidad visual del producto (Priority: P2)

El joyero percibe la app como una herramienta profesional de taller, no como una calculadora genérica. La portada transmite precisión, oficio y producto cuidado.

**Why this priority**: La diferenciación es el motivo declarado del rediseño, pero el valor funcional (US1) puede entregarse sin ella.

**Independent Test**: Se compara la pantalla renderizada con el mockup de referencia y se verifica que respeta la paleta, la tipografía y el espaciado del sistema de diseño.

**Acceptance Scenarios**:

1. **Given** la portada visible, **When** el usuario la observa, **Then** el fondo, los dorados y la tipografía coinciden con el sistema de diseño definido.
2. **Given** un dispositivo con color dinámico del sistema activado, **When** se abre la app, **Then** la identidad dorada se mantiene intacta y no adopta los colores del fondo de pantalla del usuario.
3. **Given** el arranque de la app, **When** el sistema muestra su pantalla de carga previa, **Then** el fondo es oscuro y continúa visualmente con la portada, sin destello claro.

---

### User Story 3 - Atribuir la autoría (Priority: P3)

El autor de la app aparece acreditado en la portada.

**Why this priority**: Requisito explícito del propietario del producto, sin impacto en la funcionalidad.

**Independent Test**: Se abre la portada y se lee el crédito en la parte inferior.

**Acceptance Scenarios**:

1. **Given** la portada visible, **When** el usuario mira la parte inferior, **Then** lee "Desarrollado por José Ramón Blanco".
2. **Given** un dispositivo con barra de navegación por gestos o por botones, **When** se muestra la portada, **Then** el crédito queda por encima de la barra del sistema y es legible.

---

### Edge Cases

- **Pantallas de proporción distinta a la del fondo**: el fondo tiene una relación de aspecto más ancha que la de un móvil actual. Al ajustarlo a pantalla completa se recorta por los laterales; el contenido central debe permanecer siempre visible y centrado.
- **Pantallas muy pequeñas o con fuente del sistema muy grande**: el bloque central no debe solaparse con el crédito inferior ni salirse de pantalla.
- **Texto con tildes y eñes**: "Precisión", "cálculo" y "José Ramón" deben renderizarse correctamente con la tipografía elegida.
- **Doble pulsación rápida en "Comenzar"**: no debe producir dos navegaciones ni dejar la portada en el historial.
- **Rotación o cambio de configuración**: la portada debe recomponerse sin perder su estado ni duplicar el registro de telemetría.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: La aplicación MUST mostrar la portada como primera pantalla en cada arranque, sin condiciones ni excepciones.
- **FR-002**: La portada MUST mostrar el logo del producto, el título "Calculadora de Joyeros", un ornamento separador, el subtítulo "Precisión y cálculo para tu taller" y un botón de acción "Comenzar".
- **FR-003**: La portada MUST mostrar el texto "Desarrollado por José Ramón Blanco" en su parte inferior, con jerarquía visual secundaria respecto al contenido principal.
- **FR-004**: Al activar "Comenzar", el sistema MUST navegar a la pantalla principal.
- **FR-005**: Tras navegar a la pantalla principal, el sistema MUST retirar la portada del historial de navegación, de modo que el gesto de retroceso salga de la aplicación.
- **FR-006**: La aplicación MUST aplicar el sistema de diseño (paleta, tipografía, radios y espaciados) definido en la design spec, sustituyendo el tema por defecto de la plantilla.
- **FR-007**: La aplicación MUST mantener su identidad cromática con independencia de las preferencias de color dinámico del sistema operativo.
- **FR-008**: La pantalla de carga que muestra el sistema antes de la portada MUST usar un fondo oscuro coherente con ella.
- **FR-009**: Todos los textos visibles MUST poder traducirse a otro idioma sin modificar el comportamiento de la pantalla.
- **FR-010**: El nombre visible de la aplicación en el lanzador MUST ser "Calculadora de Joyeros".
- **FR-011**: El sistema MUST registrar la visualización de la portada y la activación de "Comenzar" como eventos de telemetría.
- **FR-012**: El botón "Comenzar" MUST respetar el tamaño táctil mínimo que fija el sistema de diseño para elementos accionables.

### Key Entities

No aplica: la feature no introduce ni persiste datos de dominio.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de los arranques de la aplicación muestran la portada como primera pantalla.
- **SC-002**: Un usuario puede pasar de abrir la app a la pantalla principal con **una sola pulsación**.
- **SC-003**: La portada queda completamente visible y utilizable sin desplazamiento en pantallas desde 5" hasta 7".
- **SC-004**: Ningún texto de la portada aparece con caracteres ausentes o sustituidos, incluidos los que llevan tilde y eñe.
- **SC-005**: Entre el arranque y la portada no se percibe ningún destello de color claro.
- **SC-006**: La portada renderizada es reconocible como el mockup de referencia en una comparación visual lado a lado.
- **SC-007**: El retroceso desde la pantalla principal cierra la aplicación en el 100% de los casos.

## Assumptions

- La portada es una pantalla de bienvenida con acción explícita del usuario, no una pantalla de carga automática: permanece visible hasta que se pulsa "Comenzar".
- No es un onboarding: no se recuerda si el usuario ya la ha visto, porque el propietario pidió explícitamente que aparezca siempre.
- La pantalla principal a la que navega el botón se desarrolla en la feature siguiente; en esta basta con que el destino exista y sea alcanzable.
- El retroceso desde la pantalla principal cierra la app. Es la convención de las pantallas de bienvenida y evita bucles de navegación.
- Los assets de fondo y logo entregados por el propietario son definitivos y no requieren rediseño.
- Se construyen los fundamentos del sistema de diseño (paleta, tipografía, radios, espaciados) pero no los componentes reutilizables (tarjetas de módulo, navegación inferior, selectores), que corresponden a la feature de la pantalla principal y deben especificarse allí.
- La aplicación es de tema oscuro fijo: no se contempla una variante clara.
