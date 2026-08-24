# Feature Specification: Calculadora de soldaduras de joyería

**Feature Branch**: `006-soldaduras-joyeria`

**Created**: 2026-08-24

**Status**: Draft

**Input**: User description: "Calculadora de soldaduras de joyería (módulo «Soldaduras de Oro y Plata», hoy un placeholder). Sustituye el placeholder por una calculadora con tres familias de soldadura, según el documento técnico UI_Plantillas/Feature_Soldadura/ESPECIFICACION_CALCULADORA_SOLDADURAS_JOYERIA.md (que prevalece sobre los mockups en lo matemático) y los 7 mockups de esa carpeta. La primera vez que el joyero entra solo ve el selector de la soldadura que desea fabricar (ORO LEY, CLÁSICA, PLATA); cada familia admite dos modos de entrada con conmutador («desde el metal que tengo» y «peso final deseado»), cálculo reactivo, coma o punto decimal, 3 decimales, Limpiar y Guardar en favoritos. ORO LEY: color del oro 18K (amarillo/blanco/rosa), 5 durezas (r = 0,3 / 0,5 / 1 / 2 / 3) y pantalla propia de soldadura BASE (10 g oro 24K → 0,54 cobre, 0,80 plata, 0,92 zinc, 1,00 cadmio; total 13,26) con proceso de taller y advertencia de seguridad. CLÁSICA: floja 5/2/1, fuerte 5/0,5/0,5/0,5, muy floja de ley 1/0,10/0,16/0,18, sin color. PLATA: latón respecto a la plata fina con p = 0,75 / 0,50 / 0,40 / 0,30. Advertencia de seguridad §9 en toda receta con cadmio o zinc; nota de redondeo §8.3; los diez tests mínimos de §10 como criterios de aceptación."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Elegir familia y calcular la soldadura de oro de ley (Priority: P1)

Un joyero entra en el módulo de Soldaduras y, la primera vez, solo ve el selector de la soldadura que desea fabricar: ORO LEY, CLÁSICA o PLATA. Elige ORO LEY y aparece su formulario: el color del oro de 18 K que va a usar (amarillo, blanco o rosa), un campo para los gramos de oro 18 K de que dispone y cinco durezas (muy floja, floja, media, fuerte y muy fuerte). Al instante ve cuánta soldadura BASE necesita mezclar con su oro y cuánto pesará la soldadura resultante. El color no cambia ninguna cantidad: solo identifica el oro con el que trabaja.

**Why this priority**: Es la familia protagonista del encargo y la razón del módulo: el método de dos fases (base + oro de ley) es el que da nombre a la pantalla y el flujo que abren los mockups.

**Independent Test**: Se abre el módulo, se elige ORO LEY y se recorren durezas y colores comprobando los resultados contra las tablas del documento técnico. Entrega valor completo por sí sola aunque no exista nada más.

**Acceptance Scenarios**:

1. **Given** la primera visita al módulo, **When** el joyero observa la pantalla, **Then** solo ve el selector con las tres familias, sin formulario, sin resultados y sin botones de acción.
2. **Given** el selector de familias, **When** el joyero pulsa ORO LEY, **Then** aparece debajo el formulario de oro de ley con el color amarillo y la dureza muy floja preseleccionados.
3. **Given** el formulario de ORO LEY, **When** el joyero introduce 2 gramos de oro 18 K con dureza muy floja, **Then** ve que necesita 6,667 g de soldadura BASE y un total de 8,667 g de soldadura.
4. **Given** el formulario de ORO LEY, **When** el joyero introduce 5 gramos con dureza media, **Then** ve 5,000 g de base y 10,000 g de total.
5. **Given** un cálculo en pantalla, **When** el joyero cambia el color del oro, **Then** las cantidades no varían: solo cambia la identificación del oro elegido.
6. **Given** el formulario de ORO LEY, **When** el joyero recorre las cinco durezas con la misma cantidad, **Then** a más dureza ve proporcionalmente menos base por gramo de oro, según los factores 0,3 / 0,5 / 1 / 2 / 3.
7. **Given** el campo de cantidad vacío, a cero, negativo o no interpretable, **When** el joyero observa la pantalla, **Then** no hay resultados ni mensaje alarmante y la pantalla sigue operativa.

---

### User Story 2 - Preparar la soldadura BASE (Priority: P2)

Desde el formulario de ORO LEY, el joyero pulsa «SOLDADURA BASE» y llega a una pantalla propia que le enseña a fabricar la base: primero una advertencia de seguridad sobre los humos de cadmio y zinc, después el proceso de taller (fundir oro, plata y cobre; añadir zinc y cadmio; bajar el fuego; laminar) y por último la calculadora: introduce sus gramos de oro fino de 24 K y ve cuánto cobre, plata fina, zinc y cadmio añadir y el peso teórico de base que obtendrá.

**Why this priority**: Sin la base no existe la primera fase del método de ley. Es una pantalla independiente con receta propia y las obligaciones de seguridad más estrictas del documento técnico.

**Independent Test**: Se navega a la pantalla, se comprueban advertencia y proceso, se introduce 10 g de oro 24 K y se validan los cuatro ingredientes y el total contra el documento técnico.

**Acceptance Scenarios**:

1. **Given** el formulario de ORO LEY, **When** el joyero pulsa «SOLDADURA BASE», **Then** llega a la pantalla de la base, con su título y retroceso a la pantalla anterior.
2. **Given** la pantalla de la base, **When** el joyero la observa, **Then** la advertencia de seguridad sobre humos de cadmio y zinc es visible antes del proceso informativo de taller.
3. **Given** la pantalla de la base, **When** el joyero introduce 10 gramos de oro 24 K, **Then** ve 0,540 g de cobre, 0,800 g de plata fina, 0,920 g de zinc y 1,000 g de cadmio, con un total teórico de 13,260 g.
4. **Given** cualquier cálculo de base, **When** el joyero lee la pantalla, **Then** en ningún lugar se muestran las milésimas reales de la base ni se presentan sus cantidades corregidas para alcanzar 750 milésimas; la base se llama «base de oro de 18 K» y se indica que la masa es teórica y no compensa pérdidas de taller.
5. **Given** la pantalla de la base, **When** el joyero introduce un valor vacío, cero, negativo o no numérico, **Then** no hay resultados y la pantalla sigue estable.

---

### User Story 3 - Calcular una soldadura clásica de oro amarillo (Priority: P3)

El joyero elige CLÁSICA y ve tres tipos: floja, fuerte y muy floja de ley. Introduce los gramos de oro de que dispone (oro 18 K en floja y fuerte; oro fino 24 K en muy floja de ley) y ve al instante los demás ingredientes y el total teórico. En estas recetas no se elige color: son de oro amarillo. Cuando el tipo elegido lleva cadmio, la pantalla muestra la advertencia de seguridad.

**Why this priority**: Segunda familia en valor de taller; recetas cerradas de escalado directo que funcionan sin depender de las otras familias.

**Independent Test**: Se elige CLÁSICA y se validan los tres tipos contra las recetas patrón del documento técnico.

**Acceptance Scenarios**:

1. **Given** el formulario de CLÁSICA con tipo floja, **When** el joyero introduce 10 gramos de oro 18 K, **Then** ve 4,000 g de plata fina y 2,000 g de latón, con un total de 16,000 g.
2. **Given** el formulario de CLÁSICA con tipo fuerte, **When** el joyero introduce 10 gramos de oro 18 K, **Then** ve 1,000 g de plata fina, 1,000 g de cobre y 1,000 g de latón, con un total de 13,000 g.
3. **Given** el formulario de CLÁSICA con tipo muy floja de ley, **When** el joyero introduce 10 gramos de oro 24 K, **Then** ve 1,000 g de plata fina, 1,600 g de latón y 1,800 g de cadmio, con un total de 14,400 g.
4. **Given** el tipo muy floja de ley seleccionado, **When** el joyero observa la pantalla, **Then** la advertencia de seguridad sobre humos es visible; **When** cambia a floja o fuerte, **Then** desaparece.
5. **Given** cualquier tipo de CLÁSICA, **When** el joyero busca un selector de color, **Then** no existe: las recetas clásicas son de oro amarillo.

---

### User Story 4 - Calcular una soldadura de plata (Priority: P4)

El joyero elige PLATA, introduce sus gramos de plata fina 999 y elige entre cuatro tipos: muy floja (recomendada para composturas), floja, normal y fuerte. Ve al instante el latón a añadir y el peso final. El porcentaje de cada tipo es latón respecto a la plata fina, no sobre el peso final.

**Why this priority**: Tercera familia, la más simple; cierra el alcance de las tres familias del documento técnico.

**Independent Test**: Se elige PLATA y se validan los cuatro tipos contra la tabla del documento técnico con 25 g.

**Acceptance Scenarios**:

1. **Given** el formulario de PLATA con tipo muy floja, **When** el joyero introduce 25 gramos de plata fina, **Then** ve 18,750 g de latón y un total de 43,750 g.
2. **Given** el formulario de PLATA con tipo fuerte, **When** el joyero introduce 25 gramos, **Then** ve 7,500 g de latón y un total de 32,500 g.
3. **Given** el formulario de PLATA, **When** el joyero lee los tipos, **Then** muy floja se distingue como recomendada para composturas.
4. **Given** un cálculo de plata en pantalla, **When** el joyero suma mentalmente la plata introducida y el latón indicado, **Then** coincide con el peso final mostrado.

---

### User Story 5 - Calcular desde el peso final deseado (Priority: P5)

En cualquiera de las tres familias, y también en la pantalla de la base, el joyero cambia el conmutador de modo de «desde el metal que tengo» a «peso final deseado», introduce los gramos de soldadura (o de base) que quiere obtener y ve el desglose completo de ingredientes, incluido el metal que en el modo directo era la entrada.

**Why this priority**: Modo exigido por el documento técnico en todas las familias; amplía los formularios ya construidos sin alterar sus recetas.

**Independent Test**: Se cambia el modo en cada familia y se validan los repartos contra los ejemplos del documento técnico.

**Acceptance Scenarios**:

1. **Given** ORO LEY en modo peso final con dureza muy fuerte, **When** el joyero introduce 10 gramos, **Then** ve 2,500 g de base y 7,500 g de oro 18 K del color elegido.
2. **Given** CLÁSICA floja en modo peso final, **When** el joyero introduce 8 gramos, **Then** ve 5,000 g de oro 18 K, 2,000 g de plata fina y 1,000 g de latón.
3. **Given** PLATA muy floja en modo peso final, **When** el joyero introduce 10 gramos, **Then** ve 5,714 g de plata fina y 4,286 g de latón.
4. **Given** la pantalla de la base en modo peso de base, **When** el joyero introduce 13,26 gramos, **Then** ve 10,000 g de oro 24 K junto a los cuatro ingredientes de liga.
5. **Given** un cálculo en modo directo, **When** el joyero cambia de modo, **Then** el campo de cantidad se vacía y los resultados desaparecen: una cifra tecleada como metal disponible nunca se reinterpreta en silencio como peso final.
6. **Given** CLÁSICA muy floja de ley en modo peso final con 10 gramos, **When** el joyero suma los ingredientes mostrados, **Then** la suma puede desviarse una milésima del total pedido y una nota junto al total lo explica, sin que ningún ingrediente se altere para cuadrarla.

---

### User Story 6 - Limpiar y guardar en favoritos (Priority: P6)

Tras un cálculo, el joyero pulsa «Limpiar» y el formulario de la familia activa vuelve a su estado inicial. Si pulsa «Guardar en favoritos», la app le informa con un aviso efímero de que esa función llegará próximamente.

**Why this priority**: Comodidad de uso repetido y coherencia con las calculadoras de oro y plata; no aporta cálculo nuevo.

**Independent Test**: Se completa un cálculo, se pulsa cada botón y se comprueba el efecto.

**Acceptance Scenarios**:

1. **Given** un cálculo completo en cualquier familia, **When** el joyero pulsa «Limpiar», **Then** el campo queda vacío, los selectores de esa familia vuelven a su valor inicial, los resultados desaparecen y la familia elegida se conserva.
2. **Given** cualquier estado con familia elegida, **When** el joyero pulsa «Guardar en favoritos», **Then** aparece un aviso efímero de «Próximamente» y el cálculo no se altera.
3. **Given** la primera visita sin familia elegida, **When** el joyero observa la pantalla, **Then** los botones de Limpiar y Guardar en favoritos no se muestran.

---

### Edge Cases

- **Cambio de familia con un cálculo en pantalla**: el formulario arranca limpio en la nueva familia (cantidad vacía, modo directo, selecciones por defecto); la misma cifra significaría otro metal.
- **Cantidad con formato ambiguo** («1.2,3», dos comas, solo separador): valor no interpretable; sin resultados y sin cierre de la app.
- **Coma y punto decimal**: «2,5» y «2.5» producen exactamente el mismo resultado.
- **Repartos con división no exacta** (peso final entre 1,44; base desde peso deseado; oro entre dureza 0,3): la suma visible puede desviarse una milésima del total; la nota de redondeo lo advierte y jamás se altera un ingrediente para cuadrarla.
- **Cantidades muy grandes o minúsculas**: se calculan con precisión completa; los resultados se muestran enteros sin romper la composición; por debajo de la milésima de gramo la vista muestra 0,000.
- **Muchos decimales introducidos**: se aceptan; el cálculo interno no pierde precisión aunque la vista muestre 3 decimales.
- **Teclado en pantalla desplegado**: los resultados siguen alcanzables desplazando el contenido.
- **Fuente del sistema muy grande o pantalla pequeña**: ningún texto se recorta, las cinco durezas siguen legibles y todo el contenido es alcanzable por desplazamiento.
- **Lector de pantalla activo**: los selectores anuncian su opción elegida, el campo su propósito y unidad, cada fila su ingrediente y gramos, el total su etiqueta y peso, y las advertencias de seguridad se anuncian al aparecer.
- **Salir del módulo y volver a entrar**: la pantalla arranca en su estado inicial (solo el selector de familias); no recuerda el cálculo anterior.
- **Redondeo de vista**: lo mostrado nunca se usa para recalcular.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El módulo «Soldaduras de Oro y Plata» del menú principal MUST llevar a la calculadora real; la pantalla de andamiaje desaparece de ese destino.
- **FR-002**: La pantalla MUST abrir mostrando únicamente el selector de familia con tres opciones excluyentes —ORO LEY, CLÁSICA y PLATA— sin ninguna preseleccionada; el formulario, los resultados y los botones de acción MUST aparecer solo tras elegir familia.
- **FR-003**: Cada familia MUST ofrecer un conmutador de modo con dos opciones excluyentes: «desde el metal que tengo» (por defecto) y «peso final deseado»; la pantalla de la base MUST ofrecer el suyo («desde el oro 24 K» y «peso de base deseado»).
- **FR-004**: El campo de cantidad MUST identificarse con su unidad en gramos, desplegar un teclado apto para decimales y aceptar coma y punto como equivalentes.
- **FR-005**: El sistema MUST tratar como entrada no válida el campo vacío, los valores no numéricos y los menores o iguales a cero; con entrada no válida MUST NOT mostrarse ningún resultado y la pantalla MUST permanecer estable.
- **FR-006**: Con familia elegida y entrada válida, el sistema MUST calcular y mostrar el resultado al instante, recalculando ante cualquier cambio de cantidad, modo, tipo, dureza o color, sin acción adicional del usuario.
- **FR-007**: Todos los cálculos MUST seguir las recetas, factores y fórmulas del documento técnico `UI_Plantillas/Feature_Soldadura/ESPECIFICACION_CALCULADORA_SOLDADURAS_JOYERIA.md` (anexo de lógica de negocio de esta spec), que MUST prevalecer sobre los mockups; en particular, la receta de la base es, por 10 g de oro 24 K: 0,54 g de cobre, 0,80 g de plata fina, 0,92 g de zinc y 1,00 g de cadmio (total teórico 13,26 g), aunque el mockup muestre esos valores intercambiados.
- **FR-008**: Las familias MUST NOT mezclarse: cada cálculo usa exclusivamente las recetas y factores de su familia.
- **FR-009**: ORO LEY MUST ofrecer la selección del color del oro 18 K entre exactamente amarillo, blanco y rosa; el color MUST NOT alterar ninguna cantidad y MUST identificar el oro en el resultado y en la telemetría.
- **FR-010**: ORO LEY MUST ofrecer exactamente cinco durezas —muy floja, floja, media, fuerte y muy fuerte— con factores 0,3 / 0,5 / 1 / 2 / 3 gramos de oro 18 K por gramo de base; en modo directo (gramos de oro 18 K disponibles) MUST mostrarse la soldadura BASE necesaria y el peso final; en modo peso final MUST mostrarse base, oro 18 K del color elegido y total.
- **FR-011**: ORO LEY MUST ofrecer un acceso visible «SOLDADURA BASE» hacia la pantalla de la base, acompañado de la aclaración de que es una soldadura de 18 K con punto de fusión muy bajo.
- **FR-012**: La pantalla de la base MUST calcular la receta de la base desde los gramos de oro 24 K o desde el peso de base deseado, mostrando los cuatro ingredientes de liga (y el oro 24 K en modo inverso) y el peso teórico total.
- **FR-013**: La pantalla de la base MUST mostrar el proceso informativo de taller —fundir primero oro, plata y cobre; añadir después zinc y cadmio; bajar la intensidad del fuego; laminar el lingote— como texto separado de la calculadora, y MUST indicar que la masa calculada es teórica y que no se compensan pérdidas de fundición.
- **FR-014**: La pantalla de la base MUST conservar el nombre tradicional «base de oro de 18 K», MUST NOT mostrar sus milésimas reales y MUST NOT corregir los pesos para forzar 750 milésimas.
- **FR-015**: CLÁSICA MUST ofrecer exactamente tres tipos —floja (5 g oro 18 K, 2 g plata fina, 1 g latón), fuerte (5 g oro 18 K, 0,50 g latón, 0,50 g cobre, 0,50 g plata fina) y muy floja de ley (1 g oro 24 K, 0,10 g plata fina, 0,16 g latón, 0,18 g cadmio)— escalados proporcionalmente; la entrada del modo directo MUST ser el oro de la receta (18 K o 24 K según el tipo) y MUST NOT ofrecerse selección de color.
- **FR-016**: PLATA MUST ofrecer exactamente cuatro tipos —muy floja (0,75), floja (0,50), normal (0,40) y fuerte (0,30)— donde el factor es latón respecto a la plata fina, no sobre el peso final; muy floja MUST señalarse como recomendada para composturas.
- **FR-017**: Toda configuración cuya receta contenga cadmio o zinc —el tipo muy floja de ley de CLÁSICA y la pantalla de la base— MUST mostrar una advertencia de seguridad visible con el texto del documento técnico (humos peligrosos, no inhalarlos, extracción y protección, no sustituye la formación profesional); en la pantalla de la base MUST aparecer antes del proceso informativo. La advertencia MUST desaparecer en CLÁSICA al cambiar a un tipo sin cadmio.
- **FR-018**: La interfaz MUST NOT afirmar que las recetas estén certificadas o verificadas metalúrgicamente, y las instrucciones de taller MUST NOT presentarse como garantía de seguridad.
- **FR-019**: El cálculo interno MUST conservar la precisión completa, MUST NOT redondear pasos intermedios y MUST NOT reutilizar cantidades ya redondeadas para cálculos posteriores.
- **FR-020**: Los resultados MUST mostrarse con 3 decimales y coma decimal española, redondeados a la cifra más cercana, sin que la presentación altere el cálculo interno.
- **FR-021**: Junto al peso total MUST mostrarse una nota de que la suma visible puede variar mínimamente por redondeo; el sistema MUST NOT ajustar ningún ingrediente para cuadrar la suma mostrada.
- **FR-022**: Cada resultado MUST presentar sus ingredientes con nombre e imagen identificativa, en el orden de la receta del documento técnico, y el peso total etiquetado; en modo directo el metal introducido MUST NOT repetirse como fila de resultado en CLÁSICA y PLATA.
- **FR-023**: Cambiar de familia MUST reiniciar el formulario (cantidad vacía, modo directo, selecciones por defecto); cambiar de modo MUST vaciar la cantidad y los resultados conservando las demás selecciones.
- **FR-024**: Con familia elegida, la pantalla MUST ofrecer «Limpiar» —que devuelve el formulario de la familia activa a su estado inicial conservando la familia— y «Guardar en favoritos» —que en esta versión muestra un aviso efímero de «Próximamente» sin alterar el estado—; ambos MUST existir también en la pantalla de la base.
- **FR-025**: Ambas pantallas MUST llegar por navegación (la principal desde el menú, la base desde ORO LEY), ofrecer retroceso a la pantalla anterior y MUST NOT mostrar la barra de navegación inferior.
- **FR-026**: El contenido MUST poder desplazarse cuando no quepa completo, incluido con el teclado desplegado.
- **FR-027**: El sistema MUST registrar como telemetría la visualización de cada una de las dos pantallas —la principal conservando la identidad que ya emite el andamiaje— y la realización de cálculos identificando familia, modo, tipo o dureza y, en ORO LEY, color; MUST NOT registrar las cantidades introducidas y MUST NOT registrar cálculos repetidos de la misma combinación mientras solo cambie la cifra tecleada.
- **FR-028**: Los elementos accionables MUST respetar el tamaño táctil mínimo del sistema de diseño; selectores, campo, filas de resultado, totales y advertencias MUST anunciarse correctamente a los lectores de pantalla.
- **FR-029**: Todos los textos visibles MUST poder traducirse a otro idioma sin modificar el comportamiento de las pantallas.

### Key Entities

- **Familia de soldadura**: cada uno de los tres métodos excluyentes que ofrece el módulo (oro de ley 18 K por base, clásicas de oro amarillo, plata), con sus tipos o durezas propios. No se configuran ni se descargan: viajan dentro de la app.
- **Receta de soldadura**: composición patrón de ingredientes con pesos de referencia y orden de presentación estable (las tres clásicas y la base), escalable proporcionalmente en ambos sentidos. Sus valores son los del documento técnico, versionados como única fuente de verdad.
- **Factor de mezcla**: proporción que define un tipo sin receta tabulada: latón respecto a plata fina en PLATA (0,75/0,50/0,40/0,30) y oro 18 K por gramo de base en ORO LEY (0,3/0,5/1/2/3).
- **Cálculo de soldadura**: resultado de escalar una receta o aplicar un factor, en modo directo (desde el metal disponible) o inverso (desde el peso final). Comprende los ingredientes con sus gramos exactos, el peso total teórico y, en ORO LEY, el color del oro elegido.
- **Ingrediente**: cada metal o preparado que interviene (oro 24 K, oro 18 K por color, plata fina, latón, cobre, zinc, cadmio y la propia base), con nombre visible traducible, imagen identificativa e identificador estable independiente del idioma.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Los diez casos de prueba mínimos del documento técnico (§10) producen exactamente los valores esperados: reparto de 8 g flojos (5/2/1), de 6,50 g fuertes (0,50/0,50/0,50/5), de 10,08 g muy flojos de ley (7/0,70/1,12/1,26), plata 25 g muy floja (18,75/43,75) y fuerte (7,50/32,50), base desde 10 g de oro (0,54/0,80/0,92/1/13,26), mezcla desde 1 g de base muy floja (0,30/1,30), 10 g muy fuertes desde peso final (2,5/7,5), color blanco con 0,5 g, y rechazo de vacío/cero/negativo/no numérico.
- **SC-002**: En todas las familias, modos y tipos, con cualquier entrada válida: todos los ingredientes calculados son positivos, la suma interna coincide con el total teórico, duplicar la entrada duplica todos los ingredientes y cambiar el color solo cambia el material identificado, nunca su peso.
- **SC-003**: Los ejemplos de los mockups se reproducen en pantalla: 2 g de oro 18 K muy floja → 6,667 g de base; clásica floja con 10 g → 4,000 g de plata y 2,000 g de latón; clásica fuerte con 10 g → 1,000/1,000/1,000; muy floja de ley con 10 g → 1,000/1,600/1,800; plata muy floja con 25 g → 18,750 g de latón.
- **SC-004**: Un joyero completa un cálculo —elegir familia, introducir cantidad y elegir tipo— con **3 interacciones** y ve los resultados **al instante**, sin botón de calcular.
- **SC-005**: La advertencia de seguridad es visible en el **100%** de las configuraciones con cadmio o zinc (muy floja de ley y pantalla de la base) y en la base precede al proceso informativo; nunca aparece en configuraciones sin ellos.
- **SC-006**: En la primera visita solo es visible el selector de familias; tras elegir una, el formulario correspondiente aparece sin abandonar la pantalla.
- **SC-007**: Introducir la misma cantidad con coma o con punto produce resultados idénticos en el **100%** de los casos.
- **SC-008**: En los repartos con división no exacta, la suma de los ingredientes mostrados difiere del total pedido como máximo en una milésima de gramo por ingrediente, la nota de redondeo es visible y ningún valor interno se altera para cuadrar la vista.
- **SC-009**: Con la fuente del sistema al doble y en pantallas desde 5", ningún texto se recorta, las cinco durezas son legibles y todo el contenido es alcanzable.
- **SC-010**: Con lector de pantalla, el **100%** de los controles, resultados y avisos se anuncian con una descripción que identifica su función o su valor.
- **SC-011**: Ningún destino del menú principal lleva ya a una pantalla de andamiaje para el módulo de soldaduras.
- **SC-012**: Las pantallas renderizadas son reconocibles frente a los mockups de referencia en una comparación lado a lado, con los estilos propios de la app.

## Assumptions

- El destino del módulo de soldaduras ya existe en la navegación desde la feature 002 y hoy muestra una pantalla de andamiaje; esta feature sustituye su contenido y añade el destino nuevo de la base.
- **Fuente de verdad numérica**: `UI_Plantillas/Feature_Soldadura/ESPECIFICACION_CALCULADORA_SOLDADURAS_JOYERIA.md` prevalece sobre cualquier otra referencia, incluidos los mockups (los valores de la base del mockup están intercambiados y no se usan).
- **Cinco durezas en ORO LEY**: el mockup pinta cuatro, pero el documento técnico define cinco y sus criterios de aceptación exigen que todos los factores coincidan con él. Decisión confirmada con el autor.
- **Selector de color en ORO LEY**: el mockup no lo dibuja, pero el documento técnico exige elegir color en este método. Se ofrece con las tres opciones admitidas (sin el rojo de la calculadora de oro). Decisión confirmada con el autor.
- **Ambos modos de entrada**: los mockups solo dibujan el modo «desde el metal»; el documento técnico exige como mínimo el peso final. Se ofrecen los dos con conmutador en todas las familias y en la base. Decisión confirmada con el autor.
- **Estado inicial**: sin familia preseleccionada (mockup de primera visita); dentro de cada familia, modo directo, primera dureza/tipo y color amarillo. Al volver a entrar, la pantalla arranca en el estado inicial sin memoria del cálculo anterior.
- **Decimales**: 3 en la vista, con coma decimal, redondeados a la cifra más cercana. Aquí no hay ley de contraste que proteger (a diferencia de la calculadora de plata, que trunca): son recetas de taller y la cifra más fiel es la más útil; el documento pide 3 decimales por defecto. Los 3 decimales fijos (sin eliminar ceros finales) mantienen la coherencia visual con las calculadoras de oro y plata.
- **Totales visibles**: el peso final teórico se muestra en todas las familias y modos, también donde el mockup lo omite, porque el documento técnico exige presentarlo. En ORO LEY directo se muestran la base necesaria y el total.
- Los botones son «Limpiar» y «Guardar en favoritos», como en oro y plata, y no el botón «OK» del mockup: el cálculo es reactivo y no hay nada que confirmar.
- La advertencia de seguridad usa la redacción del documento técnico (§9); el consejo de doble fundido y laminado de la mezcla final (§5.6) se muestra como recomendación junto al resultado de ORO LEY.
- **Imágenes**: se incorporan las nuevas de granalla, cadmio, zinc, latón y proceso; el oro, la plata y el cobre reutilizan las existentes. Los iconos de dureza en imagen de los mockups no se usan: los selectores de la app son tipográficos y mandan sus estilos. Decisión del encargo («esto es orientativo, usa los estilos de la aplicación»).
- La telemetría de la pantalla principal conserva la identidad que ya emite el andamiaje; la pantalla de la base estrena la suya.
- La lógica también resuelve la mezcla de ley desde la cantidad de base disponible, verificada por pruebas, aunque la interfaz de esta versión no exponga ese tercer modo.
- La app sigue en un solo idioma, español, y bloqueada en orientación vertical.
- Quedan fuera de alcance y entrarán como features propias: persistencia de favoritos, selección de resolución de balanza, compensación de mermas, otras leyes de oro en la mezcla (14 K, 9 K), recetas configurables por el usuario, costes y precios. El diseño de la lógica no debe impedir añadirlas después.
