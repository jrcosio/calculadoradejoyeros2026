# Implementation Plan: Calculadora de aleaciones de plata

**Branch**: `005-aleaciones-plata` | **Date**: 2026-08-24 | **Spec**: [spec.md](./spec.md)

## Summary

`Route.Plata` deja de caer en `PlaceholderScreen` y pasa a la calculadora real: campo de
gramos de plata fina 999‰, selector de ley (950/925/900/800 milésimas), fila de resultado
con el cobre a añadir y tarjeta de total con el peso de la aleación, con recálculo
reactivo, advertencia legal para las dos leyes técnicas, «Limpiar» y un «Guardar en
favoritos» que de momento solo avisa de «Próximamente».

Es la segunda calculadora del proyecto, y eso parte el trabajo en dos mitades. La primera
es un motor nuevo en `domain/`, **más simple que el de oro**: un único metal de liga (Cu) y
ninguna tabla de recetas, porque §28 del documento técnico prohíbe expresamente tabular
coeficientes por ley y exige la fórmula general. La segunda es un refactor: los siete
composables que la 004 dejó privados en `OroScreen.kt` los piden ahora dos pantallas, así
que suben a `ui/components/`. Es el mismo movimiento que ya hicieron `DiamondDivider`
(portada → Info) y `TarjetaAcento` (Info → oro).

La lógica de negocio es transcripción del documento técnico
`UI_Plantillas/Feature_plata/ESPECIFICACION_CALCULADORA_ALEACIONES_PLATA.md` (§ citados
desde aquí).

## Technical Context

**Language/Version**: Kotlin 2.2.10 (el que fija AGP 9.3.1) · Java 17

**Primary Dependencies**: Compose BOM 2026.08.00, Material 3, Navigation Compose 2.9.8 con
rutas `@Serializable`, Koin 4.2.2, Firebase BoM 34.18.0 (Analytics + Crashlytics);
`java.math.BigDecimal` del JDK para el motor

**Storage**: N/A — nada se persiste; la pantalla arranca limpia en cada visita

**Testing**: JUnit4 + MockK + Turbine + `kotlinx-coroutines-test` (JVM);
`compose-ui-test-junit4` (instrumentado)

**Target Platform**: Android, minSdk 24 / targetSdk 36 / compileSdk 37, solo vertical

**Project Type**: app Android de un solo módulo (`:app`), MVVM `ui → domain ← data`

**Performance Goals**: recálculo por pulsación de tecla en el hilo principal sin jank —
media docena de operaciones `BigDecimal`, muy por debajo de un frame

**Constraints**: sin dependencias nuevas; sin recursos nuevos; sin `material-icons`;
precisión interna completa sin redondeos intermedios (§14); la ley resultante nunca por
debajo de la objetivo **ni por efecto del redondeo de pantalla** (§16, §20); textos
traducibles

**Scale/Scope**: una pantalla, un motor con 4 leyes y un metal de liga, 2 casos de uso,
7 composables promovidos a compartidos, 0 imágenes nuevas, 0 iconos nuevos

**Esta feature no añade ninguna dependencia y no añade ningún recurso.** Todo lo que
necesita ya está en `gradle/libs.versions.toml` y en `res/`: `modulo_plata.png` y
`cobre.png` viven en `drawable-nodpi/` desde la 002 y la 004, y los siete iconos que usa
(`ic_lingotes`, `ic_refrescar`, `ic_estrella`, `ic_aviso`, `ic_balanza`, `ic_check`,
`ic_atras`, `ic_info`) los dibujó la 004.

### Decisiones y sus porqués

| Decisión | Motivo |
|---|---|
| Tipos de dominio propios (`LeyPlata`, `CalculoPlata`) en vez de generalizar el motor de oro | `RecetaLiga` lleva `color: ColorOro` y `ley: LeyOro` en su firma, y `CalculoAleacion` nombra sus campos «oro puro» y «liga». Generalizarlos obligaría a refactorizar el motor de oro, sus dos casos de uso y sus 205 líneas de test para que plata pueda reutilizar tres constantes. Dos motores paralelos y legibles, cada uno fiel a su documento técnico, valen más que uno genérico y abstracto |
| Sin tabla de recetas: fórmula general y nada más | §28 lo exige textualmente: prohibido `if target == 925: copper = source * 0.08`, aunque dé el mismo resultado. Con un solo metal de liga no hay proporciones que repartir, así que `RecetasPlata` (el equivalente de `RecetasOro`) simplemente no existe. Los coeficientes de §6 quedan como valores esperados de los tests, no como datos del motor |
| Constantes de precisión propias en `CalculoPlata` (`FINURA_ORIGEN`, `ESCALA = 15`, `TOLERANCIA = 1E-9`) | Tres literales duplicados respecto a `CalculoAleacion`. La alternativa —extraer un objeto de precisión compartido— toca la API pública del motor de oro (sus use cases y sus tests referencian `CalculoAleacion.ESCALA`) para ahorrar tres líneas. La duplicación es deliberada y va comentada: son dos documentos técnicos distintos y el 0,999 de plata no depende del 0,999 de oro |
| Motor con `BigDecimal`; división única con escala 15 y redondeo **a favor de la ley** | §14 prohíbe redondear pasos intermedios y §16/§20 exigen que la ley resultante nunca baje de la objetivo. Las multiplicaciones `BigDecimal` son exactas; solo se divide para obtener la masa final (directo) o la plata de partida (inverso) y la ley teórica de verificación. En el modo **directo** la división redondea `DOWN`: un peso final una pizca menor da menos cobre y la ley real queda igual o por encima. En el **inverso** redondea `UP`: ahí lo que protege la ley es poner una pizca **más** de plata fina. Mismo criterio que el motor de oro |
| **Formateo de vista truncado (`RoundingMode.DOWN`), no `HALF_UP` como en oro** | Es la decisión central de la feature y la única divergencia deliberada respecto al módulo hermano. Con 100 g de plata fina hacia 950‰ el cobre exacto es 5,157894736842105 g: `HALF_UP` mostraría **5,158 g**, y pesar esa cantidad da una ley práctica de 949,999‰ — por debajo del objetivo, y la Ley 17/1985 no admite tolerancia en menos (§3, §16). Truncar muestra **5,157 g** y una ley práctica de 950,008‰, que es exactamente el ejemplo de salida de §17 y §19. No es un caso de borde: con `HALF_UP` fallarían dos de los cuatro casos obligatorios de §21. Ver [Complexity Tracking](#complexity-tracking) |
| Tres decimales de vista = «modo taller seguro» de §16-§17 sin selector de balanza | §18 plantea un selector de resolución (0,1 / 0,01 / 0,001 / 0,0001 g) con doble cifra teórica y de pesada. Truncar a 3 decimales **es** el truncado a la resolución de 0,001 g que ese mismo apartado recomienda por defecto, así que la cifra mostrada ya es directamente pesable y la ley queda protegida sin pedirle al joyero una decisión más. El selector queda fuera de alcance, recogido en las asunciones de la spec |
| Dos casos de uso: cálculo directo y cálculo inverso | La constitución (IV) exige test unitario por caso de uso, y el inverso (§22-§23, FR-016) debe existir y probarse desde ya aunque sin UI. Mismo reparto que en oro: cada uno con una única responsabilidad y su test, y la construcción y verificación del resultado compartidas en el modelo |
| Cálculo síncrono, sin corrutinas ni `DispatcherProvider` | Media docena de operaciones aritméticas no justifica saltar de hilo. Sin corrutinas no hay nada que inyectar ni que testear con `TestDispatcher`; `OroViewModel` tampoco abre `viewModelScope`. Si el módulo creciera (histórico, persistencia), entrarían entonces |
| Estado inicial 925‰, campo vacío; «Limpiar» vuelve a él; sin memoria entre visitas | Asunción confirmada en la spec. 925 es la plata Sterling, la ley de trabajo habitual, la única de las cuatro que es a la vez oficial española y de uso corriente, y la que el mockup muestra activa |
| Telemetría: `plata_calculado` con `ley`, deduplicado; nunca la cantidad | FR-019. El recálculo es por pulsación de tecla: registrar cada una sería ruido. Se emite un evento cuando un cálculo válido estrena ley o cuando la entrada pasa de inválida a válida. `screen_view` sigue siendo `"plata"`, el que ya emitía el placeholder, para no romper la serie histórica. Botón de favoritos → `plata_favoritos_proximamente`. `analyticsId` de la ley son las milésimas en texto («925»), estable e independiente del idioma |
| Siete composables privados de `OroScreen.kt` promovidos a `ui/components/` | `CampoCantidad`, `CabeceraSeccion`, `BotonDorado`, el aviso de ley técnica, `FilaMetal`, `TarjetaTotal` y `LineaPunteada`. Sin promoverlos, `PlataScreen.kt` nacería con ~180 líneas copiadas. Es la doctrina que el proyecto ya aplicó dos veces: en cuanto un segundo consumidor lo pide, deja de ser privado. Es extracción pura, sin cambio de comportamiento, y `OroScreenTest` con sus dos `@Preview` son la red |
| `FilaMetal` y `TarjetaTotal` se generalizan a parámetros de presentación | La versión de oro recibe `MetalCalculado` y `ColorOro`, tipos de su feature. Promovidas reciben `imagenRes`, nombre, cifra y acento —y etiqueta y acento— y es cada pantalla la que mapea sus enums, exactamente como ya hace `OroScreen` con `MetalLiga.presentacion()`. Así `ui/components/` no conoce el dominio de ninguna feature |
| Strings genéricos sacados del namespace `oro_` | Un componente compartido no puede codificar `R.string.oro_entrada_unidad`. `oro_entrada_unidad` → `unidad_gramos`, los tres nombres y descripciones de metal → `metal_*`, y los tres textos de acción → `accion_limpiar`, `accion_guardar_favoritos`, `aviso_proximamente`. Hay precedente de strings compartidos sin prefijo de feature (`topbar_info`, `topbar_atras`, `nav_home`). El renombrado toca solo tres ficheros: `strings.xml`, `OroScreen.kt` y `OroScreenTest.kt` |
| Acentos: `SilverPrimary` en entrada y total, `TealPrimary` en selector y resultado | Lo que pinta el mockup: filete plateado en la tarjeta de entrada y turquesa en la selección y en la cifra de cobre. Decisión confirmada con el autor. El total va en plateado porque lo que pesa al final **es** la plata, y así entrada y total enmarcan en plata un centro en teal |
| Sin tokens de color nuevos | `SilverPrimary` (#C7CDD2) y `SilverDark` (#707980) ya existen en `JewelryColors` desde la 001. `TarjetaAcento(acento = SilverPrimary)` da el filete al 65 % de opacidad y el degradado suave del mockup; `SilverDark` sirve de borde del campo de cantidad, el papel que hace `BorderGold` en oro. No se inventa ningún `BorderSilver` |
| Botones inferiores dorados, y son «Limpiar» y «Guardar en favoritos» | El mockup dibuja un único botón «OK», pero el cálculo es reactivo y no hay nada que confirmar. Decisión confirmada con el autor: mismos dos botones que el módulo de oro, con `BotonDorado` sin parametrizar el color — el dorado es el lenguaje de acción principal de la app, no el acento del módulo |
| «(ley)» en las etiquetas de 925 y 800 | Las dos leyes oficiales de la Ley 17/1985 se distinguen en el propio selector (FR-005), no solo por ausencia de advertencia. El mockup marca únicamente 925, pero 800 lo es igual y marcar una sola sería incoherente. Decisión confirmada con el autor. Si «925 (ley)» no cupiera en su segmento, el `TextAutoSize` de `SelectorSegmentado` la encoge antes que recortarla |
| Advertencia con un texto por ley técnica, no uno genérico | §3 da dos redacciones distintas y cada una sitúa su milésima respecto de las oficiales («supera la ley 925‰» / «supera la ley 800‰ pero no alcanza 925‰»). Un texto genérico perdería esa información, que es justo la que evita que el joyero contraste mal |
| Reutilización de `SelectorSegmentado`, `TarjetaAcento` y `JewelryScaffold` tal cual | Los tres nacieron o se generalizaron en la 003/004 previendo este momento; el `SelectorSegmentado` incluso se puso en `components/` «porque el futuro módulo de plata pedirá el mismo control». Ninguno necesita cambios |
| «Próximamente» con `Toast` nativo, disparado desde el composable con estado | Mismo patrón exacto que oro (FR-015): aviso efímero sin estado propio, que se reemplaza solo por muchas pulsaciones que haya. El ViewModel solo registra la telemetría y no conoce Android |
| Barra superior estándar de sección: título + flecha atrás, sin barra inferior | El mockup dibuja el logo y un menú, pero es una sección de módulo y sigue el patrón de la app (misma justificación que en la 003 y la 004). Título: el `modulo_plata_titulo` existente |

## Constitution Check

*GATE: revisado antes de la Fase 0 y de nuevo tras el diseño. Sin violaciones.*

| Principio | Cumplimiento |
|---|---|
| I. SDD obligatorio | spec → plan → tasks → implement. Ningún fichero de producto se toca antes de `tasks.md`. La lógica de negocio viene del documento anexo referenciado por la spec, no improvisada. El refactor de componentes entra dentro de esta feature porque es ella la que lo hace necesario |
| II. MVVM con capas estancas | El motor (`LeyPlata`, `CalculoPlata`, los dos casos de uso) es Kotlin puro con `java.math.BigDecimal`: cero imports de `android.*`, `androidx.*`, Firebase o `data.*`. `PlataViewModel` expone un único `StateFlow<PlataUiState>`, no importa `androidx.compose.*` y recibe todo por constructor. `PlataScreen` se parte en resolutor + `PlataContent` sin estado con `@Preview`. El mapeo `LeyPlata` → `R.string` vive en la capa Compose como extension privada, igual que `LeyOro.etiquetaRes` |
| III. DI solo por Koin | Los dos casos de uso con `factoryOf` en `domainModule` y `viewModelOf(::PlataViewModel)` en `viewModelModule`. Sin módulo nuevo que registrar en `featureModules`, sin `get()` interno, sin `String` ni primitivos en el grafo |
| IV. Test obligatorio | Test unitario por caso de uso (directo: 4 casos de §21 + tabla de taller de §7 + invariantes de §20 + validaciones de §26; inverso: §23) y `PlataViewModelTest` (parseo, reactividad, truncado, limpiar, telemetría). `KoinModulesTest` cubre los registros nuevos sin tocarlo. Sin corrutinas no hay `TestDispatcher` que montar: el cálculo es síncrono y `DispatcherProvider` sigue sin tener consumidor, igual que en la 004 |
| V. Versiones en `libs.versions.toml` | No se añaden dependencias ni versiones |

Restricciones técnicas: ninguna API por encima de minSdk 24 (`BigDecimal`,
`BasicTextField`, `Toast` existen desde siempre); ningún producto nuevo de Firebase;
`google-services.json` intacto.

El refactor de `OroScreen.kt` y el renombrado de strings **no** son desviaciones de la
constitución: no cambian capas, no cambian comportamiento y quedan cubiertos por los tests
que ya existen. La divergencia de redondeo de presentación respecto al módulo de oro sí se
declara por escrito, abajo.

## Project Structure

### Documentation (this feature)

```text
specs/005-aleaciones-plata/
├── spec.md
├── plan.md              # este fichero
├── data-model.md        # entidades del motor de plata (Fase 1)
├── tasks.md             # salida de /speckit-tasks
└── checklists/
    └── requirements.md
```

No se generan `research.md`, `contracts/` ni `quickstart.md`, por los mismos motivos que en
la 004: la spec no dejó ningún `NEEDS CLARIFICATION` (las cuatro decisiones abiertas se
cerraron con el autor antes de redactarla y están en Assumptions), la app no expone
interfaz a terceros, y la guía de validación vive en la fase de verificación de `tasks.md`
y en los criterios de éxito de la spec. Sí se genera `data-model.md`: la feature estrena
modelo de dominio.

### Source Code (repository root)

```text
app/src/main/
├── java/com/jrblanco/calculadoradejoyeros2021/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── LeyPlata.kt              (N) enum puro: LEY_950…LEY_800 (milésimas, finura,
│   │   │   │                                esSoloTecnica para 950 y 900, analyticsId)
│   │   │   └── CalculoPlata.kt          (N) resultado: plata pura, cobre, masa y ley finales,
│   │   │                                    constantes de precisión y verificaciones de §20
│   │   └── usecase/
│   │       ├── CalcularAleacionPlataUseCase.kt        (N) modo directo (§9)
│   │       └── CalcularAleacionInversaPlataUseCase.kt (N) modo inverso (§22; sin UI)
│   ├── ui/
│   │   ├── components/
│   │   │   ├── Formularios.kt           (N) CampoCantidad + CabeceraSeccion, con acento
│   │   │   ├── Botones.kt               (N) BotonDorado
│   │   │   ├── Avisos.kt                (N) AvisoTecnico, con el texto por parámetro
│   │   │   ├── Tarjetas.kt              (M) + FilaMetal + TarjetaTotal, genéricas
│   │   │   └── Ornamentos.kt            (M) + LineaPunteada
│   │   ├── oro/OroScreen.kt             (M) consume los siete compartidos y borra sus copias
│   │   │                                    privadas; strings genéricos renombrados
│   │   ├── plata/                       (N) paquete de la feature
│   │   │   ├── PlataUiState.kt          (N) entrada, ley y resultado formateado
│   │   │   ├── PlataViewModel.kt        (N) StateFlow único + parseo + recálculo + telemetría
│   │   │   └── PlataScreen.kt           (N) PlataScreen + PlataContent + mapeos privados
│   │   └── navigation/AppNavHost.kt     (M) Route.Plata → PlataScreen
│   └── core/di/
│       ├── DomainModule.kt              (M) + los dos casos de uso de plata
│       └── ViewModelModule.kt           (M) + viewModelOf(::PlataViewModel)
└── res/values/strings.xml               (M) bloque plata_ nuevo; oro_entrada_unidad →
                                             unidad_gramos, oro_metal_* → metal_*,
                                             oro_limpiar/guardar/proximamente → accion_*/aviso_*

app/src/test/.../domain/usecase/CalcularAleacionPlataUseCaseTest.kt         (N)
app/src/test/.../domain/usecase/CalcularAleacionInversaPlataUseCaseTest.kt  (N)
app/src/test/.../ui/plata/PlataViewModelTest.kt                             (N)
app/src/androidTest/.../ui/plata/PlataScreenTest.kt                         (N)
app/src/androidTest/.../ui/oro/OroScreenTest.kt                             (M) strings renombrados

CLAUDE.md                                (M) cinco destinos pendientes pasan a cuatro; motor de
                                             plata, componentes promovidos y la nota de que
                                             plata trunca donde oro redondea a la media
```

No hay entradas en `res/drawable/` ni `res/drawable-nodpi/`: **esta feature no añade ni un
recurso gráfico**. Es la primera pantalla del proyecto que se pinta entera con lo que ya
había.

**Structure Decision**: se mantiene el módulo único `:app` con `ui → domain ← data`. La
feature añade un segundo motor a `domain/` y un paquete de pantalla bajo `ui/`, y consolida
`ui/components/` con lo que la 004 había dejado privado; no toca `data/` — no hay
persistencia ni fuente externa, y la telemetría ya tiene su interfaz de dominio.

## Complexity Tracking

Cero dependencias nuevas, cero recursos nuevos, cero capas nuevas y ninguna violación de la
constitución. Queda una divergencia deliberada que declarar por escrito, y un refactor cuyo
alcance conviene acotar.

| Divergencia | Por qué es necesaria | Alternativa más simple, y por qué se rechaza |
|---|---|---|
| El formateo de presentación de plata trunca (`RoundingMode.DOWN`) donde el de oro redondea a la media (`HALF_UP`) | La Ley 17/1985 no admite tolerancia en menos para plata (§3, §16), y el redondeo de pantalla es el último sitio donde la ley puede perderse. Con `HALF_UP` caen por debajo del objetivo **la mitad de los casos obligatorios de §21** (10 g hacia 950‰ da 949,980‰ y hacia 800‰ da 799,967‰) y también el ejemplo de salida de §19 (100 g hacia 950‰ da 949,999‰). Truncando, esos tres dan 950,071‰, 800,032‰ y 950,008‰ — el último es el valor exacto que §17 y §19 ponen como salida esperada. FR-011 lo exige y SC-003 lo mide | Usar `HALF_UP` por coherencia con el módulo de oro. Se rechaza porque haría fallar el requisito legal de la feature. La coherencia entre módulos no puede comprarse con una cifra que el joyero no debe pesar; la asimetría va comentada en el código y documentada en `CLAUDE.md` |
| Esta feature modifica `OroScreen.kt`, `OroScreenTest.kt` y renombra siete strings de la 004 | Los siete composables que plata necesita nacieron privados en la pantalla de oro. Duplicarlos costaría ~180 líneas de copia en `PlataScreen.kt` y dos sitios que mantener sincronizados para siempre | Copiar y pegar en `PlataScreen.kt`. Se rechaza: el proyecto ya resolvió este caso dos veces promoviendo (`DiamondDivider`, `TarjetaAcento`) y lo dejó escrito en `CLAUDE.md`. El riesgo de tocar la 004 se acota a extracción pura sin cambio de comportamiento, verificada por `OroScreenTest`, las dos `@Preview` de oro y la suite completa |
