# Feature Specification: Favoritos — guardar y reabrir cálculos

**Feature Branch**: `009-favoritos`

**Created**: 2026-08-26

**Status**: Draft

**Input**: User description: "Favoritos: guardar y reabrir cálculos. Las cinco calculadoras de la app (aleaciones de oro, aleaciones de plata, soldaduras, soldadura BASE y peso de chapas) ya tienen un botón «Guardar en favoritos» que hoy solo muestra un aviso de «Próximamente». Quiero que guarde de verdad el cálculo que el joyero tiene en pantalla, y que la pestaña Favoritos —hoy una pantalla en construcción— se convierta en el listado de lo guardado. Cada favorito se presenta como una tarjeta con la imagen de su sección (para que se reconozca de un vistazo), el nombre de la sección y los datos: lo que el joyero introdujo y lo que la app le respondió. Por ejemplo, una aleación de 30 gramos de oro de 18 K en oro blanco muestra la plata, el cobre y el paladio que hacen falta y el peso total resultante; lo mismo con las soldaduras y con el peso de una chapa. Pulsar una tarjeta lleva a la calculadora correspondiente con esos datos ya puestos, listos para repetir el cálculo o para retocarlos; el favorito guardado no cambia por editarlo allí. Cada tarjeta lleva una estrella que lo quita de favoritos, y quitarlo pide confirmación antes de hacerlo. Detalles ya decididos: el título de cada tarjeta lo compone la app con los propios datos, sin pedirle un nombre al joyero; guardar dos veces el mismo cálculo no crea una entrada repetida, sino que avisa de que ya estaba; el listado va del más reciente al más antiguo; y una tarjeta muestra como mucho tres líneas de resultado, indicando cuántas quedan por ver cuando hay más. Los favoritos deben sobrevivir al cierre de la app y acompañar al joyero a un móvil nuevo, porque son una decisión suya y no un dato derivado. Lo guardado son las entradas del cálculo; los resultados se rehacen al mostrarlos, para que se vean siempre en el idioma elegido y con el redondeo propio de cada calculadora. Fuera de alcance en esta feature: renombrar favoritos, ordenarlos o filtrarlos a mano, agruparlos por sección, exportarlos o compartirlos, y guardar favoritos desde la pantalla de precio de metales (no es un cálculo del joyero, es una cotización con fecha)."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Guardar el cálculo que tengo delante (Priority: P1)

El joyero está preparando una aleación en el taller. Ha teclateado 30 gramos, ha elegido 18 K y
oro blanco, y la app le ha dicho cuánta plata, cuánto cobre y cuánto paladio necesita. Es una
mezcla que hace a menudo, así que pulsa «Guardar en favoritos». La app le confirma que queda
guardado, y desde ese momento lo encuentra en la pestaña Favoritos: al cerrar la app y volver
al día siguiente, sigue ahí.

**Why this priority**: Es la mitad de la feature que hoy está engañando al joyero. El botón
existe en cinco pantallas desde hace cinco versiones y lo único que hace es decir
«Próximamente». Sin esto no hay nada que listar, nada que reabrir y nada que borrar: es el
MVP y el resto de historias no tienen sentido sin él.

**Independent Test**: Se prueba de punta a punta guardando un cálculo en cada una de las cinco
calculadoras, cerrando la app por completo y comprobando que la pestaña Favoritos los muestra
todos. No hace falta que la reapertura ni el borrado existan todavía.

**Acceptance Scenarios**:

1. **Given** una aleación de oro válida en pantalla, **When** el joyero pulsa «Guardar en
   favoritos», **Then** la app le confirma que queda guardado y el cálculo aparece en la pestaña
   Favoritos.
2. **Given** un cálculo guardado, **When** el joyero cierra la app por completo y la vuelve a
   abrir, **Then** el favorito sigue en la lista con los mismos datos.
3. **Given** un cálculo ya guardado, **When** el joyero vuelve a pulsar «Guardar en favoritos»
   con exactamente las mismas entradas, **Then** la app le avisa de que ya estaba en favoritos y
   la lista sigue teniendo una sola entrada.
4. **Given** una calculadora con el campo de cantidad vacío o con un valor que no sirve,
   **When** el joyero pulsa «Guardar en favoritos», **Then** la app le dice que complete el
   cálculo antes de guardar y no añade nada a la lista.
5. **Given** cálculos guardados desde las cinco calculadoras, **When** el joyero abre la pestaña
   Favoritos, **Then** los ve todos en una sola lista, del más reciente al más antiguo.
6. **Given** dos cálculos que solo se diferencian en un dato —la misma cantidad y ley pero otro
   color, o el mismo peso en el modo directo y en el inverso—, **When** el joyero los guarda los
   dos, **Then** la lista tiene dos entradas distintas.

---

### User Story 2 - Volver a un cálculo guardado y seguir trabajando (Priority: P2)

El joyero necesita repetir una soldadura que guardó la semana pasada. Abre Favoritos, pulsa su
tarjeta y aterriza en la calculadora de soldaduras con la familia, la dureza, el color, el modo
y la cantidad ya puestos, y el resultado calculado. Desde ahí puede simplemente leerlo, o
cambiar la cantidad porque esta vez necesita el doble: lo que retoque no altera el favorito
guardado, que sigue esperándole tal como lo dejó.

**Why this priority**: Es la razón de ser de la lista. Sin esto Favoritos es un archivo de
consulta; con esto es un atajo que ahorra teclear.

**Independent Test**: Se prueba guardando un cálculo de cada tipo, abriéndolo desde la lista y
comprobando que la calculadora llega con todos los datos puestos, que se pueden editar y que al
volver a Favoritos la tarjeta no ha cambiado.

**Acceptance Scenarios**:

1. **Given** un favorito de cada una de las cinco calculadoras, **When** el joyero pulsa su
   tarjeta, **Then** la app abre la calculadora que le corresponde con todas las entradas
   guardadas puestas y el resultado ya calculado.
2. **Given** una calculadora abierta desde un favorito, **When** el joyero cambia una entrada,
   **Then** el resultado se recalcula y el favorito guardado no cambia.
3. **Given** una calculadora abierta desde un favorito y editada, **When** el joyero vuelve
   atrás, **Then** regresa a la lista de Favoritos y la tarjeta muestra los datos originales.
4. **Given** una calculadora abierta desde un favorito y editada, **When** el joyero pulsa
   «Guardar en favoritos», **Then** la variante se guarda como un favorito nuevo y el original
   se conserva.
5. **Given** un favorito de peso de chapas, **When** el joyero pulsa su tarjeta, **Then** la app
   abre Herramientas con la sub-herramienta de peso de chapas ya elegida y las medidas puestas.
6. **Given** una calculadora abierta desde un favorito y con algo editado, **When** el sistema
   la recompone por un cambio de tamaño de letra o de tema, **Then** lo editado sigue en
   pantalla y no vuelve a los valores del favorito.

---

### User Story 3 - Quitar de la lista lo que ya no sirve (Priority: P3)

La lista del joyero ha crecido y hay cálculos de una pieza que ya entregó. Pulsa la estrella de
esa tarjeta y la app le pregunta si de verdad quiere quitarlo, nombrando cuál. Confirma, la
tarjeta desaparece y las demás siguen intactas. Si se ha equivocado al pulsar, cancela y no pasa
nada.

**Why this priority**: Una lista que solo crece acaba siendo inservible, pero la app funciona sin
esto mientras la lista sea corta. Va después de guardar y de reabrir.

**Independent Test**: Se prueba con varios favoritos guardados: quitar uno, comprobar que
desaparece y que los demás siguen; y pulsar la estrella y cancelar, comprobando que no se pierde
nada.

**Acceptance Scenarios**:

1. **Given** una lista con varios favoritos, **When** el joyero pulsa la estrella de una tarjeta,
   **Then** la app le pide confirmación antes de quitar nada, nombrando el favorito afectado.
2. **Given** la pregunta de confirmación en pantalla, **When** el joyero confirma, **Then** ese
   favorito desaparece de la lista y los demás siguen tal cual.
3. **Given** la pregunta de confirmación en pantalla, **When** el joyero cancela o descarta la
   pregunta, **Then** no se quita nada.
4. **Given** un favorito quitado, **When** el joyero cierra la app y la vuelve a abrir,
   **Then** ese favorito sigue sin estar.
5. **Given** un favorito quitado, **When** el joyero repite exactamente el mismo cálculo y lo
   guarda, **Then** vuelve a entrar en la lista como si fuera nuevo.
6. **Given** una lista de la que se ha quitado el último favorito, **When** la lista se queda
   vacía, **Then** la app muestra la misma invitación que ve un joyero que aún no ha guardado nada.

---

### User Story 4 - Reconocer cada favorito de un vistazo (Priority: P4)

El joyero tiene doce favoritos de secciones distintas. No quiere leer: quiere reconocer. Cada
tarjeta lleva la imagen de su sección y su nombre, de modo que el oro, la plata, las soldaduras
y las chapas se distinguen sin esfuerzo, y debajo los datos del cálculo: lo que introdujo y lo
que la app le respondió, con el peso total destacado y la fecha en que lo guardó.

**Why this priority**: La lista es utilizable con títulos a secas; la identificación visual es
lo que la hace rápida. Es refinamiento sobre una función que ya sirve.

**Independent Test**: Se prueba con un favorito de cada tipo en la lista, comprobando que cada
tarjeta lleva su imagen, su nombre de sección, un título que resume las entradas, las cifras del
resultado, el total y la fecha.

**Acceptance Scenarios**:

1. **Given** favoritos de secciones distintas, **When** el joyero mira la lista, **Then** cada
   tarjeta lleva la imagen y el nombre de su sección.
2. **Given** un favorito de aleación de oro de 30 gramos, 18 K, blanco, **When** el joyero mira
   su tarjeta, **Then** lee un título que resume esas entradas y las cifras de plata, cobre y
   paladio, más el peso total.
3. **Given** un favorito cuyo resultado tiene más de tres cifras, **When** el joyero mira su
   tarjeta, **Then** ve las tres primeras y una indicación de cuántas quedan por ver.
4. **Given** favoritos guardados en momentos distintos, **When** el joyero mira la lista,
   **Then** cada tarjeta muestra la fecha en que se guardó, en el formato del idioma de la app.
5. **Given** la app en otro idioma, **When** el joyero mira la lista, **Then** los nombres de
   sección, los títulos y los nombres de metal están en el idioma elegido, incluidos los
   favoritos que guardó cuando la app estaba en español.

---

### Edge Cases

- **Guardar con el cálculo incompleto**: el botón está siempre disponible, así que pulsarlo con
  el campo vacío tiene que decir algo. El silencio se lee como una app rota.
- **Guardar dos veces lo mismo**: no se crea una segunda entrada. Dos entradas se consideran el
  mismo cálculo cuando **todas** sus entradas coinciden, incluido el modo de cálculo (partir del
  metal que se tiene o del peso final que se quiere) y el orden de las medidas de una chapa: una
  chapa de 10 × 2 × 30 mm y otra de 30 × 2 × 10 mm pesan lo mismo pero no son el mismo favorito.
- **Cantidades escritas de forma distinta**: «30», «30,0» y «30.0» son el mismo cálculo y no
  pueden producir dos favoritos.
- **Abrir un favorito con la calculadora a medias**: los datos del favorito sustituyen lo que
  hubiera en el formulario. Es lo que el joyero pide al pulsar la tarjeta.
- **Abrir un favorito que ya no existe** (quitado mientras estaba a la vista): la calculadora se
  abre vacía, como si se hubiera entrado desde el menú. Sin mensaje de error: no es un fallo.
- **Lista vacía**: la primera visita muestra una invitación que nombra el botón con el que se
  guarda, no una pantalla en blanco.
- **Un favorito de una versión más nueva de la app** (el joyero volvió a una versión anterior):
  no se muestra, y tampoco se destruye; le espera intacto si vuelve a la versión nueva.
- **Cambio de idioma con la lista abierta**: la lista se repinta en el idioma nuevo sin perder
  nada y sin volver al principio.
- **Muchos favoritos**: la lista sigue siendo utilizable y no hay número máximo impuesto.
- **La hora del móvil cambiada hacia atrás**: el orden de la lista sigue siendo el orden real en
  que se guardaron.
- **Una receta o una densidad que cambie en una versión futura**: los favoritos viejos mostrarán
  las cifras nuevas, porque lo que se guarda es el cálculo, no su resultado.

## Requirements *(mandatory)*

### Functional Requirements

**Guardar**

- **FR-001**: El botón «Guardar en favoritos» de las cinco calculadoras (aleaciones de oro,
  aleaciones de plata, soldaduras, soldadura BASE y peso de chapas) MUST guardar el cálculo que
  hay en pantalla, y MUST dejar de mostrar el aviso de «Próximamente».
- **FR-002**: Lo que se guarda MUST ser únicamente las entradas del cálculo: la cantidad y todas
  las opciones elegidas (ley, color, familia, dureza, tipo, material, medidas y modo de cálculo).
- **FR-003**: El sistema MUST NOT guardar los resultados. Las cifras de una tarjeta MUST
  recalcularse a partir de las entradas cada vez que se muestran.
- **FR-004**: Al guardar, el sistema MUST confirmar al joyero que el cálculo ha quedado guardado.
- **FR-005**: Si el cálculo de la pantalla no es válido o está incompleto, el sistema MUST NOT
  guardar nada y MUST avisar al joyero de que complete el cálculo.
- **FR-006**: El sistema MUST NOT crear una segunda entrada cuando el joyero guarda un cálculo
  cuyas entradas coinciden todas con las de un favorito existente, y MUST avisarle de que ya
  estaba en favoritos.
- **FR-007**: Dos cálculos MUST considerarse el mismo favorito sólo si coinciden **todas** sus
  entradas, incluidos el modo de cálculo y el papel de cada medida. Cantidades escritas de forma
  distinta pero de igual valor numérico MUST considerarse la misma cantidad.
- **FR-008**: El sistema MUST NOT pedir al joyero un nombre para el favorito.
- **FR-009**: Pulsar el botón dos veces seguidas MUST NOT producir dos favoritos.
- **FR-010**: El sistema MUST NOT ofrecer guardar favoritos desde la pantalla de precio de
  metales.

**Listar**

- **FR-011**: La pestaña Favoritos MUST mostrar los cálculos guardados como un listado de
  tarjetas, y MUST dejar de ser una pantalla en construcción.
- **FR-012**: El listado MUST ordenarse del más reciente al más antiguo, y ese orden MUST ser el
  orden real en que se guardaron aunque la hora del dispositivo haya cambiado.
- **FR-013**: Cada tarjeta MUST mostrar la imagen de su sección, el nombre de su sección, un
  título que resuma las entradas del cálculo, las cifras del resultado, el peso total y la fecha
  en que se guardó.
- **FR-014**: El título de cada tarjeta MUST componerlo la app a partir de las entradas
  guardadas, y MUST leerse en el idioma que el joyero tenga elegido.
- **FR-015**: Cada tarjeta MUST mostrar como máximo tres cifras de resultado, y cuando el
  cálculo produzca más MUST indicar cuántas quedan por ver.
- **FR-016**: La fecha de cada tarjeta MUST mostrarse con el formato del idioma elegido en la
  app, no el del sistema.
- **FR-017**: Cuando no haya ningún favorito, la pantalla MUST mostrar una invitación que nombre
  el botón con el que se guardan, y MUST NOT mostrar un listado vacío sin explicación.
- **FR-018**: La pantalla MUST NOT mostrar ni el listado ni la invitación mientras aún no sabe si
  hay favoritos guardados, para que la invitación no aparezca y desaparezca en cada visita.
- **FR-019**: Todo el texto de la pantalla y de las tarjetas MUST estar disponible en los cinco
  idiomas de la app.

**Reabrir**

- **FR-020**: Pulsar una tarjeta MUST abrir la calculadora que le corresponde con todas las
  entradas guardadas puestas y el resultado ya calculado.
- **FR-021**: Un favorito de peso de chapas MUST abrir Herramientas con esa sub-herramienta ya
  elegida.
- **FR-022**: La calculadora abierta desde un favorito MUST quedar editable, y editarla MUST NOT
  modificar el favorito guardado.
- **FR-023**: Guardar desde una calculadora abierta y editada MUST crear un favorito nuevo y
  MUST conservar el original.
- **FR-024**: Volver atrás desde una calculadora abierta desde un favorito MUST devolver al
  listado de Favoritos.
- **FR-025**: Si el sistema recompone la pantalla (cambio de tamaño de letra, de tema o de idioma
  del dispositivo), la app MUST NOT volver a poner los datos del favorito encima de lo que el
  joyero llevara editado.
- **FR-026**: Abrir un favorito que ya no existe MUST dejar la calculadora en su estado inicial,
  sin mensaje de error.

**Quitar**

- **FR-027**: Cada tarjeta MUST ofrecer una estrella que quita ese cálculo de favoritos.
- **FR-028**: Quitar un favorito MUST pedir confirmación antes de hacerlo, y la pregunta MUST
  nombrar el favorito afectado.
- **FR-029**: Cancelar o descartar la confirmación MUST NOT quitar nada.
- **FR-030**: Al confirmar, el favorito MUST desaparecer del listado de inmediato y los demás
  MUST quedar intactos.
- **FR-031**: Un favorito quitado MUST poder volver a guardarse repitiendo el cálculo.

**Persistencia**

- **FR-032**: Los favoritos MUST sobrevivir al cierre de la app y al reinicio del dispositivo.
- **FR-033**: Los favoritos MUST viajar en la copia de seguridad del dispositivo y en la
  transferencia a un móvil nuevo: son una decisión del joyero, no un dato derivado.
- **FR-034**: Un favorito guardado por una versión más reciente de la app que esta no entienda
  MUST NOT mostrarse y MUST NOT destruirse.
- **FR-035**: El sistema MUST NOT borrar favoritos por su cuenta, ni por antigüedad ni por
  número, ni al actualizar la app.
- **FR-036**: El sistema MUST NOT registrar en telemetría las cantidades ni las medidas que el
  joyero introduce; sólo el tipo de cálculo.

### Key Entities

- **Favorito**: un cálculo que el joyero ha decidido conservar. Se identifica por sus entradas y
  guarda además cuándo se guardó. No contiene ni resultado ni nombre puesto a mano.
- **Entradas del cálculo**: la cantidad y las opciones que definen el cálculo, distintas en cada
  calculadora: ley y color en el oro; ley en la plata; familia, tipo o dureza, color y modo en
  las soldaduras; cantidad y modo en la soldadura BASE; material y las tres medidas en las
  chapas. Son lo único que se guarda y lo único que decide si dos favoritos son el mismo.
- **Resumen del favorito**: las cifras que el cálculo produce (los metales o ingredientes con su
  peso, y el total). Se rehacen a partir de las entradas cada vez que se muestran; no se guardan.
- **Sección**: la calculadora de la que nació el favorito. Le da la imagen, el nombre y el color
  con el que se reconoce en el listado, y decide a dónde lleva la tarjeta al pulsarla.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El joyero guarda el cálculo que tiene en pantalla con **una sola pulsación**, sin
  escribir nada y sin pasos intermedios.
- **SC-002**: El joyero recupera un cálculo guardado en **dos pulsaciones** desde cualquier zona
  principal de la app (pestaña Favoritos, tarjeta), frente a las seis o siete que cuesta hoy
  volver a introducirlo a mano.
- **SC-003**: El 100 % de los cálculos guardados sigue disponible tras cerrar la app, reiniciar
  el dispositivo y cambiar de móvil restaurando la copia de seguridad.
- **SC-004**: Guardar el mismo cálculo N veces produce **exactamente una** entrada en el listado.
- **SC-005**: Ningún favorito se pierde sin que el joyero lo haya confirmado explícitamente.
- **SC-006**: Las cifras que muestra una tarjeta coinciden **exactamente**, dígito a dígito, con
  las que muestra la calculadora de la que nació el favorito, con las mismas entradas.
- **SC-007**: Con la app en cualquiera de los cinco idiomas, ninguna tarjeta muestra texto en un
  idioma distinto del elegido, incluidos los favoritos guardados con la app en otro idioma.
- **SC-008**: Con un lector de pantalla activo, el joyero puede recorrer el listado, abrir un
  favorito y quitarlo.
- **SC-009**: Con al menos 50 favoritos guardados, el listado aparece completo en menos de un
  segundo desde que el joyero pulsa la pestaña, y se desplaza sin tirones.

## Assumptions

- **La cantidad guardada es la que el joyero introdujo, no una redondeada**: al reabrir un
  favorito, el campo muestra el mismo número que se teclateó.
- **Cada calculadora conserva su propio redondeo al mostrar las cifras en la tarjeta**: el oro y
  las soldaduras a la media, la plata truncando (la ley resultante no puede quedar por debajo de
  la objetivo) y el peso de una chapa con dos decimales. Una tarjeta no puede mostrar una cifra
  distinta de la que muestra su calculadora.
- **El formato de las cifras no se localiza** en esta feature, igual que en el resto de la app:
  la coma decimal es determinista. Es deuda conocida de una feature aparte.
- **Los resultados son dato derivado, con una consecuencia asumida**: si una versión futura
  corrige una receta de soldadura o una densidad de chapa, los favoritos guardados antes
  mostrarán las cifras nuevas. Se prefiere eso a congelar un resultado en el idioma y las cifras
  del día en que se guardó: un favorito es una receta, no un recibo.
- **La soldadura BASE cuenta como sección propia** a efectos del listado, porque tiene pantalla
  propia y es a donde debe volver su favorito.
- **No hay límite de favoritos** en esta versión. Si un día apareciera un problema de volumen se
  resolvería mostrando la lista por partes, nunca descartando favoritos del joyero.
- **La pantalla en construcción desaparece**: Favoritos era su último destino, así que el
  andamio de «Pantalla en construcción» y el aviso de «Próximamente» dejan de existir en la app.

### Fuera de alcance en esta feature

- **Poner nombre a un favorito** o renombrarlo después. El título lo compone la app.
- **Ordenar, filtrar o buscar** en el listado a mano, y **agruparlo por sección**. El orden es
  siempre del más reciente al más antiguo.
- **Exportar, compartir o imprimir** favoritos, y **sincronizarlos** entre dispositivos por
  cuenta de usuario. La copia de seguridad del dispositivo es el único traslado previsto.
- **Guardar favoritos desde la pantalla de precio de metales**: no es un cálculo del joyero, es
  una cotización con fecha, y un favorito de un precio caducaría en una hora.
- **Que el botón de la calculadora indique si el cálculo actual ya es favorito**. Sería mejor
  experiencia, pero obliga a la calculadora a vigilar la lista completa con cada tecla.
- **Deshacer un borrado**. La confirmación previa cubre el error de pulsación.
