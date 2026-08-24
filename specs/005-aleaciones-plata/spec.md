# Feature Specification: Calculadora de aleaciones de plata

**Feature Branch**: `005-aleaciones-plata`

**Created**: 2026-08-24

**Status**: Draft

**Input**: User description: "Calculadora de aleaciones de plata. El joyero introduce una cantidad de plata fina de 999 milésimas en gramos, elige la ley que quiere obtener entre 950, 925, 900 y 800 milésimas, y la app le dice al instante cuántos gramos de cobre fino tiene que añadir y cuánto pesará la aleación final. El cobre es el único metal de liga: no se añade zinc, germanio, estaño ni níquel. La plata de partida se trata siempre como 999 milésimas reales, nunca como 1000, así que 100 g de plata fina contienen 99,900 g de plata pura. El cobre se calcula con la fórmula general (plata pura dividida por la ley objetivo, menos la masa de partida), nunca con coeficientes tabulados por ley. En España la Ley 17/1985 solo reconoce como leyes oficiales de contraste 999, 925 y 800: cuando el joyero elige 950 o 900 la pantalla debe advertirle de que son composiciones técnicas y no punzones oficiales españoles, cada una con su texto propio. La norma no admite tolerancia en menos, así que ningún redondeo, ni interno ni de pantalla, puede dejar la ley resultante por debajo de la objetivo: el cobre mostrado se trunca a la milésima de gramo, que es la resolución de una balanza de taller, y así la ley real queda siempre igual o por encima. Los cálculos internos usan aritmética decimal de alta precisión y no se redondea ningún paso intermedio. La pantalla tiene además un botón de limpiar que devuelve el formulario a su estado inicial y otro de guardar en favoritos que por ahora solo avisa de que la función llegará próximamente. La lógica de cálculo debe soportar también el modo inverso —partir del peso final de aleación deseado y obtener la plata fina y el cobre necesarios— verificado por pruebas, aunque la interfaz de esta versión no lo exponga. Referencia visual: UI_Plantillas/Feature_plata/ejemplo_feature_plata.png. Fuente de verdad numérica: UI_Plantillas/Feature_plata/ESPECIFICACION_CALCULADORA_ALEACIONES_PLATA.md"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Calcular el cobre de una aleación de plata (Priority: P1)

Un joyero tiene en el taller una cantidad de plata fina 999‰ y quiere bajarla a una ley concreta para trabajarla. Abre el módulo de plata desde el menú, introduce los gramos de plata fina y elige la ley que busca (950, 925, 900 u 800 milésimas). Al instante ve cuántos gramos de cobre debe añadir y cuánto pesará la aleación final. Cada vez que cambia la cantidad o la ley, los resultados se recalculan solos, sin pulsar ningún botón.

**Why this priority**: Es la función completa del módulo y la razón de que exista: pasar de una pantalla de andamiaje a la herramienta que resuelve el cálculo de taller más frecuente en plata, hoy hecho a mano y con riesgo de quedarse por debajo de ley.

**Independent Test**: Se abre el módulo, se introduce una cantidad y se recorren las cuatro leyes comprobando los resultados contra los casos numéricos del documento técnico. Entrega todo el valor por sí sola.

**Acceptance Scenarios**:

1. **Given** la pantalla del módulo de plata, **When** el joyero introduce 10 gramos y elige 925‰, **Then** ve que debe añadir 0,800 g de cobre y que obtendrá 10,800 g de plata de 925.
2. **Given** la pantalla del módulo de plata, **When** el joyero introduce 25 gramos con 925‰, **Then** ve 2,000 g de cobre y un total de 27,000 g.
3. **Given** un cálculo en pantalla, **When** el joyero cambia la ley de 925‰ a 800‰, **Then** los resultados se recalculan al instante y muestran 2,487 g de cobre para 10 gramos de partida.
4. **Given** el campo de cantidad, **When** el joyero escribe «12,35» o «12.35», **Then** ambas formas se aceptan y producen exactamente el mismo resultado.
5. **Given** el campo de cantidad vacío, a cero o con un valor no interpretable, **When** el joyero observa la pantalla, **Then** no se muestra ningún resultado ni mensaje de fallo alarmante, y la pantalla sigue operativa.
6. **Given** cualquier cálculo en pantalla, **When** el joyero suma mentalmente la plata de partida y el cobre indicado, **Then** el resultado coincide con el peso final que muestra la pantalla.
7. **Given** un cálculo con 100 gramos y 950‰, **When** el joyero observa el cobre indicado, **Then** ve 5,157 g y no 5,158 g: la cantidad mostrada nunca se pasa de cobre.

---

### User Story 2 - Saber que 950 y 900 no son leyes oficiales españolas (Priority: P2)

Un joyero selecciona 950‰ o 900‰ para un cálculo técnico. La pantalla le advierte de forma visible de que esa milésima es una composición técnica y no una de las leyes oficiales de contraste de plata en España, con un texto propio para cada una que la sitúa respecto de las leyes oficiales, de modo que nunca contraste una pieza confiando en un dato equivocado.

**Why this priority**: Es una salvaguarda legal para el usuario. El cálculo de 950 y 900 funciona igualmente sin ella, pero mostrarla es un requisito irrenunciable del documento técnico antes de publicar.

**Independent Test**: Se selecciona 950‰ y se comprueba que aparece su advertencia; se selecciona 900‰ y se comprueba que aparece la suya, distinta; se seleccionan 925‰ y 800‰ y se comprueba que no aparece ninguna.

**Acceptance Scenarios**:

1. **Given** la pantalla del módulo de plata, **When** el joyero selecciona 950‰, **Then** aparece una advertencia visible que indica que 950‰ es una composición técnica, que no es una de las leyes oficiales de contraste de plata en España y que supera la ley 925‰.
2. **Given** la pantalla del módulo de plata, **When** el joyero selecciona 900‰, **Then** aparece una advertencia visible con su texto propio: composición técnica, no ley oficial española, supera la ley 800‰ pero no alcanza 925‰.
3. **Given** una advertencia visible, **When** el joyero cambia a 925‰ o a 800‰, **Then** la advertencia desaparece.
4. **Given** cualquier estado de la pantalla, **When** el joyero lee las opciones de ley, **Then** 925‰ y 800‰ se distinguen como leyes oficiales españolas y en ningún lugar se presentan 950‰ ni 900‰ como leyes oficiales españolas.

---

### User Story 3 - Limpiar y empezar un cálculo nuevo (Priority: P3)

Tras un cálculo, el joyero quiere empezar otro desde cero. Pulsa «Limpiar» y la pantalla vuelve a su estado inicial: campo de cantidad vacío, ley por defecto y sin resultados.

**Why this priority**: Comodidad de uso repetido en taller. Sin este botón se puede lograr lo mismo borrando el campo a mano.

**Independent Test**: Se completa un cálculo, se pulsa «Limpiar» y se comprueba que la pantalla queda como recién abierta.

**Acceptance Scenarios**:

1. **Given** un cálculo completo en pantalla, **When** el joyero pulsa «Limpiar», **Then** el campo de cantidad queda vacío, la ley vuelve a la selección inicial y los resultados desaparecen.
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
- **Cantidades minúsculas** (0,001 g): el cálculo conserva la precisión; si el cobre exacto queda por debajo de la milésima de gramo, la cantidad mostrada es 0,000 y el joyero entiende que su balanza no puede pesar esa liga.
- **Muchos decimales introducidos**: se aceptan; el cálculo interno no pierde precisión aunque la vista se limite a 3 decimales.
- **Ley que exige más plata que la disponible**: no ocurre, porque las cuatro leyes ofrecidas son inferiores a 999‰ y el cobre a añadir es siempre positivo.
- **Teclado en pantalla desplegado**: los resultados siguen alcanzables desplazando el contenido; el teclado no los oculta de forma irrecuperable.
- **Fuente del sistema muy grande o pantalla pequeña**: ningún texto se recorta y todo el contenido sigue alcanzable mediante desplazamiento.
- **Lector de pantalla activo**: el selector de ley anuncia su opción elegida, el campo de cantidad anuncia su propósito y unidad, el resultado anuncia metal y gramos, el total anuncia ley y peso, y la advertencia de ley técnica se anuncia al aparecer.
- **Salir del módulo y volver a entrar**: la pantalla arranca limpia, en su estado inicial; no recuerda el cálculo anterior.
- **Redondeo de vista**: lo mostrado nunca se usa para recalcular; y aun así, pesar exactamente el cobre mostrado nunca deja la aleación por debajo de la ley objetivo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El módulo «Aleaciones de PLATA» del menú principal MUST llevar a la calculadora real; la pantalla de andamiaje desaparece de ese destino.
- **FR-002**: La pantalla MUST ofrecer un campo para introducir la cantidad de plata fina 999‰ en gramos, identificado con su unidad, que despliegue un teclado apto para números decimales.
- **FR-003**: El campo de cantidad MUST aceptar tanto coma como punto decimal y tratarlos como equivalentes.
- **FR-004**: El sistema MUST tratar como entrada no válida el campo vacío, los valores no numéricos y los valores menores o iguales a cero; con entrada no válida MUST NOT mostrarse ningún resultado y la pantalla MUST permanecer estable.
- **FR-005**: La pantalla MUST ofrecer la selección de ley objetivo entre exactamente cuatro opciones —950‰, 925‰, 900‰ y 800‰— con una sola activa en todo momento, distinguiendo visualmente las dos que son leyes oficiales españolas (925‰ y 800‰).
- **FR-006**: Con entrada válida, el sistema MUST calcular y mostrar los gramos de cobre a añadir y el peso total de la aleación resultante, recalculando automáticamente ante cualquier cambio de cantidad o de ley, sin acción adicional del usuario.
- **FR-007**: El cobre MUST ser el único metal de liga de esta calculadora; el sistema MUST NOT proponer ni añadir zinc, germanio, estaño, níquel ni ningún otro metal.
- **FR-008**: El total MUST identificar la ley de la plata resultante junto al peso final de la aleación.
- **FR-009**: Todos los cálculos MUST seguir las fórmulas y reglas del documento técnico `UI_Plantillas/Feature_plata/ESPECIFICACION_CALCULADORA_ALEACIONES_PLATA.md` (anexo de lógica de negocio de esta spec): pureza real de origen 0,999 —nunca 1,000— y fórmula general, sin coeficientes tabulados por ley.
- **FR-010**: El cálculo interno MUST conservar la precisión completa, MUST NOT redondear pasos intermedios y MUST NOT reutilizar cantidades ya redondeadas para cálculos posteriores.
- **FR-011**: Ningún redondeo, ni interno ni de presentación, MUST dejar la ley resultante por debajo de la ley objetivo: la cantidad de cobre mostrada MUST truncarse a la milésima de gramo, de modo que pesar exactamente esa cantidad produzca una ley igual o superior a la objetivo.
- **FR-012**: Los resultados MUST mostrarse con 3 decimales y coma decimal española, sin que esa presentación altere el cálculo interno.
- **FR-013**: Mientras 950‰ o 900‰ esté seleccionado, la pantalla MUST mostrar una advertencia visible, con texto propio para cada una, de que es una composición técnica y no una de las leyes oficiales de contraste de plata en España; la advertencia MUST desaparecer al seleccionar 925‰ u 800‰ y esas dos milésimas MUST NOT presentarse nunca como leyes oficiales.
- **FR-014**: La pantalla MUST ofrecer un control «Limpiar» que devuelva el formulario a su estado inicial: cantidad vacía, ley por defecto y sin resultados.
- **FR-015**: La pantalla MUST ofrecer un control «Guardar en favoritos» que, en esta versión, muestre un aviso efímero de «Próximamente» sin alterar el estado del cálculo.
- **FR-016**: La lógica de cálculo MUST soportar también el modo inverso —partir del peso final de aleación deseado y obtener la plata fina 999‰ y el cobre necesarios— verificado por pruebas, aunque la interfaz de esta versión MUST NOT exponerlo.
- **FR-017**: La pantalla MUST llegar desde el menú principal, ofrecer un control de retroceso que devuelva al joyero a la pantalla anterior y MUST NOT mostrar la barra de navegación inferior.
- **FR-018**: El contenido MUST poder desplazarse cuando no quepa completo, incluido con el teclado desplegado.
- **FR-019**: El sistema MUST registrar como telemetría la visualización de la pantalla y la realización de cálculos identificando la ley elegida; MUST NOT registrar la cantidad introducida.
- **FR-020**: Los elementos accionables MUST respetar el tamaño táctil mínimo del sistema de diseño; selector, campo, resultados, total y advertencia MUST anunciarse correctamente a los lectores de pantalla.
- **FR-021**: Todos los textos visibles MUST poder traducirse a otro idioma sin modificar el comportamiento de la pantalla.

### Key Entities

- **Ley de plata**: cada una de las cuatro milésimas objetivo que ofrece la calculadora (950, 925, 900, 800), con su finura y con la marca de si es o no una de las leyes oficiales de contraste de plata en España según la Ley 17/1985. 925 y 800 lo son; 950 y 900 son composiciones técnicas y llevan advertencia obligatoria. No se configuran ni se descargan: viajan dentro de la app.
- **Cálculo de aleación de plata**: resultado de rebajar una cantidad de plata fina de pureza 999‰ hasta una ley objetivo añadiendo cobre. Comprende la plata pura contenida, el cobre a añadir, el peso final y la ley teórica resultante. Existe en dos sentidos: directo (desde la plata disponible) e inverso (desde el peso final deseado, sin interfaz en esta versión).
- **Cobre de liga**: único metal de aleación de esta calculadora, con nombre visible traducible e identificador estable independiente del idioma. Se asume cobre fino.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Los 4 casos de prueba numéricos del documento técnico (§21) producen exactamente los valores esperados en pantalla: con 10 g de plata fina, 0,515 g de cobre para 950‰, 0,800 g para 925‰, 1,100 g para 900‰ y 2,487 g para 800‰.
- **SC-002**: En las **4** leyes y para cualquier cantidad válida, el cobre a añadir es positivo, el peso final es la suma de la plata de partida y el cobre, y la ley teórica resultante nunca queda por debajo de la objetivo.
- **SC-003**: Pesar exactamente la cantidad de cobre **mostrada en pantalla** produce una ley real igual o superior a la objetivo en el **100%** de las combinaciones de ley y cantidad probadas; en particular, 100 g de plata fina hacia 950‰ muestran 5,157 g y no 5,158 g.
- **SC-004**: Un joyero completa un cálculo —introducir cantidad y elegir ley— con **2 interacciones** y ve los resultados **al instante**, sin botón de calcular.
- **SC-005**: Con 950‰ o 900‰ seleccionado, la advertencia correspondiente es visible en el **100%** de los casos, cada una con su texto propio; con 925‰ u 800‰ no aparece nunca.
- **SC-006**: La pantalla renderizada es reconocible como el mockup de referencia en una comparación visual lado a lado.
- **SC-007**: Con la fuente del sistema al doble y en pantallas desde 5", ningún texto se recorta y todo el contenido es alcanzable.
- **SC-008**: Con lector de pantalla, el **100%** de los controles, resultados y avisos se anuncian con una descripción que identifica su función o su valor.
- **SC-009**: Ningún destino del menú principal de módulos lleva ya a una pantalla de andamiaje para el módulo de plata.
- **SC-010**: Introducir la misma cantidad con coma o con punto produce resultados idénticos en el **100%** de los casos.

## Assumptions

- El destino del módulo de plata ya existe en la navegación desde la feature 002 y hoy muestra una pantalla de andamiaje; esta feature sustituye su contenido, no crea un destino nuevo.
- **Fuente de verdad numérica**: el documento `UI_Plantillas/Feature_plata/ESPECIFICACION_CALCULADORA_ALEACIONES_PLATA.md` prevalece sobre cualquier otra referencia, incluido el mockup.
- **Estado inicial**: campo de cantidad vacío y 925‰ preseleccionada —la plata Sterling, la ley de trabajo habitual y la que muestra el mockup—, sin resultados. Al volver a entrar en el módulo la pantalla arranca en ese estado inicial, sin memoria del cálculo anterior.
- **Resultados mostrados**: una fila de cobre y el total de la aleación, más la advertencia de ley técnica cuando toca. La plata pura contenida y la ley teórica resultante se calculan internamente pero no se muestran. Decisión confirmada con el autor.
- **Decimales**: 3 en la vista, con coma decimal, truncados a la baja. Los 3 decimales equivalen a la resolución de 0,001 g que el documento técnico recomienda por defecto para una balanza de taller (§18), de modo que el «modo taller seguro» del documento (§16-§17) se cumple sin necesidad de que el joyero elija resolución. Decisión confirmada con el autor.
- **Selector de resolución de balanza**: fuera de alcance en esta versión. El documento lo plantea como recomendable (§18), pero con 3 decimales truncados la cifra mostrada ya es directamente pesable y la ley queda protegida. Decisión confirmada con el autor.
- **Marca de ley oficial**: 925‰ y 800‰ se distinguen en el selector como leyes oficiales españolas; el mockup solo marca 925‰, pero 800‰ lo es igualmente según la Ley 17/1985 y marcar una sola sería incoherente. Decisión confirmada con el autor.
- La pantalla no lleva barra de navegación inferior, aunque el mockup dibuje otra composición de barra superior: es una sección de módulo y sigue el patrón de navegación de la app (título y retroceso).
- Los botones inferiores son «Limpiar» y «Guardar en favoritos», como en el módulo de oro, y no el botón «OK» que dibuja el mockup: el cálculo es reactivo y no hay nada que confirmar. Decisión confirmada con el autor.
- La telemetría de la pantalla conserva la identidad que ya emite el andamiaje del módulo de plata, para no romper la serie histórica.
- La app sigue en un solo idioma, español, y bloqueada en orientación vertical.
- Quedan fuera de alcance y entrarán como features propias: persistencia de favoritos (el botón solo avisa de «Próximamente»), modo inverso en la interfaz, selector de resolución de balanza, vista técnica con más decimales, otros orígenes de plata (999,9‰, plata reciclada de ley conocida), mezcla de dos platas, subida de ley, cobre de pureza configurable, mermas de fundición, soldaduras de plata, aleaciones con germanio o Argentium, costes y precios. El diseño de la lógica no debe impedir añadirlas después.
