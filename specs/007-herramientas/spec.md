# Feature Specification: Herramientas — precio de metales y peso de chapas

**Feature Branch**: `007-herramientas`

**Created**: 2026-08-25

**Status**: Draft

**Input**: User description: "Módulo «Herramientas» del menú principal, con dos sub-herramientas: «Precio metales» y «Peso de chapas». Al entrar en Herramientas se ven solo las dos opciones excluyentes y una invitación a elegir; el contenido aparece al pulsar una de ellas y la elección se conserva mientras el joyero está en la pantalla. PRECIO METALES: cotización en euros de oro, plata, cobre, paladio y rodio, cada uno con nombre, símbolo químico, imagen, precio en la unidad elegida y flecha de tendencia (sube/baja). Un selector de unidad convierte todos los precios a gramo (por defecto), kilo u onza troy. Al pulsar un metal, la tarjeta «Información del mercado» muestra su ask, bid, máximo, mínimo, variación, variación en %, unidad y fecha-hora de actualización (hora local). El precio principal es el precio medio del mercado (mid). Los datos se obtienen de un proveedor externo (Metal Sentinel, vía RapidAPI) al abrir la sección y se conservan durante una hora, también si se cierra la app: reabrir la sección dentro de esa hora no consulta al proveedor (una carga completa son cinco consultas, una por metal, y la cuota mensual es limitada). Si un metal falla, se muestran los demás y el motivo del fallo; si hay un dato anterior se muestra marcado como desactualizado. Sin conexión con la caché caducada se muestra lo último conocido con su fecha y un aviso. Solo hay «Reintentar» cuando hay errores, y un reintento inmediato tras un fallo espera un minuto (cinco tras superar el límite de consultas). Nota «Precios orientativos. Pueden variar según el mercado.» y «Fuente: Metal Sentinel». Formato: importes con dos decimales, cuatro si son menores que uno (el cobre por gramo es muy pequeño), punto de miles, coma decimal. PESO DE CHAPAS: el joyero elige la familia ORO (18K, 14K, 12K, 9K; en dorado) o PLATA (950, 925, 900, 800; en turquesa, las mismas leyes que el resto de la app), introduce ancho, espesor y largo en milímetros (coma o punto) y ve al instante, sin botón de calcular y solo cuando las tres medidas son válidas, el peso de la chapa en gramos con dos decimales, el volumen, la densidad aplicada, la pureza y el metal fino; encima, una ilustración isométrica de la chapa que se va construyendo y se redibuja con las medidas introducidas, sus cotas y el color del metal elegido. Límites operativos: ancho y largo hasta 10 000 mm, espesor hasta 1 000 mm; fuera de rango se avisa y no hay resultado. Nota «Valores aproximados. Considera merma según tu proceso.» Ni 12K ni 950 ni 900 se presentan como ley oficial española (se reutilizan los avisos existentes). Botones «Limpiar» y «Guardar en favoritos» como en el resto de calculadoras (favoritos aún es «Próximamente»). Fuentes de verdad: UI_Plantillas/Feature_Herramientas/Especificacion_Calculadora_Peso_Chapas_Oro_Plata.md (fórmula peso = ancho × largo × espesor × densidad / 1000, densidades de §5.1, casos de §7 y §21: 10 × 20 × 0,5 mm en oro 18K = 1,558 g mostrado como 1,56 g; los números del mockup de chapas NO son fuente), UI_Plantillas/Feature_Herramientas/Guia_API_Precios_Metales_Android_Kotlin_Compose.md, y la respuesta real del proveedor (campos symbol, currency, ask, mid, bid, high, low, timestamp, change, changePercentage, unit=OUNCE). Mockups: «Screen para precio metales.png» y «Screen_peso_chapas.png»; imagen rodio.png. Decisiones ya confirmadas con el autor: una sola feature con ambas sub-herramientas; consulta directa al proveedor desde la app asumida como prototipo (la cuota gratuita no da para una app pública, un backend propio será una feature aparte); precio principal = mid; dos decimales en el peso de la chapa; primera visita sin sub-herramienta elegida; orden de secciones de chapas el del mockup (ilustración, material, medidas, resultado, botones); ORO dorado y PLATA turquesa. Fuera de alcance: cálculo inverso de chapa, merma, densidades personalizables, coste estimado, históricos, backend."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Entrar en Herramientas y consultar el precio de los metales (Priority: P1)

Un joyero entra en el módulo de Herramientas y, la primera vez, solo ve dos opciones excluyentes —PRECIO METALES y PESO DE CHAPAS— con una invitación a elegir. Pulsa PRECIO METALES y, tras una breve carga, ve la cotización del día de los cinco metales de su oficio —oro, plata, cobre, paladio y rodio— con nombre, símbolo químico, imagen, precio en euros por gramo y una flecha que indica si el metal sube o baja. Debajo lee que los precios son orientativos, la fuente y la hora de la última actualización. Si vuelve a entrar más tarde, dentro de la misma hora, ve los mismos precios al instante: la app no vuelve a molestar al proveedor.

**Why this priority**: Es la herramienta que da razón de ser al módulo y la única que depende de un servicio externo con cuota limitada; sin ella Herramientas seguiría siendo andamiaje. La caché de una hora es parte del valor, no un detalle: protege la cuota del proveedor y hace la pantalla instantánea.

**Independent Test**: Se abre Herramientas, se elige PRECIO METALES y se comprueba que aparecen los cinco precios; se sale, se cierra la app y se vuelve a entrar dentro de la hora comprobando que los precios son los mismos, que la hora no cambia y que el proveedor no registra consultas nuevas. Entrega valor completo por sí sola.

**Acceptance Scenarios**:

1. **Given** la primera visita al módulo, **When** el joyero observa la pantalla, **Then** solo ve las dos opciones del selector, ninguna marcada, y una invitación a elegir; no hay precios, formularios ni botones de acción.
2. **Given** el selector, **When** el joyero pulsa PRECIO METALES, **Then** ve un indicador de carga y después la lista de los cinco metales en el orden oro, plata, cobre, paladio y rodio, cada uno con nombre, símbolo químico, imagen, precio en euros por gramo y flecha de tendencia.
3. **Given** un precio de 4.606,40 € por onza troy para el oro, **When** el joyero lee la lista en gramos, **Then** ve 148,10 €/g; **Given** el cobre por debajo de 1 €/g, **Then** su precio se muestra con cuatro decimales.
4. **Given** precios cargados hace menos de una hora, **When** el joyero sale del módulo, cierra la app y vuelve a PRECIO METALES, **Then** ve los mismos precios al instante, con la misma hora de actualización, y el proveedor no recibe ninguna consulta nueva.
5. **Given** precios cargados hace más de una hora, **When** el joyero entra en PRECIO METALES, **Then** la app consulta de nuevo al proveedor y la hora de actualización cambia.
6. **Given** la lista de precios, **When** el joyero lee el pie, **Then** ve «Precios orientativos. Pueden variar según el mercado.», «Fuente: Metal Sentinel» y la fecha y hora de la última actualización en su hora local.
7. **Given** PRECIO METALES abierto, **When** el joyero cambia a PESO DE CHAPAS y vuelve a PRECIO METALES sin salir del módulo, **Then** los precios siguen ahí sin consulta nueva y la opción elegida aparece marcada.

---

### User Story 2 - Cambiar la unidad y consultar la información del mercado (Priority: P2)

Con los precios en pantalla, el joyero cambia la unidad entre gramo, kilo y onza troy y ve todos los precios convertidos. Pulsa un metal y la tarjeta «Información del mercado» le muestra el detalle de ese metal: precio de venta (ask), precio de compra (bid), máximo y mínimo de la sesión, variación absoluta y en porcentaje, la unidad elegida y la fecha y hora del dato.

**Why this priority**: El proveedor cotiza por onza troy y el joyero trabaja en gramos; sin conversión la cifra no le sirve. El detalle del mercado es lo que distingue una lista de precios de una herramienta profesional.

**Independent Test**: Con precios cargados, se recorren las tres unidades comprobando las conversiones y se pulsa cada metal comprobando que la tarjeta cambia.

**Acceptance Scenarios**:

1. **Given** la lista en gramos con el oro a 148,10 €/g, **When** el joyero elige kilo, **Then** los cinco precios cambian a euros por kilo —el oro a 148.099,20 €/kg— y la unidad visible pasa a «€/kg».
2. **Given** la lista, **When** el joyero elige onza troy, **Then** ve la cifra tal y como cotiza el proveedor —el oro a 4.606,40 €/oz— y al volver a gramo recupera 148,10 €/g.
3. **Given** la lista con la tarjeta de mercado mostrando el oro por defecto, **When** el joyero pulsa PLATA, **Then** la fila de plata queda marcada y la tarjeta muestra «Plata (AG)» con su ask, bid, máximo, mínimo, variación, variación %, unidad y actualización.
4. **Given** la tarjeta de mercado en gramos, **When** el joyero cambia a kilo, **Then** ask, bid, máximo, mínimo y variación se convierten a kilo y la variación en porcentaje no cambia.
5. **Given** un metal cuya unidad de cotización el proveedor no confirma, **When** el joyero lo lee, **Then** ve el precio original con la unidad que indica el proveedor, sin convertir, y el selector de unidad no lo altera.
6. **Given** una variación negativa, **When** el joyero lee la fila y la tarjeta, **Then** la flecha y la variación se muestran como bajada; positiva, como subida; nula, como sin cambio.

---

### User Story 3 - Calcular el peso de una chapa (Priority: P3)

El joyero elige PESO DE CHAPAS, escoge la familia ORO o PLATA y su ley, e introduce el ancho, el espesor y el largo de la chapa en milímetros. En cuanto las tres medidas son válidas, sin pulsar ningún botón, ve el peso de la chapa en gramos junto con el volumen, la densidad aplicada, la pureza y el metal fino que contiene. Cambiar de ley o de familia recalcula al instante conservando las medidas.

**Why this priority**: Es la segunda herramienta del encargo y funciona sin red ni proveedor: aporta valor de taller por sí sola.

**Independent Test**: Se elige PESO DE CHAPAS, se introducen 10 × 0,5 × 20 mm y se recorren los ocho materiales comprobando los pesos contra la tabla del documento técnico.

**Acceptance Scenarios**:

1. **Given** PESO DE CHAPAS recién abierto, **When** el joyero observa la pantalla, **Then** ve la familia ORO con 18K preseleccionados, los tres campos vacíos, la ilustración de una chapa de referencia y ningún resultado.
2. **Given** ORO 18K, **When** el joyero introduce ancho 10, espesor 0,5 y largo 20, **Then** ve un peso de 1,56 g, «Calculado para Oro 18 K», volumen 0,100 cm³, densidad 15,58 g/cm³, pureza 75,0 % (18 K) y oro fino 1,169 g.
3. **Given** las mismas medidas, **When** el joyero elige 14K, 12K y 9K, **Then** ve 1,31 g, 1,28 g y 1,12 g respectivamente.
4. **Given** las mismas medidas en ORO, **When** el joyero elige PLATA, **Then** el selector de ley pasa a 950, 925, 900 y 800 con 925 preseleccionada, el acento de la sección cambia a turquesa, las medidas se conservan y el peso pasa a 1,04 g con 0,958 g de plata fina.
5. **Given** PLATA con las mismas medidas, **When** el joyero elige 950, 900 y 800, **Then** ve 1,04 g, 1,03 g y 1,01 g respectivamente.
6. **Given** solo dos medidas válidas, **When** el joyero observa la pantalla, **Then** no hay resultado ni mensaje alarmante; **When** completa la tercera, **Then** el resultado aparece sin ninguna acción adicional.
7. **Given** las medidas escritas con coma o con punto («0,5» y «0.5»), **When** se calcula, **Then** el resultado es idéntico.
8. **Given** un ancho o largo mayor de 10 000 mm, o un espesor mayor de 1 000 mm, **When** el joyero observa la pantalla, **Then** el campo queda marcado, un aviso explica el rango operativo y no hay resultado.
9. **Given** 12K, 950 o 900 elegidos, **When** el joyero observa la pantalla, **Then** ve el aviso de ley técnica ya existente en la app; con 18K, 14K, 9K, 925 u 800 no hay aviso.
10. **Given** cualquier resultado, **When** el joyero lee el pie, **Then** ve «Valores aproximados. Considera merma según tu proceso.».

---

### User Story 4 - Ver la chapa construirse con sus medidas (Priority: P4)

Encima del formulario, una ilustración isométrica de la chapa se dibuja al entrar —primero el contorno, luego las caras, por último las cotas— y a partir de ahí se redibuja con cada medida que el joyero teclea: el ancho estira la arista frontal, el espesor el canto, el largo la arista en fuga, y cada cota muestra el valor introducido en milímetros. La chapa toma el color del metal elegido.

**Why this priority**: Es el efecto que el autor pide expresamente como seña de calidad de la pantalla y como ayuda para no confundir las tres medidas; no altera ningún cálculo.

**Independent Test**: Se abre PESO DE CHAPAS, se observa la construcción inicial, se teclean medidas distintas comprobando que la chapa y sus cotas cambian, y se cambia de familia comprobando el color.

**Acceptance Scenarios**:

1. **Given** PESO DE CHAPAS recién abierto, **When** el joyero observa la ilustración, **Then** la chapa se construye progresivamente en menos de un segundo hasta quedar completa, con el tono del oro.
2. **Given** la ilustración, **When** el joyero introduce un ancho válido, **Then** la arista frontal cambia de proporción con una transición suave y bajo ella aparece la cota con el valor introducido («10,00 mm»); el espesor actúa sobre el canto y su cota lateral, y el largo sobre la arista en fuga y su cota.
3. **Given** medidas de proporciones extremas (0,1 mm de espesor y 10 000 mm de largo), **When** el joyero observa la ilustración, **Then** la chapa sigue siendo legible, con caras y cotas visibles, y las cotas muestran los valores reales.
4. **Given** la familia ORO, **When** el joyero elige PLATA, **Then** la chapa se redibuja en tono plateado-turquesa.
5. **Given** un campo vacío o no válido, **When** el joyero observa la ilustración, **Then** esa cota no muestra valor, la chapa usa una proporción de referencia para esa medida y las caras se ven atenuadas hasta que las tres medidas son válidas.
6. **Given** un lector de pantalla activo, **When** llega a la ilustración, **Then** anuncia el material y las tres medidas de la chapa.

---

### User Story 5 - Convivir con los fallos y rematar el flujo (Priority: P5)

Cuando el proveedor no responde, no hay conexión o falta la credencial, el joyero entiende qué pasa y qué puede hacer: ve un mensaje claro, los últimos precios conocidos si existen —marcados como desactualizados— y un botón «Reintentar» que respeta un tiempo de espera para no castigar la cuota. En PESO DE CHAPAS, «Limpiar» devuelve el formulario a su estado inicial y «Guardar en favoritos» avisa de que llegará próximamente.

**Why this priority**: Cierra el comportamiento de la pantalla en condiciones adversas y la coherencia con el resto de calculadoras; no aporta cálculo nuevo.

**Independent Test**: Se activa el modo avión con y sin caché previa, se simula el fallo de un metal y la falta de credencial, se pulsa «Reintentar» dentro y fuera de la ventana de espera, y se prueban los dos botones de chapas.

**Acceptance Scenarios**:

1. **Given** sin conexión y sin precios previos, **When** el joyero abre PRECIO METALES, **Then** ve un mensaje de que no se ha podido conectar y el botón «Reintentar»; la app sigue operativa.
2. **Given** sin conexión y precios de hace más de una hora, **When** el joyero abre PRECIO METALES, **Then** ve los últimos precios conocidos marcados como desactualizados, con su fecha, un aviso y «Reintentar».
3. **Given** que uno de los cinco metales falla, **When** el joyero observa la lista, **Then** los otros cuatro se muestran con normalidad, el metal fallido muestra el motivo (y su último dato conocido, si existe) y «Reintentar» está disponible.
4. **Given** un fallo hace menos de un minuto, **When** el joyero pulsa «Reintentar», **Then** no se consulta al proveedor y un aviso pide esperar un momento; **Given** más de un minuto, **Then** se consultan solo los metales que fallaron.
5. **Given** que el proveedor rechaza la consulta por límite de cuota, **When** el joyero observa la lista, **Then** ve el motivo y la espera antes de poder reintentar es de cinco minutos.
6. **Given** una app construida sin credencial del proveedor, **When** el joyero abre PRECIO METALES, **Then** ve que el servicio no está configurado, sin cierre de la app ni consulta al proveedor.
7. **Given** un cálculo completo en PESO DE CHAPAS, **When** el joyero pulsa «Limpiar», **Then** los campos quedan vacíos, la familia vuelve a ORO 18K, el resultado desaparece y la chapa vuelve a su proporción de referencia.
8. **Given** PESO DE CHAPAS, **When** el joyero pulsa «Guardar en favoritos», **Then** aparece un aviso efímero de «Próximamente» y el cálculo no se altera.
9. **Given** la primera visita o PRECIO METALES, **When** el joyero busca «Limpiar» o «Guardar en favoritos», **Then** no existen: solo PESO DE CHAPAS los ofrece.

---

### Edge Cases

- **Salir del módulo y volver a entrar**: el selector arranca sin opción marcada y PESO DE CHAPAS en su estado inicial; los precios, en cambio, se conservan y se reutilizan dentro de la hora.
- **Cambiar de sub-herramienta sin salir**: lo tecleado en PESO DE CHAPAS y los precios cargados se conservan.
- **Reloj del dispositivo adelantado o atrasado**: un dato guardado «en el futuro» no se considera vigente y se vuelve a consultar; la hora mostrada es la del dato del proveedor, en la zona horaria local.
- **Mercado cerrado o fin de semana**: la fecha del dato puede ser de hace días; se muestra tal cual, sin inventar frescura.
- **Moneda distinta al euro en la respuesta**: se trata como respuesta no válida de ese metal; jamás se mezclan monedas.
- **Precio medio ausente o a cero**: se usa el precio de venta y, si tampoco existe, el de compra; sin ninguno de los tres, el metal se muestra como sin dato.
- **Cantidades muy grandes** (kilo de rodio): las cifras llevan separador de miles y no rompen la composición; **muy pequeñas** (cobre por gramo): cuatro decimales.
- **Medidas con formato ambiguo** («1,2,3», «1.2.3», texto): no válidas; sin resultado y sin cierre de la app.
- **Muchos decimales en las medidas**: se aceptan; el cálculo interno conserva la precisión aunque la vista redondee.
- **Teclado en pantalla desplegado**: la ilustración y los resultados siguen alcanzables por desplazamiento.
- **Fuente del sistema muy grande o pantalla pequeña**: ningún texto se recorta y todo es alcanzable por desplazamiento; la tarjeta de mercado se reorganiza en lugar de recortarse.
- **Lector de pantalla activo**: el selector anuncia la opción elegida, cada fila de metal su nombre, precio y tendencia, la tarjeta de mercado sus ocho datos, cada campo su medida y unidad, y los avisos se anuncian al aparecer.
- **Redondeo de vista**: lo mostrado nunca se usa para recalcular ni para convertir.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El módulo «Herramientas» del menú principal MUST llevar a la pantalla real; la pantalla de andamiaje desaparece de ese destino.
- **FR-002**: La pantalla MUST abrir mostrando únicamente un selector con dos opciones excluyentes —PRECIO METALES y PESO DE CHAPAS—, cada una con icono y etiqueta, sin ninguna preseleccionada, y una invitación a elegir; el contenido MUST aparecer solo tras elegir una y la elección MUST conservarse mientras el joyero permanece en la pantalla, junto con el estado de cada sub-herramienta.
- **FR-003**: PRECIO METALES MUST mostrar exactamente cinco metales, en el orden oro, plata, cobre, paladio y rodio, cada uno con nombre, símbolo químico, imagen identificativa, precio en euros en la unidad elegida y flecha de tendencia según el signo de la variación.
- **FR-004**: Los precios MUST obtenerse del proveedor externo Metal Sentinel, en euros, al abrir PRECIO METALES, con una consulta por metal; la carga MUST mostrar un indicador mientras dura y MUST NOT bloquear el resto de la pantalla.
- **FR-005**: Cada precio obtenido con éxito MUST conservarse durante una hora, también tras cerrar la app; mientras un metal tenga un precio de hace menos de una hora, el sistema MUST NOT consultar al proveedor por ese metal, cualquiera que sea el número de veces que se abra la sección.
- **FR-006**: Si un metal falla, los demás MUST mostrarse con normalidad; el metal fallido MUST mostrar el motivo y, si existe un precio anterior, ese precio marcado como desactualizado. Sin conexión y con precios caducados, MUST mostrarse lo último conocido con su fecha y un aviso.
- **FR-007**: «Reintentar» MUST ofrecerse solo cuando haya errores; un reintento MUST NOT consultar al proveedor si el último intento fue hace menos de un minuto (cinco minutos si el proveedor rechazó por límite de consultas) y, cuando consulte, MUST hacerlo solo por los metales sin precio vigente.
- **FR-008**: El precio principal de cada metal MUST ser el precio medio del mercado; si no está disponible, el precio de venta; si tampoco, el de compra.
- **FR-009**: Un selector MUST permitir elegir la unidad entre gramo (por defecto), kilo y onza troy; la conversión MUST aplicar 1 onza troy = 31,1034768 g y 1 kg = 1 000 g a todas las cifras monetarias de la lista y de la tarjeta de mercado, MUST NOT aplicarse a la variación en porcentaje, y MUST partir siempre del valor original del proveedor, nunca de una cifra ya redondeada.
- **FR-010**: Si el proveedor no confirma la unidad de cotización de un metal, su precio MUST mostrarse en el valor y la unidad originales, sin convertir, y el selector de unidad MUST NOT alterarlo.
- **FR-011**: La tarjeta «Información del mercado» MUST mostrar, para el metal seleccionado (oro por defecto; se cambia pulsando una fila), su precio de venta, precio de compra, máximo, mínimo, variación, variación en porcentaje, unidad y fecha y hora del dato en la zona horaria del dispositivo.
- **FR-012**: Los importes MUST mostrarse con coma decimal, separador de miles, dos decimales cuando sean iguales o mayores que uno y cuatro cuando sean menores, redondeados a la cifra más cercana; la variación MUST llevar signo y el porcentaje MUST mostrarse con dos decimales y signo.
- **FR-013**: PRECIO METALES MUST mostrar «Precios orientativos. Pueden variar según el mercado.», «Fuente: Metal Sentinel» y la fecha y hora de la última actualización.
- **FR-014**: Los fallos MUST comunicarse con un mensaje comprensible según su causa —sin conexión, credencial rechazada, servicio no disponible, límite de consultas alcanzado, respuesta no válida, servicio no configurado— y MUST NOT revelar la credencial ni detalles técnicos.
- **FR-015**: La credencial de acceso al proveedor MUST NOT formar parte del código fuente versionado, MUST NOT mostrarse ni registrarse en ningún sitio, y una app construida sin ella MUST mostrar el estado «servicio no configurado» en lugar de fallar.
- **FR-016**: PESO DE CHAPAS MUST ofrecer la familia ORO con las leyes 18K, 14K, 12K y 9K y la familia PLATA con 950, 925, 900 y 800; al abrir MUST estar preseleccionados ORO y 18K; al cambiar a PLATA MUST preseleccionarse 925 y al volver a ORO 18K; cambiar de familia o de ley MUST conservar las medidas introducidas.
- **FR-017**: Los tres campos de medida —ancho, espesor y largo— MUST identificarse con su nombre y su unidad en milímetros, desplegar un teclado apto para decimales y aceptar coma y punto como equivalentes.
- **FR-018**: El sistema MUST tratar como no válida una medida vacía, no numérica, menor o igual que cero, o fuera del rango operativo (ancho y largo hasta 10 000 mm, espesor hasta 1 000 mm); con alguna medida no válida MUST NOT mostrarse resultado y la pantalla MUST permanecer estable; una medida fuera de rango MUST marcarse y acompañarse de un aviso que explique el rango.
- **FR-019**: Con las tres medidas válidas, el sistema MUST calcular y mostrar el resultado al instante y recalcular ante cualquier cambio de medida, familia o ley, sin botón de calcular.
- **FR-020**: El peso MUST calcularse como ancho × largo × espesor × densidad ÷ 1 000 con las densidades del documento técnico `UI_Plantillas/Feature_Herramientas/Especificacion_Calculadora_Peso_Chapas_Oro_Plata.md` (anexo de lógica de negocio de esta spec): oro 18K 15,58, 14K 13,07, 12K 12,75, 9K 11,20; plata 950 10,40, 925 10,36, 900 10,31, 800 10,14 g/cm³; el documento MUST prevalecer sobre los mockups, cuyas cifras de ejemplo no son fuente.
- **FR-021**: El resultado MUST mostrar el peso de la chapa en gramos con dos decimales redondeados a la cifra más cercana, el material para el que se calcula, el volumen en cm³ con tres decimales, la densidad aplicada con dos, la pureza en porcentaje con un decimal junto a la ley, y el metal fino en gramos con tres decimales; junto al resultado MUST mostrarse «Valores aproximados. Considera merma según tu proceso.».
- **FR-022**: El cálculo interno MUST conservar la precisión completa, MUST NOT redondear pasos intermedios y MUST NOT reutilizar cifras ya redondeadas; el metal fino MUST calcularse con la ley elegida (14K es 585 milésimas, no 14/24).
- **FR-023**: Las leyes 12K, 950 y 900 MUST NOT presentarse como leyes oficiales españolas y MUST acompañarse del aviso de ley técnica que ya usa la app; 18K, 14K, 9K, 925 y 800 no llevan aviso.
- **FR-024**: PESO DE CHAPAS MUST mostrar una ilustración isométrica de la chapa en la que ancho, espesor y largo sean identificables, con una cota por medida válida que muestre su valor en milímetros con dos decimales; la chapa MUST tomar el color de la familia elegida, MUST construirse progresivamente al abrir la sección y al cambiar de familia, MUST cambiar de proporción con una transición suave al variar una medida y MUST seguir siendo legible con proporciones extremas; sin alguna medida válida MUST usar una proporción de referencia para ella y mostrarse atenuada; MUST ofrecer una descripción con material y medidas para lectores de pantalla. Los cálculos MUST usar exclusivamente los valores introducidos, nunca las proporciones dibujadas.
- **FR-025**: PESO DE CHAPAS MUST ofrecer «Limpiar» —que devuelve la sub-herramienta a su estado inicial— y «Guardar en favoritos» —que en esta versión muestra un aviso efímero de «Próximamente» sin alterar el estado—; PRECIO METALES y la primera visita MUST NOT mostrarlos.
- **FR-026**: La familia ORO MUST identificarse con el acento dorado y PLATA con el turquesa en selectores, ilustración y resultado; el selector de sub-herramientas y los botones de acción MUST usar el lenguaje dorado de acción de la app.
- **FR-027**: La pantalla MUST llegar por navegación desde el menú, ofrecer retroceso, MUST NOT mostrar la barra de navegación inferior y su contenido MUST poder desplazarse cuando no quepa, incluido con el teclado desplegado.
- **FR-028**: El sistema MUST registrar como telemetría la visualización de la pantalla —conservando la identidad que ya emite el andamiaje— y de cada sub-herramienta, la sub-herramienta elegida, cada carga de precios (indicando si vino del proveedor o de la caché y si fue parcial), cada fallo total (con su motivo), los cambios de unidad y de metal seleccionado, cada cálculo de chapa identificando material y ley —sin repetirlo mientras solo cambien las cifras tecleadas— y el uso de favoritos; MUST NOT registrar las medidas introducidas ni la credencial.
- **FR-029**: Los elementos accionables MUST respetar el tamaño táctil mínimo del sistema de diseño; selector, filas, tarjeta de mercado, campos, ilustración, resultados y avisos MUST anunciarse correctamente a los lectores de pantalla.
- **FR-030**: Todos los textos visibles MUST poder traducirse a otro idioma sin modificar el comportamiento de la pantalla.

### Key Entities

- **Sub-herramienta**: cada una de las dos utilidades excluyentes del módulo (precio de metales, peso de chapas). La elección vive mientras el joyero está en la pantalla.
- **Metal cotizado**: cada uno de los cinco metales consultados (oro, plata, cobre, paladio, rodio), con nombre visible traducible, símbolo químico, imagen e identificador estable ante el proveedor.
- **Cotización**: el dato de mercado de un metal en euros —precio medio, de venta, de compra, máximo, mínimo, variación absoluta y porcentual—, con la unidad en que cotiza el proveedor, el instante del dato y el instante en que se obtuvo.
- **Instantánea de cotizaciones**: el conjunto de resultados de los cinco metales —cotización o motivo de fallo con su último dato conocido— más el instante del último intento; es lo que se conserva durante una hora y sobrevive al cierre de la app.
- **Unidad de precio**: gramo, kilo u onza troy; la elegida por el joyero determina cómo se muestran todas las cifras monetarias.
- **Material de chapa**: cada combinación de familia y ley (ocho en total) con su fracción de metal fino, su densidad orientativa y la indicación de si es una ley técnica fuera de la relación oficial española.
- **Medida**: cada una de las tres dimensiones de la chapa (ancho, espesor, largo) en milímetros, con su límite operativo.
- **Cálculo de chapa**: resultado de aplicar el material a las tres medidas: área, volumen, densidad aplicada, peso total, metal fino y resto de aleación, con precisión completa.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: La primera apertura de PRECIO METALES produce exactamente **5** consultas al proveedor (una por metal); volver a abrirla dentro de la hora siguiente —incluso tras cerrar la app— produce **0**, verificable en el panel del proveedor.
- **SC-002**: Un precio de 4.606,40 € por onza troy se muestra como 148,10 €/g, 148.099,20 €/kg y 4.606,40 €/oz, y las tres unidades se recorren sin volver a consultar al proveedor.
- **SC-003**: Los ocho casos de aceptación del documento técnico (§21.3) se reproducen en pantalla con 10 × 20 × 0,5 mm: 1,56 g (18K), 1,31 g (14K), 1,28 g (12K), 1,12 g (9K), 1,04 g (950), 1,04 g (925), 1,03 g (900) y 1,01 g (800), con oro fino 1,169 g en 18K.
- **SC-004**: Un joyero completa un cálculo de chapa —introducir tres medidas— con **3 interacciones** y ve el resultado **al instante**, sin botón de calcular.
- **SC-005**: Con un metal fallido, los otros cuatro precios son visibles en el **100%** de los casos; con los cinco fallidos y una instantánea anterior, los cinco últimos precios conocidos son visibles marcados como desactualizados.
- **SC-006**: En la primera visita solo es visible el selector con su invitación; tras elegir, el contenido aparece sin abandonar la pantalla; al salir y volver, el selector vuelve a estar sin opción marcada.
- **SC-007**: Introducir la misma medida con coma o con punto produce resultados idénticos en el **100%** de los casos.
- **SC-008**: La ilustración refleja cada medida válida en menos de **1 segundo** desde que se teclea, con la cota igual al valor introducido en el **100%** de los casos, y queda construida en menos de **1 segundo** al abrir la sección.
- **SC-009**: Con la fuente del sistema al doble y en pantallas desde 5", ningún texto se recorta y todo el contenido, incluida la tarjeta de mercado, es alcanzable.
- **SC-010**: Con lector de pantalla, el **100%** de los controles, filas, resultados, ilustración y avisos se anuncian con una descripción que identifica su función o su valor.
- **SC-011**: Ningún destino del menú principal lleva ya a una pantalla de andamiaje para el módulo de Herramientas.
- **SC-012**: La credencial del proveedor aparece **0** veces en el repositorio, en la pantalla y en los registros de la app.
- **SC-013**: Un reintento dentro del minuto siguiente a un fallo produce **0** consultas al proveedor; pasado el minuto, produce exactamente tantas como metales sin precio vigente.
- **SC-014**: Las pantallas renderizadas son reconocibles frente a los mockups de referencia en una comparación lado a lado, con los estilos propios de la app.

## Assumptions

- El destino del módulo de Herramientas ya existe en la navegación desde la feature 002 y hoy muestra una pantalla de andamiaje; esta feature sustituye su contenido sin añadir destinos nuevos. Favoritos y Ajustes siguen en andamiaje.
- **Una sola feature** con las dos sub-herramientas, en lugar de dos features: el selector solo tiene sentido con ambas. Decisión confirmada con el autor.
- **Consulta directa al proveedor desde la app**, asumida como prototipo: la cuota gratuita del proveedor (15 000 consultas al mes) equivale a 3 000 cargas completas al mes para toda la base de usuarios, insuficiente para una app pública; un servicio propio que custodie la credencial y comparta la caché será una feature aparte. La credencial se configura en el equipo de desarrollo y no se versiona. Decisión confirmada con el autor.
- **Precio principal = precio medio** del mercado; el de venta y el de compra se ven en la tarjeta de mercado. Decisión confirmada con el autor.
- **Dos decimales** en el peso de la chapa, como fija el documento técnico (1,558 g → «1,56 g»): las densidades son orientativas y un tercer decimal sería precisión aparente. Volumen y metal fino a tres, pureza a uno. Es una política de redondeo distinta de la de las calculadoras de oro, plata y soldaduras, a propósito. Decisión confirmada con el autor.
- **Primera visita sin sub-herramienta elegida**, con invitación a elegir; el mockup preselecciona PRECIO METALES y no se sigue: así el joyero que solo quiere chapas no gasta consultas. Decisión confirmada con el autor.
- **Caché de una hora** y no los quince minutos que sugiere la guía del proveedor: es la exigencia del encargo y protege mejor la cuota. Una instantánea completa y vigente bloquea la red también para «Reintentar», que solo aparece con errores. El tiempo de espera entre reintentos (un minuto; cinco tras límite de cuota) evita castigar la cuota ante un fallo persistente.
- **Hora local sin sufijo** en la fecha del dato; el mockup dice «UTC» y «tiempo real», y ninguno se sigue: la caché de una hora hace incierto «tiempo real» y la hora local es la que entiende el joyero.
- **Separador de miles** solo en precios: es la primera pantalla de la app con cifras de seis dígitos (kilo de oro o rodio). El resto de calculadoras no cambia.
- **Fuente de verdad numérica de chapas**: `UI_Plantillas/Feature_Herramientas/Especificacion_Calculadora_Peso_Chapas_Oro_Plata.md` prevalece sobre el mockup, cuyas cifras (3,13 g y densidad 15,55 para 10 × 20 × 0,80 mm de 18K) no son coherentes con la fórmula: el valor correcto es 2,49 g con 15,58 g/cm³.
- **Contrato del proveedor**: la respuesta real observada en la documentación pública del proveedor devuelve, por metal, símbolo, moneda, precio de venta, medio y de compra, máximo, mínimo, instante, variación absoluta y porcentual y la unidad «OUNCE» (onza troy). Antes de implementar se confirma con la credencial real qué nombre de parámetro acepta el proveedor y la unidad de cada uno de los cinco metales; una unidad desconocida se muestra sin convertir.
- **Orden de secciones de chapas** el del mockup: ilustración, material, medidas, resultado y botones. La etiqueta del tercer campo es «Largo» (no «Largo deseado»: no hay cálculo inverso). No se incluyen el chip «Vista: Isométrica» ni el botón «Calcular» del mockup ni la flecha de navegación de la tarjeta de mercado. Decisión confirmada con el autor.
- **Colores**: ORO dorado y PLATA turquesa en chapas; el mockup pinta ambas en turquesa y no se sigue. La ilustración de la chapa se dibuja en la app; la imagen `chapa.png` del encargo queda como último recurso solo si el dibujo resultara inviable. Decisión confirmada con el autor.
- **Límites operativos** de las medidas: los sugeridos por el documento técnico (§11.4); son controles de interfaz, no leyes físicas.
- **Imágenes**: se incorpora la de rodio; oro, plata, cobre y paladio reutilizan las existentes.
- Los botones de PESO DE CHAPAS son «Limpiar» y «Guardar en favoritos», como en las demás calculadoras; PRECIO METALES no tiene nada que limpiar ni guardar.
- La telemetría de la pantalla conserva la identidad que ya emite el andamiaje; cada sub-herramienta estrena la suya.
- La app sigue en un solo idioma, español, y bloqueada en orientación vertical.
- Quedan fuera de alcance y entrarán como features propias: servicio propio que custodie la credencial y comparta la caché, cálculo inverso de chapa (largo, ancho o espesor desde un peso), merma, densidades personalizables, coste estimado cruzando precios y chapas, históricos de precios, aviso de mercado cerrado y persistencia de favoritos. El diseño no debe impedir añadirlas después.
