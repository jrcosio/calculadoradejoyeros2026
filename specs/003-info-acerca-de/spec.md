# Feature Specification: Pantalla de Información («Acerca de»)

**Feature Branch**: `003-info-acerca-de`

**Created**: 2026-08-23

**Status**: Draft

**Input**: User description: "Pantalla de Información («Acerca de»). Hoy el botón de info de la barra superior lleva a una pantalla de andamiaje; debe llevar a una pantalla real que cuente quién ha hecho la app y por qué, y ofrezca acceso directo a las redes del autor y de la joyería. Título «Información»; bloque de perfil con foto, nombre y texto de presentación, más una fila de etiquetas profesionales; acceso a LinkedIn; bloque informativo de Blanco Joyeros con logotipo, imagen de joyería y texto; acceso a Instagram; versión de la app al pie. Los accesos abren la app nativa si está instalada y, si no, el navegador. Se llega desde el botón de información de la barra superior y se vuelve con la flecha de retroceso; sin barra inferior. Mockup de referencia: UI_Plantillas/Feature_Info/ejemplo_info.png."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Saber quién está detrás de la app y por qué existe (Priority: P1)

Un joyero que se plantea usar la app a diario para calcular precios de su trabajo quiere saber quién la ha hecho y con qué criterio. Toca el control de información de la barra superior y llega a una pantalla que le presenta al autor con su foto, su nombre, su doble condición de desarrollador y joyero artesano, y el propósito de la herramienta.

**Why this priority**: Es la razón de ser de la pantalla. Una calculadora de precios pide confianza antes de que nadie base en ella el presupuesto de una pieza; saber que la firma alguien del oficio es lo que la da.

**Independent Test**: Se abre la pantalla desde el control de información y se comprueba que el perfil del autor se muestra completo y es legible. Entrega valor por sí solo, sin ninguno de los accesos externos.

**Acceptance Scenarios**:

1. **Given** cualquier pantalla con barra superior, **When** el joyero toca el control de información, **Then** llega a la pantalla de información y ya no ve una pantalla de andamiaje.
2. **Given** la pantalla de información, **When** el joyero la observa, **Then** ve un título que la identifica, la foto y el nombre del autor, y el texto que explica el propósito de la app.
3. **Given** la pantalla de información, **When** el joyero mira bajo la presentación, **Then** ve las etiquetas que resumen el perfil profesional del autor.
4. **Given** la pantalla de información, **When** el joyero mira la barra superior, **Then** no encuentra en ella un acceso a la información: ya está dentro.
5. **Given** la pantalla de información, **When** el joyero usa el control de retroceso, **Then** regresa a la pantalla desde la que entró, no a una pantalla fija.
6. **Given** la pantalla de información en un móvil pequeño, **When** el joyero desplaza el contenido, **Then** alcanza todos los bloques hasta el pie.

---

### User Story 2 - Llegar a las redes del autor y de la joyería (Priority: P2)

El joyero quiere seguir el trabajo del autor o contactar con él profesionalmente, y ver las piezas de Blanco Joyeros. Desde la misma pantalla toca el acceso a LinkedIn o el de Instagram y aterriza en el perfil correspondiente, dentro de la aplicación de esa red si la tiene instalada.

**Why this priority**: Convierte la pantalla en un canal de contacto real, pero la app sigue siendo útil y la presentación del autor sigue entregándose sin estos accesos.

**Independent Test**: Se toca cada acceso y se comprueba que se abre el perfil esperado; y se repite con las apps de esas redes desinstaladas, comprobando que se abre en el navegador.

**Acceptance Scenarios**:

1. **Given** la pantalla de información, **When** el joyero la observa, **Then** ve dos accesos externos claramente distinguibles —LinkedIn e Instagram— y la dirección a la que lleva cada uno.
2. **Given** el acceso a LinkedIn, **When** el joyero lo activa con la aplicación de LinkedIn instalada, **Then** se abre en ella el perfil del autor.
3. **Given** el acceso a Instagram, **When** el joyero lo activa sin la aplicación de Instagram instalada, **Then** se abre el perfil de Blanco Joyeros en el navegador del dispositivo.
4. **Given** el bloque de Blanco Joyeros, **When** el joyero lo toca, **Then** no ocurre nada: es informativo y así se percibe, sin sugerir que sea accionable.
5. **Given** un dispositivo sin ninguna aplicación capaz de abrir direcciones web, **When** el joyero activa un acceso, **Then** la app permanece abierta y estable en la pantalla de información.
6. **Given** el acceso a LinkedIn, **When** el joyero lo pulsa dos veces seguidas antes de que el destino llegue a abrirse, **Then** el perfil se abre una sola vez y la telemetría registra un solo evento.

---

### User Story 3 - Identificar la versión instalada (Priority: P3)

El joyero que reporta un problema necesita decir qué versión tiene. La encuentra al pie de la pantalla de información sin salir de la app.

**Why this priority**: Es apoyo al soporte, no valor de uso diario. La pantalla cumple su función sin este dato.

**Independent Test**: Se compara la versión mostrada al pie con la de la aplicación instalada en el dispositivo.

**Acceptance Scenarios**:

1. **Given** la pantalla de información, **When** el joyero llega al final del contenido, **Then** ve la versión de la app.
2. **Given** una nueva publicación de la app, **When** el joyero abre la pantalla de información, **Then** la versión mostrada es la de la app instalada, sin haber tenido que actualizarla a mano en ningún sitio.

---

### Edge Cases

- **Sin conexión a internet**: activar un acceso externo abre igualmente la app o el navegador; el error de red lo gestiona el destino, no esta pantalla.
- **Sin ninguna aplicación capaz de abrir enlaces web**: la app no debe cerrarse ni quedarse bloqueada, y el intento fallido debe quedar registrado para poder diagnosticarlo.
- **Doble pulsación rápida sobre un acceso**: no debe abrir dos veces el mismo destino ni duplicar el registro de telemetría.
- **Volver de la red social a la app**: se regresa a la pantalla de información en el mismo punto de desplazamiento.
- **Pulsar repetidamente el control de información desde la pantalla de origen**: no debe apilar el destino varias veces ni obligar a retroceder más de una vez.
- **Fuente del sistema muy grande o pantalla pequeña**: ningún texto se recorta y todo el contenido sigue siendo alcanzable mediante desplazamiento.
- **Lector de pantalla activo**: la foto, el logotipo y la imagen de joyería se describen; los dos accesos se anuncian como accionables e indican adónde llevan.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El control de información de la barra superior MUST llevar a la pantalla de información desde cualquier pantalla que muestre esa barra.
- **FR-002**: La pantalla MUST identificarse con un título visible.
- **FR-003**: La pantalla MUST presentar al autor con su fotografía, su nombre y un texto que explique el propósito de la aplicación.
- **FR-004**: La pantalla MUST mostrar las etiquetas que resumen el perfil profesional del autor.
- **FR-005**: La pantalla MUST ofrecer un acceso al perfil de LinkedIn del autor, mostrando el nombre de la red y la dirección de destino.
- **FR-006**: La pantalla MUST ofrecer un acceso al perfil de Instagram de Blanco Joyeros, mostrando el nombre de la red y la dirección de destino.
- **FR-007**: La pantalla MUST incluir un bloque de Blanco Joyeros con su logotipo, una imagen de joyería y un texto que describa la relación; este bloque MUST NOT ser accionable.
- **FR-008**: Al activar un acceso externo, el sistema MUST abrirlo en la aplicación nativa de esa red si está instalada y, en su defecto, en el navegador del dispositivo.
- **FR-009**: Si no existe ninguna aplicación capaz de abrir la dirección, el sistema MUST permanecer estable en la pantalla de información y MUST registrar el fallo como error para su diagnóstico.
- **FR-010**: La pantalla MUST mostrar la versión de la aplicación instalada, obtenida de la propia aplicación y no escrita a mano.
- **FR-011**: La pantalla MUST ofrecer un control de retroceso que devuelva al joyero a la pantalla desde la que entró, y MUST NOT mostrar la barra de navegación inferior.
- **FR-012**: El contenido de la pantalla MUST poder desplazarse cuando no quepa completo.
- **FR-013**: El sistema MUST registrar como telemetría la visualización de la pantalla y la apertura de cada acceso externo, identificando cuál se ha abierto.
- **FR-014**: Los elementos accionables MUST respetar el tamaño táctil mínimo que fija el sistema de diseño y anunciarse como accionables a los lectores de pantalla.
- **FR-015**: Toda imagen con significado MUST tener una descripción textual alternativa.
- **FR-016**: Todos los textos visibles MUST poder traducirse a otro idioma sin modificar el comportamiento de la pantalla.
- **FR-017**: Mientras una apertura de acceso externo esté en curso, el sistema MUST ignorar nuevas activaciones de accesos externos, de forma que un destino no se abra dos veces ni su evento de telemetría se registre por duplicado. Al regresar a la pantalla, el sistema MUST volver a admitir activaciones.
- **FR-018**: La barra superior de la pantalla de información MUST NOT ofrecer el acceso a la propia pantalla de información; el hueco que deja no debe descentrar el resto de la barra.

### Key Entities

- **Acceso externo**: destino fuera de la aplicación al que la pantalla ofrece un atajo. Tiene un nombre visible (la red), una dirección de destino y un identificador estable para telemetría, independiente del idioma. En esta feature hay dos y la lista es fija; no se configura ni se descarga.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Desde cualquier pantalla con barra superior, el joyero llega a la información de la app con **una sola pulsación**.
- **SC-002**: Desde la pantalla de información, el joyero alcanza cualquiera de los dos perfiles sociales con **una sola pulsación**.
- **SC-003**: Todo el contenido de la pantalla es alcanzable en pantallas desde 5" hasta 7", con desplazamiento si hace falta, y con la fuente del sistema al doble de tamaño sin que se recorte ningún texto.
- **SC-004**: En el **100%** de los intentos de abrir un enlace sin aplicación capaz de atenderlo, la app sigue abierta y en la pantalla de información.
- **SC-005**: La versión mostrada coincide con la de la aplicación instalada en el **100%** de las compilaciones, sin intervención manual.
- **SC-006**: Desde la pantalla de información, el joyero vuelve a su pantalla de origen con **una sola acción**.
- **SC-007**: Ningún destino de la barra superior lleva ya a una pantalla de andamiaje.
- **SC-008**: Con lector de pantalla, el **100%** de los elementos accionables e imágenes con significado se anuncian con una descripción que identifica su contenido o su destino.
- **SC-009**: La pantalla renderizada es reconocible como el mockup de referencia en una comparación visual lado a lado.
- **SC-010**: Dos pulsaciones consecutivas sobre el mismo acceso externo, separadas por menos de un segundo, producen **un único** evento de telemetría.

## Assumptions

- El destino de información ya existe en la navegación desde la feature 002 y hoy muestra una pantalla de andamiaje. Esta feature sustituye su contenido; no crea un destino nuevo.
- La pantalla **no** lleva barra de navegación inferior, aunque el mockup la dibuje: es una sección, no una de las tres zonas principales, y el sistema de navegación de la feature 002 reserva esa barra a Home, Favoritos y Ajustes. Decisión confirmada con el autor.
- El acceso a la información desaparece de la barra superior **solo** en la propia pantalla de información (FR-018); en el resto de pantallas sigue exactamente igual.
- Los textos, las direcciones de los perfiles y las imágenes son fijos y viajan dentro de la aplicación: no se configuran ni se descargan.
- La fotografía del autor, el logotipo de Blanco Joyeros y la imagen de joyería los aporta el propio autor y su uso está autorizado.
- El bloque de Blanco Joyeros es una mención informativa; no describe condiciones comerciales ni enlaza a una tienda.
- La aplicación sigue en un solo idioma, español, como el resto de pantallas.
- Quedan fuera de alcance: licencias de software de terceros, política de privacidad, aviso legal, formulario de contacto y valoración en la tienda de aplicaciones. Si hacen falta, entran como feature propia.
