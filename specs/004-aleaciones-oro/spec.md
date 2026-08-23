# Feature Specification: Calculadora de aleaciones de oro

**Feature Branch**: `004-aleaciones-oro`

**Created**: 2026-08-23

**Status**: Draft

**Input**: User description: "Calculadora de aleaciones de oro. Sustituir el placeholder del módulo «Aleaciones de ORO» por la calculadora real: el usuario introduce una cantidad en gramos de oro fino 999‰ (24K), selecciona la ley objetivo (18K/750‰, 14K/585‰, 12K/500‰ o 9K/375‰) y el color del oro (amarillo, blanco, rosa o rojo), y la pantalla calcula y muestra los gramos exactos de cada metal de liga que hay que añadir (plata fina, cobre y/o paladio, solo los que use la receta seleccionada) y el peso total de la aleación resultante. El cálculo es reactivo. La lógica de negocio está definida al completo en UI_Plantillas/Feature_Oro/ESPECIFICACION_CALCULADORA_ALEACIONES_ORO.md, que es la fuente de verdad; las recetas de taller de 18K amarillo y blanco no deben sustituirse. El diseño visual sigue el mockup UI_Plantillas/Feature_Oro/feature_oro_ejemplo.png adaptado al sistema de diseño de la app. Al seleccionar 12K se muestra una advertencia de que 500‰ no es ley oficial española. Botón «Limpiar» y botón «Guardar en favoritos» que de momento solo muestra un aviso efímero de «Próximamente». El modo inverso queda soportado por la lógica de cálculo y sus pruebas, sin exponerse en la interfaz de esta versión."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Calcular la liga de una aleación de oro (Priority: P1)

Un joyero tiene en el taller una cantidad de oro fino 999‰ y quiere fabricar oro de una ley y un color concretos. Abre el módulo de oro desde el menú, introduce los gramos de oro fino, elige la ley (18K, 14K, 12K o 9K) y el color (amarillo, blanco, rosa o rojo), y ve al instante cuántos gramos exactos de cada metal de liga debe añadir —plata fina, cobre y/o paladio, solo los que use esa receta— y el peso total de la aleación que obtendrá. Cada vez que cambia la cantidad, la ley o el color, los resultados se recalculan solos, sin pulsar ningún botón.

**Why this priority**: Es la función completa del módulo y la razón de que exista: pasar de una pantalla de andamiaje a una herramienta que resuelve un cálculo real de taller que hoy se hace a mano y con riesgo de bajar de ley.

**Independent Test**: Se abre el módulo, se introduce una cantidad y se recorren leyes y colores comprobando los resultados contra los casos numéricos del documento técnico. Entrega todo el valor por sí sola.

**Acceptance Scenarios**:

1. **Given** la pantalla del módulo de oro, **When** el joyero introduce 10 gramos y elige 18K amarillo, **Then** ve que debe añadir 2,191 g de plata fina y 1,129 g de cobre, y que obtendrá 13,320 g de oro amarillo.
2. **Given** un cálculo en pantalla, **When** el joyero cambia el color de amarillo a rojo, **Then** los resultados se recalculan al instante y solo aparece el cobre como metal de liga.
3. **Given** un cálculo en pantalla con 18K blanco, **When** el joyero observa los resultados, **Then** ve tres metales —plata fina, cobre y paladio— cada uno con sus gramos, y el total identifica el color («oro BLANCO»).
4. **Given** el campo de cantidad, **When** el joyero escribe «12,35» o «12.35», **Then** ambas formas se aceptan y producen exactamente el mismo resultado.
5. **Given** un cálculo en pantalla, **When** el joyero cambia solo la ley, **Then** los resultados se actualizan sin tocar nada más.
6. **Given** el campo de cantidad vacío, a cero o con un valor no interpretable, **When** el joyero observa la pantalla, **Then** no se muestra ningún resultado ni mensaje de fallo alarmante, y la pantalla sigue operativa.
7. **Given** una selección de 9K blanco, **When** el joyero observa los resultados, **Then** solo aparece la plata fina: los metales que la receta no usa no se muestran a cero.

---

### User Story 2 - Saber que 12K no es una ley oficial española (Priority: P2)

Un joyero selecciona 12K para un cálculo técnico. La pantalla le advierte de forma visible de que 500‰ se incluye únicamente como referencia técnica de cálculo y no es una de las leyes oficiales de oro previstas para comercialización en España, de modo que nunca contraste una pieza confiando en un dato equivocado.

**Why this priority**: Es una salvaguarda legal para el usuario. El cálculo de 12K funciona igualmente sin ella, pero mostrarla es un requisito irrenunciable del documento técnico antes de publicar.

**Independent Test**: Se selecciona 12K y se comprueba que la advertencia aparece; se selecciona cualquier otra ley y se comprueba que desaparece.

**Acceptance Scenarios**:

1. **Given** la pantalla del módulo de oro, **When** el joyero selecciona 12K, **Then** aparece una advertencia visible que indica que 500‰ es solo una referencia técnica de cálculo y no una ley oficial española.
2. **Given** la advertencia de 12K visible, **When** el joyero cambia a 18K, 14K o 9K, **Then** la advertencia desaparece.
3. **Given** cualquier estado de la pantalla, **When** el joyero lee la opción de 12K, **Then** en ningún lugar se presenta 500‰ como ley oficial española.

---

### User Story 3 - Limpiar y empezar un cálculo nuevo (Priority: P3)

Tras un cálculo, el joyero quiere empezar otro desde cero. Pulsa «Limpiar» y la pantalla vuelve a su estado inicial: campo de cantidad vacío, selección por defecto y sin resultados.

**Why this priority**: Comodidad de uso repetido en taller. Sin este botón se puede lograr lo mismo borrando el campo a mano.

**Independent Test**: Se completa un cálculo, se pulsa «Limpiar» y se comprueba que la pantalla queda como recién abierta.

**Acceptance Scenarios**:

1. **Given** un cálculo completo en pantalla, **When** el joyero pulsa «Limpiar», **Then** el campo de cantidad queda vacío, la ley y el color vuelven a la selección inicial y los resultados desaparecen.
2. **Given** la pantalla recién limpiada, **When** el joyero introduce una nueva cantidad, **Then** el cálculo funciona con normalidad.

---

### User Story 4 - Intentar guardar en favoritos (Priority: P4)

El joyero quiere guardar un cálculo que repite a menudo. Pulsa «Guardar en favoritos» y la app le informa con un aviso efímero de que esa función llegará próximamente.

**Why this priority**: El botón prepara el hueco visual y el hábito de uso para la futura feature de favoritos, pero hoy no aporta funcionalidad de cálculo.

**Independent Test**: Se pulsa el botón y se comprueba que aparece el aviso y que la pantalla no cambia de estado.

**Acceptance Scenarios**:

1. **Given** cualquier estado de la pantalla, **When** el joyero pulsa «Guardar en favoritos», **Then** aparece un aviso efímero de «Próximamente» y el cálculo en pantalla no se altera.
2. **Given** el aviso visible, **When** el joyero pulsa varias veces seguidas el botón, **Then** los avisos no se acumulan de forma que bloqueen o ensucien la pantalla.

---

### Edge Cases

- **Cantidad con formato ambiguo** («1.2,3», dos comas, solo un separador sin dígitos): se trata como valor no interpretable; no hay resultados ni cierre de la app.
- **Cantidades muy grandes** (miles de gramos): los resultados se calculan y se muestran completos sin romper la composición de la pantalla.
- **Cantidades minúsculas** (0,001 g): el cálculo conserva la precisión y muestra los resultados redondeados solo para la vista.
- **Muchos decimales introducidos**: se aceptan; el cálculo interno no pierde precisión aunque la vista redondee a 3 decimales.
- **Metales no usados por la receta**: nunca aparecen como filas a cero; solo se listan los metales que la receta emplea.
- **Teclado en pantalla desplegado**: los resultados siguen alcanzables desplazando el contenido; el teclado no los oculta de forma irrecuperable.
- **Fuente del sistema muy grande o pantalla pequeña**: ningún texto se recorta y todo el contenido sigue alcanzable mediante desplazamiento.
- **Lector de pantalla activo**: cada selector anuncia su opción elegida, el campo de cantidad anuncia su propósito y unidad, cada resultado anuncia metal y gramos, y la advertencia de 12K se anuncia al aparecer.
- **Salir del módulo y volver a entrar**: la pantalla arranca limpia, en su estado inicial; no recuerda el cálculo anterior.
- **Redondeo de vista**: lo mostrado nunca se usa para recalcular; la suma real de los metales coincide con la liga total exacta aunque las cifras redondeadas visibles no cuadren al miligramo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El módulo «Aleaciones de ORO» del menú principal MUST llevar a la calculadora real; la pantalla de andamiaje desaparece de ese destino.
- **FR-002**: La pantalla MUST ofrecer un campo para introducir la cantidad de oro fino 999‰ en gramos, identificado con su unidad, que despliegue un teclado apto para números decimales.
- **FR-003**: El campo de cantidad MUST aceptar tanto coma como punto decimal y tratarlos como equivalentes.
- **FR-004**: El sistema MUST tratar como entrada no válida el campo vacío, los valores no numéricos y los valores menores o iguales a cero; con entrada no válida MUST NOT mostrarse ningún resultado y la pantalla MUST permanecer estable.
- **FR-005**: La pantalla MUST ofrecer la selección de ley objetivo entre exactamente cuatro opciones —18K, 14K, 12K y 9K— con una sola activa en todo momento.
- **FR-006**: La pantalla MUST ofrecer la selección de color del oro entre exactamente cuatro opciones —amarillo, blanco, rosa y rojo— con una sola activa en todo momento.
- **FR-007**: Con entrada válida, el sistema MUST calcular y mostrar los gramos de cada metal de liga a añadir y el peso total de la aleación resultante, recalculando automáticamente ante cualquier cambio de cantidad, ley o color, sin acción adicional del usuario.
- **FR-008**: Los resultados MUST mostrar únicamente los metales que la receta seleccionada emplea (plata fina, cobre y/o paladio), cada uno identificado por su nombre y acompañado de su cantidad en gramos.
- **FR-009**: El total MUST identificar el color del oro resultante junto al peso final de la aleación.
- **FR-010**: Todos los cálculos MUST seguir las fórmulas, recetas y reglas del documento técnico `UI_Plantillas/Feature_Oro/ESPECIFICACION_CALCULADORA_ALEACIONES_ORO.md` (anexo de lógica de negocio de esta spec): pureza real de origen 0,999, reparto de la liga según las proporciones internas de las 16 recetas color×ley, y sin sustituir las recetas de taller de 18K amarillo y 18K blanco.
- **FR-011**: El cálculo interno MUST conservar la precisión completa, MUST NOT redondear pasos intermedios y MUST NOT reutilizar cantidades ya redondeadas para cálculos posteriores; la ley teórica resultante MUST NOT quedar por debajo de la ley objetivo por efecto de redondeos.
- **FR-012**: Los resultados MUST mostrarse redondeados a 3 decimales con coma decimal española, sin que ese redondeo altere el cálculo interno.
- **FR-013**: Mientras 12K esté seleccionado, la pantalla MUST mostrar una advertencia visible de que 500‰ se incluye únicamente como referencia técnica de cálculo y no es una ley oficial de comercialización en España; la advertencia MUST desaparecer al seleccionar otra ley y 500‰ MUST NOT presentarse nunca como ley oficial.
- **FR-014**: La pantalla MUST ofrecer un control «Limpiar» que devuelva el formulario a su estado inicial: cantidad vacía, selección por defecto y sin resultados.
- **FR-015**: La pantalla MUST ofrecer un control «Guardar en favoritos» que, en esta versión, muestre un aviso efímero de «Próximamente» sin alterar el estado del cálculo.
- **FR-016**: La lógica de cálculo MUST soportar también el modo inverso —partir del peso final de aleación deseado y obtener el oro 999‰ y la liga necesarios— verificado por pruebas, aunque la interfaz de esta versión MUST NOT exponerlo.
- **FR-017**: La pantalla MUST llegar desde el menú principal, ofrecer un control de retroceso que devuelva al joyero a la pantalla anterior y MUST NOT mostrar la barra de navegación inferior.
- **FR-018**: El contenido MUST poder desplazarse cuando no quepa completo, incluido con el teclado desplegado.
- **FR-019**: El sistema MUST registrar como telemetría la visualización de la pantalla y la realización de cálculos identificando ley y color elegidos; MUST NOT registrar la cantidad introducida.
- **FR-020**: Los elementos accionables MUST respetar el tamaño táctil mínimo del sistema de diseño; selectores, campo, resultados y advertencia MUST anunciarse correctamente a los lectores de pantalla.
- **FR-021**: Todos los textos visibles MUST poder traducirse a otro idioma sin modificar el comportamiento de la pantalla.

### Key Entities

- **Receta de liga**: combinación de color y ley que define las proporciones internas de la liga —qué parte de cada metal (plata fina, cobre, paladio) compone el añadido—, sumando esas proporciones exactamente la unidad. Hay 16, fijas, definidas en el documento técnico anexo; viajan dentro de la app, no se configuran ni se descargan. La de 500‰ lleva la marca de «solo referencia técnica».
- **Cálculo de aleación**: resultado de aplicar una receta a una cantidad de oro de partida con pureza 999‰. Comprende el oro puro contenido, la liga total a añadir, los gramos exactos de cada metal y el peso y la ley teórica finales. Existe en dos sentidos: directo (desde el oro disponible) e inverso (desde el peso final deseado, sin interfaz en esta versión).
- **Metal de liga**: cada metal que puede componer la liga —plata fina, cobre, paladio—, con nombre visible traducible e identificador estable independiente del idioma.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Los 5 casos de prueba numéricos del documento técnico (§13) producen exactamente los valores esperados en pantalla, redondeados a 3 decimales.
- **SC-002**: En las **16** combinaciones color×ley, la suma exacta de los metales añadidos coincide con la liga total, el peso final es la suma del oro de partida y la liga, y la ley teórica resultante nunca queda por debajo de la objetivo.
- **SC-003**: Un joyero completa un cálculo —introducir cantidad, elegir ley y color— con **3 interacciones** y ve los resultados **al instante**, sin botón de calcular.
- **SC-004**: Con 12K seleccionado, la advertencia de referencia técnica es visible en el **100%** de los casos; con cualquier otra ley no aparece nunca.
- **SC-005**: La pantalla renderizada es reconocible como el mockup de referencia en una comparación visual lado a lado.
- **SC-006**: Con la fuente del sistema al doble y en pantallas desde 5", ningún texto se recorta y todo el contenido es alcanzable.
- **SC-007**: Con lector de pantalla, el **100%** de los controles, resultados y avisos se anuncian con una descripción que identifica su función o su valor.
- **SC-008**: Ningún destino del menú principal de módulos lleva ya a una pantalla de andamiaje para el módulo de oro.
- **SC-009**: Introducir la misma cantidad con coma o con punto produce resultados idénticos en el **100%** de los casos.

## Assumptions

- El destino del módulo de oro ya existe en la navegación desde la feature 002 y hoy muestra una pantalla de andamiaje; esta feature sustituye su contenido, no crea un destino nuevo.
- **Fuente de verdad numérica**: el documento `UI_Plantillas/Feature_Oro/ESPECIFICACION_CALCULADORA_ALEACIONES_ORO.md` prevalece sobre cualquier otra referencia, incluido el mockup, cuyas cifras son ilustrativas (sus valores de plata y cobre están intercambiados respecto al cálculo correcto). Decisión confirmada con el autor.
- **Estado inicial**: campo de cantidad vacío, 18K y amarillo preseleccionados, sin resultados. Al volver a entrar en el módulo la pantalla arranca en ese estado inicial, sin memoria del cálculo anterior.
- **Decimales**: 3 en la vista, con coma decimal, conservando el cálculo interno la precisión completa. Decisión confirmada con el autor (el mockup muestra 2; prevalece el documento técnico).
- **Resultados mostrados**: solo filas de metales y total de aleación, como el mockup, más la advertencia de 12K. El oro puro contenido, la liga total y la ley teórica se calculan internamente pero no se muestran. Decisión confirmada con el autor.
- La pantalla no lleva barra de navegación inferior, aunque el mockup dibuje otra composición de barra superior: es una sección de módulo y sigue el patrón de navegación de la app (título y retroceso).
- La telemetría de la pantalla conserva la identidad que ya emite el andamiaje del módulo de oro, para no romper la serie histórica.
- La app sigue en un solo idioma, español, y bloqueada en orientación vertical.
- Quedan fuera de alcance y entrarán como features propias: persistencia de favoritos (el botón solo avisa de «Próximamente»), modo inverso en la interfaz, vista técnica de 6 decimales, otros orígenes de oro (916‰, 999,9‰), mezclas de aleaciones, mermas, soldaduras, costes y precios. El diseño de la lógica no debe impedir añadirlas después.
