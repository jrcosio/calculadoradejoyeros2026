# Implementation Plan: Calculadora de aleaciones de oro

**Branch**: `004-aleaciones-oro` | **Date**: 2026-08-23 | **Spec**: [spec.md](./spec.md)

## Summary

`Route.Oro` deja de caer en `PlaceholderScreen` y pasa a la calculadora real: campo de
gramos de oro fino 999‰, selector de ley (18K/14K/12K/9K), selector de color (amarillo,
blanco, rosa, rojo), filas de resultado por metal de liga y total de aleación, con
recálculo reactivo, advertencia legal para 12K, «Limpiar» y un «Guardar en favoritos»
que de momento solo avisa de «Próximamente».

Es la primera feature que llena la capa de dominio: estrena `domain/model/` (recetas y
resultado del cálculo como Kotlin puro con `BigDecimal`) y `domain/usecase/` (cálculo
directo e inverso), y con ello el `domainModule` de Koin, hoy vacío. La lógica de negocio
es transcripción literal del documento técnico
`UI_Plantillas/Feature_Oro/ESPECIFICACION_CALCULADORA_ALEACIONES_ORO.md` (§ citados desde
aquí); sus 16 recetas quedan como única fuente de verdad compilada en un solo fichero.

## Technical Context

**Language/Version**: Kotlin 2.2.10 (el que fija AGP 9.3.1) · Java 17
**Primary Dependencies**: Compose BOM 2026.08.00, Material 3, Navigation Compose 2.9.8 con
rutas `@Serializable`, Koin 4.2.2, Firebase BoM 34.18.0 (Analytics + Crashlytics);
`java.math.BigDecimal` del JDK para el motor
**Storage**: N/A — nada se persiste; las recetas viajan compiladas y la pantalla arranca
limpia en cada visita
**Testing**: JUnit4 + MockK + Turbine + `kotlinx-coroutines-test` (JVM);
`compose-ui-test-junit4` (instrumentado)
**Target Platform**: Android, minSdk 24 / targetSdk 36 / compileSdk 37, solo vertical
**Project Type**: app Android de un solo módulo (`:app`), MVVM `ui → domain ← data`
**Performance Goals**: recálculo por pulsación de tecla en el hilo principal sin jank —
aritmética `BigDecimal` de una decena de operaciones, muy por debajo de un frame
**Constraints**: sin dependencias nuevas; sin `material-icons`; precisión interna completa
sin redondeos intermedios (§10); la ley resultante nunca por debajo de la objetivo (§12);
textos traducibles
**Scale/Scope**: una pantalla, un motor de cálculo con 16 recetas, 2 casos de uso,
2 imágenes nuevas y ~7 iconos vectoriales nuevos

**Esta feature no añade ninguna dependencia.** Todo lo que necesita ya está en
`gradle/libs.versions.toml`; el motor usa `java.math.BigDecimal` del JDK.

### Decisiones y sus porqués

| Decisión | Motivo |
|---|---|
| Recetas como constantes Kotlin en `domain/model/`, no JSON en assets | Única fuente de verdad (§15) compilada y testeable en JVM sin IO ni parseo. `kotlinx-serialization` está disponible pero un JSON en assets exigiría contexto Android para leerlo, y eso está prohibido en `domain/`. Las proporciones se escriben como los literales decimales exactos del documento, en `String`, para que `BigDecimal` no herede el error binario de un `Double` |
| Motor con `BigDecimal`; divisiones con escala fija alta y redondeo **a la baja** | §10 prohíbe redondear pasos intermedios y §12 exige que la ley resultante nunca baje de la objetivo. Las multiplicaciones `BigDecimal` son exactas; solo dividen `masaFinal = oroPuro / finuraObjetivo` y la ley teórica de verificación. Redondear esa división a la baja (escala 15, `RoundingMode.DOWN`) hace la liga una pizca menor, con lo que la ley real queda **siempre** igual o por encima del objetivo. `HALF_UP` podría dejarla por debajo por una billonésima y romper la verificación de §12 |
| Validación de recetas con tolerancia computacional (1e-9), no igualdad estricta | Los presets del documento son literales decimales: los tres del blanco 750 suman 0,9999999999999999. El propio documento (§12) admite «una tolerancia puramente computacional muy pequeña». La suma de metales contra la liga total se verifica con la misma tolerancia |
| Dos casos de uso: cálculo directo y cálculo inverso | La constitución (IV) exige test unitario por caso de uso, y el inverso (§14, FR-016) debe existir y probarse desde ya aunque sin UI. Separados, cada uno tiene una única responsabilidad y su test; el reparto de liga común vive en el modelo |
| Cálculo síncrono, sin corrutinas ni `DispatcherProvider` | Una decena de operaciones aritméticas no justifica saltar de hilo. Sin corrutinas no hay nada que inyectar ni que testear con `TestDispatcher`; si el módulo creciera (histórico, persistencia), entrarían entonces |
| Formateo de vista en el ViewModel (3 decimales, coma, `HALF_UP`) | Kotlin puro y testeable en JVM: el estado expone las cifras ya formateadas y la vista solo pinta. `HALF_UP` es lo que usan los propios ejemplos de salida del documento (§17 muestra 1.129 para 1.128580858). El redondeo es solo de presentación: jamás se recalcula a partir de lo mostrado (§21) |
| Estado inicial 18K + amarillo, campo vacío; «Limpiar» vuelve a él; sin memoria entre visitas | Asunción confirmada en la spec. 18K es la ley de referencia del taller español y amarillo el primer color; el mockup arranca con 18K activo |
| Telemetría: `oro_calculado` con `ley` y `color`, deduplicado; nunca la cantidad | FR-019. El recálculo es por pulsación de tecla: registrar cada una sería ruido. Se emite un evento cuando un cálculo válido estrena combinación ley×color o cuando la entrada pasa de inválida a válida. `screen_view` sigue siendo `"oro"`, el que ya emitía el placeholder, para no romper la serie histórica. Botón de favoritos → `oro_favoritos_proximamente` |
| `SelectorSegmentado` a mano en `ui/components/` | `SegmentedButton` de Material impone su altura, su forma y su marca de selección; el mockup pide píldora con degradado dorado (leyes) o teal (colores) y check. Mismo precedente que `JewelryBottomBar` y el botón de portada. Va a `components/` porque el futuro módulo de plata pedirá el mismo control |
| `TarjetaDorada` de `ui/info/` promovida a `ui/components/` como `TarjetaAcento` | Mismo movimiento que se hizo con `DiamondDivider` en la 003: en cuanto un segundo consumidor la pide, deja de ser privada. Se parametriza el color de acento porque la tarjeta de resultados del mockup es teal; Info la sigue usando en dorado por defecto |
| Campo de cantidad con `BasicTextField` decorado a mano | `OutlinedTextField` impone altura, padding y tipografía de Material; el mockup pide cifra grande centrada en caja redondeada con sufijo «gr». Teclado `KeyboardType.Decimal`; se aceptan coma y punto y se normalizan al parsear (FR-003) |
| Barra superior estándar de sección: título + flecha atrás, sin barra inferior | El mockup dibuja el logo y un menú, pero es una sección de módulo y sigue el patrón de la app (misma justificación documentada en la 003). Título: el `modulo_oro_titulo` existente |
| Estilo de cifra grande como `TextStyle` suelto en `Type.kt`, fuera de `Typography` | Mismo precedente que `TitleSerif`: es un estilo de un contexto concreto, no un rol del sistema. Manrope Bold — Playfair Display sigue reservada a la portada |
| Sin token de color para el cobre | El mockup pinta todas las cifras de metal en teal y los nombres en claro: la identidad del metal la dan su imagen y su nombre, no un color. No se añade nada a `JewelryColors` |
| `cobre.png` y `paladio.png` a 512 px en `drawable-nodpi/`; oro y plata reutilizados | Precedente de la 002/003 (`sips -Z 512`). `modulo_oro.png` ilustra la tarjeta de entrada y `modulo_plata.png` la fila de plata fina, como pidió el autor |
| Iconos nuevos como vectores propios de trazo 1.8 | `material-icons` no está en el classpath y está deprecada. Check de selección, aviso, refrescar, estrella, balanza y los dos de sección (lingotes, paleta), tintados en tiempo de ejecución |
| «Próximamente» con `Toast` nativo | Aviso efímero sin estado propio (FR-015). No hay `Scaffold` de Material ni `SnackbarHost` en el árbol y montarlos solo para esto sería más chrome que feature. El `Toast` se dispara desde el composable con el contexto local; el ViewModel solo registra la telemetría |
| Enums de dominio (`ColorOro`, `LeyOro`, `MetalLiga`) con `analyticsId` | Mismo patrón que `HomeModule`/`InfoEnlace`: identificadores estables independientes del idioma. Viven en `domain/` porque las recetas los usan como clave; `ui` puede importarlos (la flecha `ui → domain` lo permite), el mapeo a textos e imágenes queda en la capa Compose |

## Constitution Check

*GATE: revisado antes de la Fase 0 y de nuevo tras el diseño. Sin violaciones.*

| Principio | Cumplimiento |
|---|---|
| I. SDD obligatorio | spec → plan → tasks → implement. Ningún fichero de producto se toca antes de `tasks.md`. La lógica de negocio viene de un documento anexo referenciado por la spec, no improvisada |
| II. MVVM con capas estancas | El motor entero (`domain/model` + `domain/usecase`) es Kotlin puro con `java.math.BigDecimal`: cero imports de `android.*`, `androidx.*`, Firebase o `data.*`. `OroViewModel` expone un único `StateFlow<OroUiState>`, no importa `androidx.compose.*` y recibe todo por constructor. `OroScreen` se parte en resolutor + `OroContent` sin estado con `@Preview` |
| III. DI solo por Koin | Use cases con `factoryOf` en `domainModule` (estrenándolo) y `viewModelOf(::OroViewModel)` en `viewModelModule`. Sin módulo nuevo que registrar en `featureModules`, sin `get()` interno, sin `String` ni primitivos en el grafo |
| IV. Test obligatorio | Test unitario por caso de uso (directo: 16 combinaciones + 5 casos §13 + validaciones §16; inverso: §14) y `OroViewModelTest` (parseo, reactividad, limpiar, telemetría). `KoinModulesTest` cubre los registros nuevos sin tocarlo. Sin corrutinas no hay `TestDispatcher` que montar |
| V. Versiones en `libs.versions.toml` | No se añaden dependencias ni versiones |

Restricciones técnicas: ninguna API por encima de minSdk 24 (`BigDecimal`,
`BasicTextField`, `Toast` existen desde siempre); ningún producto nuevo de Firebase;
`google-services.json` intacto.

Sin desviaciones que declarar.

## Project Structure

### Documentation (this feature)

```text
specs/004-aleaciones-oro/
├── spec.md
├── plan.md              # este fichero
├── data-model.md        # entidades del motor y las 16 recetas (Fase 1)
├── tasks.md             # salida de /speckit-tasks
└── checklists/
    └── requirements.md
```

No se generan `research.md`, `contracts/` ni `quickstart.md`: la spec no dejó ningún
`NEEDS CLARIFICATION` (las decisiones numéricas están resueltas en «Decisiones y sus
porqués»), la app no expone interfaz a terceros, y la guía de validación vive en la fase
de verificación de `tasks.md` y en los criterios de éxito de la spec. Sí se genera
`data-model.md`, el primero del proyecto: esta feature estrena el modelo de dominio y las
16 recetas merecen quedar especificadas como datos, no solo como código.

### Source Code (repository root)

```text
app/src/main/
├── java/com/jrblanco/calculadoradejoyeros2021/
│   ├── domain/
│   │   ├── model/                       (N) primer contenido de la capa
│   │   │   ├── ColorOro.kt              (N) enum puro: AMARILLO, BLANCO, ROSA, ROJO (+ analyticsId)
│   │   │   ├── LeyOro.kt                (N) enum puro: LEY_18K…LEY_9K (milésimas, finura,
│   │   │   │                                esSoloTecnica para 500‰, analyticsId)
│   │   │   ├── MetalLiga.kt             (N) enum puro: PLATA_FINA, COBRE, PALADIO (+ analyticsId)
│   │   │   ├── RecetaLiga.kt            (N) proporciones internas de la liga; valida suma ≈ 1
│   │   │   ├── RecetasOro.kt            (N) las 16 recetas del documento, única fuente de verdad
│   │   │   └── CalculoAleacion.kt       (N) resultado: oro puro, liga, metales, masa y ley finales
│   │   └── usecase/                     (N)
│   │       ├── CalcularAleacionOroUseCase.kt        (N) modo directo (desde oro disponible)
│   │       └── CalcularAleacionInversaOroUseCase.kt (N) modo inverso (desde peso final; sin UI)
│   ├── ui/
│   │   ├── components/
│   │   │   ├── SelectorSegmentado.kt    (N) fila de opciones excluyentes con acento y check
│   │   │   └── Tarjetas.kt              (N) TarjetaAcento (antes TarjetaDorada, privada en Info)
│   │   ├── info/InfoScreen.kt           (M) usa TarjetaAcento compartida; borra su copia privada
│   │   ├── oro/                         (N) paquete de la feature
│   │   │   ├── OroUiState.kt            (N) entrada, selecciones y resultado formateado
│   │   │   ├── OroViewModel.kt          (N) StateFlow único + parseo + recálculo + telemetría
│   │   │   └── OroScreen.kt             (N) OroScreen + OroContent + tarjetas y filas privadas
│   │   ├── theme/Type.kt                (M) + estilo de cifra grande, fuera de Typography
│   │   └── navigation/AppNavHost.kt     (M) Route.Oro → OroScreen
│   └── core/di/
│       ├── DomainModule.kt              (M) primeros registros: los dos use cases
│       └── ViewModelModule.kt           (M) + viewModelOf(::OroViewModel)
└── res/
    ├── drawable-nodpi/                  (N) cobre.png, paladio.png — 512 px desde
    │                                        UI_Plantillas/Feature_Oro/ (oro y plata se reutilizan)
    ├── drawable/                        (N) ic_check.xml, ic_aviso.xml, ic_refrescar.xml,
    │                                        ic_estrella.xml, ic_balanza.xml, ic_lingotes.xml,
    │                                        ic_paleta.xml
    └── values/strings.xml               (M) bloque oro_ (etiquetas, metales, aviso 12K, botones)

app/src/test/.../domain/usecase/CalcularAleacionOroUseCaseTest.kt         (N)
app/src/test/.../domain/usecase/CalcularAleacionInversaOroUseCaseTest.kt  (N)
app/src/test/.../ui/oro/OroViewModelTest.kt                               (N)
app/src/androidTest/.../ui/oro/OroScreenTest.kt                           (N)

CLAUDE.md                                (M) seis destinos pendientes pasan a cinco; domain
                                             estrenado y componentes nuevos documentados
```

**Structure Decision**: se mantiene el módulo único `:app` con `ui → domain ← data`. La
feature añade el primer contenido real de `domain/` (modelo + casos de uso) y un paquete
de pantalla bajo `ui/`; no toca `data/` — no hay persistencia ni fuente externa, y la
telemetría ya tiene su interfaz de dominio.

## Complexity Tracking

Sin complejidad que justificar: cero dependencias nuevas, cero capas nuevas (se estrena
contenido de una capa ya prevista por la arquitectura), nada persistido y ninguna
desviación de la constitución.

Las divergencias respecto al mockup —barra superior de sección en lugar de logo, 3
decimales en lugar de 2, y que sus cifras de ejemplo no casan con la matemática del
documento técnico (plata y cobre intercambiados)— no son desviaciones de la constitución:
están decididas con el autor y recogidas como asunciones en la spec.
