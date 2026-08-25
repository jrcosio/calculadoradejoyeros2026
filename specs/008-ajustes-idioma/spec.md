# Feature Specification: Ajustes — idioma de la aplicación

**Feature Branch**: `008-ajustes-idioma`

**Created**: 2026-08-25

**Status**: Draft

**Input**: User description: "Ajustes: idioma de la aplicación. El joyero puede elegir en qué idioma ve la app entre español, inglés, francés, alemán e italiano, más una opción «Automático» que sigue al idioma del dispositivo. Hoy la pantalla de Ajustes es un andamiaje sin contenido y la app está solo en español, así que un joyero con el móvil en otro idioma no la entiende. Comportamiento esperado: (1) mientras no se haya elegido nada en Ajustes, la app se muestra en el idioma del dispositivo, detectado ya desde la pantalla de inicio; si el idioma del dispositivo no es uno de los cinco soportados, se muestra en español; (2) al tocar un idioma en Ajustes, todos los textos de la app cambian al instante, sin reiniciar ni salir de la pantalla; (3) la elección se recuerda entre arranques y prevalece sobre el idioma del dispositivo aunque este cambie; (4) la opción «Automático» devuelve el control al dispositivo y también se recuerda; (5) la pantalla muestra cada idioma con su bandera y su nombre en su propio idioma, e indica claramente cuál está activo y qué idioma ha detectado del sistema. Alcance: se traducen todos los textos visibles de las cinco calculadoras, la portada, el menú, la información y las barras de navegación. No se traducen la marca «Calculadora de Joyeros», los nombres de personas y empresas, los nombres de las redes sociales ni los símbolos de unidades. El formato de las cifras (coma decimal) no cambia con el idioma en esta feature. Ajustes no incorpora ninguna otra opción por ahora."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Elegir el idioma y ver la app cambiar al instante (Priority: P1)

Un joyero italiano abre Ajustes, ve una lista de idiomas con sus banderas y toca la bandera italiana.
Antes de levantar el dedo, el título de la pantalla, la barra inferior y el propio nombre de la
sección ya están en italiano. Sale a la portada y recorre sus calculadoras: todo está en italiano.

**Why this priority**: Es la feature entera vista desde el joyero. Sin ella Ajustes sigue siendo un
andamiaje y la app solo sirve a quien lee español. Que el cambio se vea en el mismo momento y en la
misma pantalla es lo que convierte la lista en algo comprensible: el joyero recibe la confirmación
en el idioma que acaba de elegir, sin tener que fiarse de un mensaje que no entiende.

**Independent Test**: Se abre Ajustes en un dispositivo en español, se toca cada una de las cinco
banderas y se comprueba que la pantalla visible cambia de idioma sin navegar ni reiniciar. Entrega
valor completo por sí sola.

**Acceptance Scenarios**:

1. **Given** la app en español y el joyero en Ajustes, **When** toca la bandera alemana, **Then** el
   título de la sección, el rótulo del idioma y las tres etiquetas de la barra inferior pasan a
   alemán sin que la pantalla se reinicie, se cierre ni pierda su posición.
2. **Given** el idioma alemán recién elegido, **When** el joyero vuelve al menú principal, **Then**
   los cuatro módulos, sus descripciones y la barra superior están en alemán.
3. **Given** cualquier idioma elegido, **When** el joyero observa la lista de Ajustes, **Then** la
   fila del idioma activo se distingue de las demás por una marca visible, y solo una fila la lleva.
4. **Given** la lista de idiomas, **When** el joyero la lee en cualquier idioma de la app, **Then**
   cada idioma aparece escrito en su propia lengua —«Español», «English», «Français», «Deutsch»,
   «Italiano»— acompañado de su bandera.
5. **Given** el idioma francés elegido, **When** el joyero vuelve a tocar la bandera francesa,
   **Then** nada cambia y la app no realiza trabajo visible ni pierde el estado de la pantalla.

---

### User Story 2 - Que la app hable el idioma del móvil sin configurar nada (Priority: P2)

Un joyero francés instala la app y la abre por primera vez. Desde la pantalla de inicio —el primer
texto que ve— la app está en francés, porque su móvil está en francés. Nunca ha entrado en Ajustes.

**Why this priority**: Es el primer contacto y decide si el joyero sigue o desinstala. Además cubre
al usuario que nunca abrirá Ajustes, que serán la mayoría: la app acierta sin pedirle nada.

**Independent Test**: Con la app recién instalada y sin ninguna elección previa, se arranca el
dispositivo en cada uno de los cinco idiomas y se comprueba que la portada aparece ya traducida; con
el dispositivo en un sexto idioma se comprueba que aparece en español.

**Acceptance Scenarios**:

1. **Given** un dispositivo en inglés y la app recién instalada, **When** el joyero la abre,
   **Then** la pantalla de inicio ya está en inglés, sin ningún paso previo de configuración.
2. **Given** un dispositivo en portugués —idioma no soportado— y la app recién instalada, **When**
   el joyero la abre, **Then** la app se muestra en español, y en Ajustes «Automático» aparece como
   la opción activa indicando español como idioma detectado.
3. **Given** un dispositivo en italiano y ninguna elección hecha, **When** el joyero abre Ajustes,
   **Then** la opción «Automático» está marcada como activa y muestra el idioma detectado del
   sistema; ninguna bandera aparece marcada.
4. **Given** un dispositivo cuyo idioma incluye variante regional (por ejemplo, español de México o
   inglés británico), **When** el joyero abre la app sin haber elegido nada, **Then** la app se
   muestra en el idioma correspondiente de los cinco soportados, sin tener en cuenta la región.

---

### User Story 3 - Que la elección mande y se pueda deshacer (Priority: P3)

El joyero eligió alemán hace semanas. Cambia el idioma de su móvil a inglés por trabajo, pero la app
sigue en alemán, que es lo que él pidió. Cuando quiere volver a que la app siga a su móvil, entra en
Ajustes y elige «Automático».

**Why this priority**: Es la regla de precedencia que da sentido a la feature: una elección explícita
vale más que una detección automática. Sin «Automático» la primera elección sería un camino sin
retorno, y el joyero que se equivoca de bandera se quedaría sin forma de volver al estado inicial.

**Independent Test**: Se elige un idioma, se cierra la app por completo y se vuelve a abrir; se
cambia el idioma del dispositivo y se comprueba que la app no se mueve; se elige «Automático» y se
comprueba que vuelve a seguir al dispositivo, también tras reiniciar la app.

**Acceptance Scenarios**:

1. **Given** el idioma alemán elegido en Ajustes, **When** el joyero cierra la app por completo y la
   vuelve a abrir, **Then** la portada y el resto de la app siguen en alemán.
2. **Given** el idioma alemán elegido y un dispositivo que pasa de español a inglés, **When** el
   joyero abre la app, **Then** sigue en alemán.
3. **Given** el idioma alemán elegido, **When** el joyero elige «Automático» y el dispositivo está en
   inglés, **Then** la app pasa a inglés al instante y «Automático» queda marcado.
4. **Given** «Automático» elegido explícitamente, **When** el joyero cierra la app, cambia el idioma
   del dispositivo a francés y la vuelve a abrir, **Then** la app se muestra en francés.
5. **Given** cualquier elección guardada, **When** el joyero desinstala y reinstala la app, **Then**
   la app vuelve a seguir al idioma del dispositivo, como en la primera instalación.

---

### User Story 4 - Leer toda la app en el idioma elegido, sin islas en español (Priority: P4)

El joyero alemán recorre la app entera: portada, menú, aleaciones de oro, aleaciones de plata,
soldaduras y su soldadura BASE, las dos herramientas y la pantalla de información. No encuentra ni
un texto en español, ni etiquetas cortadas, ni palabras que se salgan de su botón.

**Why this priority**: Una traducción parcial es peor que ninguna: el joyero deja de fiarse de las
cifras cuando el aviso legal que las acompaña está en un idioma que no entiende. Va al final porque
depende de que el mecanismo de las historias anteriores funcione, pero sin ella la feature no se
puede dar por hecha.

**Independent Test**: Con cada uno de los cinco idiomas activos se recorren las nueve pantallas de la
app comparando contra un inventario de textos visibles, y se revisa que ninguna etiqueta se corte ni
desborde su contenedor.

**Acceptance Scenarios**:

1. **Given** cualquiera de los cinco idiomas activo, **When** el joyero recorre las nueve pantallas,
   **Then** todos los textos visibles están en ese idioma, incluidos títulos, rótulos de sección,
   nombres de metales, etiquetas de botón, notas al pie, mensajes de error y avisos técnicos y de
   seguridad.
2. **Given** cualquier idioma activo, **When** una descripción de imagen se lee con el lector de
   pantalla, **Then** también está en ese idioma.
3. **Given** el idioma alemán, cuyas palabras son más largas, **When** el joyero mira la barra
   inferior, los botones de acción y las tarjetas del menú, **Then** ninguna etiqueta se corta, se
   parte a mitad de palabra ni desborda su contenedor.
4. **Given** cualquier idioma activo, **When** el joyero lee la marca de la app, los nombres de
   personas o empresas, los nombres de las redes sociales, el nombre del proveedor de cotizaciones o
   los símbolos de unidad, **Then** se muestran igual que en español.
5. **Given** cualquier idioma activo, **When** el joyero consulta la fecha y la hora de actualización
   de los precios, **Then** el nombre del mes aparece en el idioma elegido.
6. **Given** cualquier idioma activo, **When** el joyero lee un aviso que se refiere a la ley
   española de contraste, **Then** el aviso está traducido y sigue diciendo que se refiere a España.
7. **Given** cualquier idioma activo, **When** el joyero lee una cifra calculada, **Then** el
   separador decimal es la coma en los cinco idiomas, como hasta ahora.

---

### Edge Cases

- **Primer fotograma del arranque**: ¿qué ve el joyero en el instante entre abrir la app y saber cuál
  es el idioma guardado? No debe verse un destello de texto en el idioma equivocado.
- **Elección guardada ilegible o desconocida** (por ejemplo, una copia de seguridad de una versión
  futura con un sexto idioma): la app se comporta como si no hubiera elección y sigue al dispositivo.
- **Dispositivo con varios idiomas configurados por orden de preferencia**: manda el primero de la
  lista que esté entre los soportados.
- **Cambio de idioma con un formulario a medias**: el joyero ha escrito 25 gramos en la calculadora
  de plata y cambia de idioma; las cifras introducidas y los resultados no se pierden ni se alteran.
- **Cambio de idioma mientras hay una consulta de precios en curso**: la consulta no se reinicia ni
  se duplica, y el resultado aparece con sus rótulos en el idioma nuevo.
- **Restauración en un móvil nuevo**: el idioma elegido acompaña al joyero al restaurar la copia de
  seguridad de su dispositivo.
- **Tamaño de letra del sistema al máximo** combinado con el idioma más largo: las etiquetas siguen
  siendo legibles y no desbordan.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: La app MUST soportar cinco idiomas de interfaz: español, inglés, francés, alemán e
  italiano.
- **FR-002**: La pantalla de Ajustes MUST ofrecer, en una única lista, las cinco opciones de idioma
  más una opción «Automático» que sigue al idioma del dispositivo, y MUST reemplazar el andamiaje
  actual de esa pantalla.
- **FR-003**: Cada opción de idioma MUST mostrarse con su bandera y con el nombre del idioma escrito
  en ese mismo idioma, independientemente del idioma activo.
- **FR-004**: La lista MUST indicar visualmente cuál es la opción activa, y solo una opción MUST
  aparecer activa a la vez.
- **FR-005**: La opción «Automático» MUST indicar además qué idioma ha detectado del dispositivo.
- **FR-006**: Al elegir un idioma, todos los textos visibles MUST pasar a ese idioma de inmediato,
  incluidos los de la propia pantalla de Ajustes, sin reiniciar la app, sin cerrar ni volver a abrir
  ninguna pantalla y sin que el joyero tenga que navegar a otro sitio.
- **FR-007**: La elección MUST conservarse entre arranques de la app, incluido el cierre forzado.
- **FR-008**: Mientras no exista ninguna elección, la app MUST mostrarse en el idioma del
  dispositivo, y esa detección MUST estar ya aplicada en la primera pantalla que ve el joyero.
- **FR-009**: Si el idioma del dispositivo no es ninguno de los cinco soportados, la app MUST
  mostrarse en español.
- **FR-010**: La detección del idioma del dispositivo MUST ignorar la variante regional: cualquier
  variante de un idioma soportado MUST resolverse a ese idioma.
- **FR-011**: Una elección explícita MUST prevalecer sobre el idioma del dispositivo, incluso si este
  cambia después.
- **FR-012**: Elegir «Automático» MUST devolver el control al dispositivo, MUST aplicarse de
  inmediato y MUST conservarse entre arranques igual que cualquier otra elección.
- **FR-013**: La app MUST evitar mostrar texto en un idioma distinto del que corresponde mientras
  recupera la elección guardada al arrancar.
- **FR-014**: Una elección guardada que la app no reconozca MUST tratarse como ausencia de elección,
  sin fallo visible para el joyero.
- **FR-015**: Todos los textos visibles de la app MUST estar traducidos a los cinco idiomas: portada,
  menú principal, las dos barras de navegación, las cinco calculadoras (oro, plata, soldaduras,
  soldadura BASE, precio de metales y peso de chapas), la pantalla de información y las pantallas aún
  en construcción.
- **FR-016**: Las descripciones de imagen destinadas al lector de pantalla MUST traducirse también.
- **FR-017**: Los mensajes de error y los avisos técnicos y de seguridad MUST traducirse, y los
  avisos que se refieren a la ley española de contraste MUST seguir identificando España como su
  ámbito.
- **FR-018**: La marca «Calculadora de Joyeros», los nombres de personas y empresas, los nombres de
  las redes sociales, el nombre del proveedor de cotizaciones, los términos de mercado «Ask» y «Bid»
  y los símbolos de unidad MUST mostrarse igual en los cinco idiomas.
- **FR-019**: La fecha y la hora de actualización de los precios MUST mostrarse con el nombre del mes
  en el idioma activo.
- **FR-020**: El formato de las cifras calculadas —coma decimal y punto de miles— MUST permanecer
  igual en los cinco idiomas, sin cambios respecto al comportamiento actual.
- **FR-021**: Ningún texto traducido MUST cortarse, partirse a mitad de palabra ni desbordar su
  contenedor en ninguno de los cinco idiomas, incluido el tamaño de letra ampliado del sistema.
- **FR-022**: Cambiar de idioma MUST NOT perder ni alterar lo que el joyero haya introducido en un
  formulario, ni sus resultados en pantalla.
- **FR-023**: Cambiar de idioma MUST NOT provocar una consulta nueva al proveedor de cotizaciones ni
  invalidar los precios ya obtenidos.
- **FR-024**: La app MUST registrar en telemetría el cambio de idioma y la visita a Ajustes con
  identificadores estables que no dependan del idioma, conservando el nombre de pantalla que ya se
  venía registrando.
- **FR-025**: El idioma elegido MUST acompañar al joyero cuando restaure la copia de seguridad de su
  dispositivo en un móvil nuevo.
- **FR-026**: Ajustes MUST NOT incorporar ninguna otra opción de configuración en esta feature.

### Key Entities

- **Idioma soportado**: uno de los cinco idiomas de interfaz. Tiene un nombre propio para mostrar al
  joyero, una bandera y un identificador estable, independiente del idioma, para telemetría.
- **Elección de idioma**: lo que el joyero ha decidido en Ajustes. Puede estar ausente («Automático»,
  el estado inicial y también un estado elegible) o ser uno de los cinco idiomas. Sobrevive al cierre
  de la app y a la restauración del dispositivo.
- **Idioma del dispositivo**: el idioma que la app detecta del sistema, ya reducido a uno de los
  cinco soportados —español cuando no hay coincidencia—. Es dato de consulta: la app lo lee, nunca lo
  modifica.
- **Idioma efectivo**: el que la app está mostrando. Es la elección del joyero si existe y, si no, el
  idioma del dispositivo. Es lo único que la interfaz necesita saber para pintarse.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un joyero cuyo dispositivo está en uno de los cinco idiomas ve la app en su idioma
  desde el primer texto de la primera pantalla, sin haber configurado nada.
- **SC-002**: Cambiar de idioma le cuesta al joyero dos toques desde el menú principal (Ajustes y la
  bandera) y el resultado es visible en el mismo momento, sin esperas ni reinicios.
- **SC-003**: El 100 % de los textos visibles de la app aparece traducido en los cinco idiomas: un
  recorrido por las nueve pantallas en cada idioma no encuentra ni una cadena en otro idioma, salvo
  las excepciones de marca y símbolo declaradas en FR-018.
- **SC-004**: Ninguna etiqueta se corta ni desborda en ninguno de los cinco idiomas, comprobado en
  las nueve pantallas con el tamaño de letra por defecto del sistema y con el ampliado.
- **SC-005**: El idioma elegido sobrevive al cierre forzado de la app y a un cambio de idioma del
  dispositivo en el 100 % de los casos.
- **SC-006**: Un joyero que se equivoca de bandera puede volver al comportamiento inicial
  («Automático») desde la misma pantalla, sin desinstalar ni borrar datos.
- **SC-007**: La verificación de que no falta ninguna traducción es automática y falla antes de
  publicar, no en el móvil del joyero.

## Assumptions

- Los cinco idiomas del encargo son los del material de diseño entregado
  (`UI_Plantillas/Feature_Ajustes/`), y no se prevé un sexto en esta feature.
- El español es el idioma de partida y también el que se muestra cuando el dispositivo va en un
  idioma no soportado: es el idioma en el que están escritos los textos originales de la app.
- Las banderas identifican al idioma, no al país; se usan las cinco entregadas en el material de
  diseño. La bandera de España se muestra sin escudo, como es habitual en un selector de idioma.
- Los nombres de los idiomas se escriben siempre en su propia lengua, de modo que el joyero reconozca
  el suyo aunque la app esté en un idioma que no entiende.
- El sentido de lectura de los cinco idiomas es de izquierda a derecha: esta feature no aborda
  interfaces de derecha a izquierda.
- La traducción se realiza dentro de esta feature, sin proveedor externo de traducción. Los textos
  legales y de seguridad se traducen de forma literal, sin adaptarlos a otras jurisdicciones: siguen
  describiendo la normativa española.
- El formato numérico localizado (punto decimal en inglés) queda **fuera de alcance** por decisión
  del autor: alemán, francés e italiano comparten la coma decimal con el español, y cambiar el
  formato afecta a los cinco motores de cálculo y a sus pruebas. Se aborda en una feature aparte.
- La moneda sigue siendo el euro en los cinco idiomas: el proveedor de cotizaciones se consulta en
  euros y esta feature no toca precios.
- Favoritos sigue siendo una pantalla en construcción; solo se traduce su texto.
- El idioma elegido es un ajuste del dispositivo, no de una cuenta: no se sincroniza entre los
  dispositivos del joyero más allá de la copia de seguridad del sistema.
