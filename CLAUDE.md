# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

App Android (Kotlin + Jetpack Compose) de cálculo de precios para joyeros.

## Regla número uno: SDD obligatorio

Este proyecto usa **Spec-Driven Development con GitHub Spec Kit**. Toda feature
recorre el ciclo completo antes de tocar código de producto:

```
/speckit-specify  →  /speckit-plan  →  /speckit-tasks  →  /speckit-implement
```

- **No escribas código de producto sin un `tasks.md` aprobado** en `specs/<NNN>-<slug>/`.
  Si te piden una feature directamente, arranca por `/speckit-specify`.
- `/speckit-specify` crea también la rama `NNN-slug` (extensión git de Spec Kit).
- Opcionales pero recomendados: `/speckit-clarify` antes de planificar,
  `/speckit-analyze` antes de implementar.
- **Exentos** del ciclo: arreglos de build, subidas de versión, typos y documentación.

Las normas del proyecto viven en `.specify/memory/constitution.md`. Este fichero es
la guía operativa; la constitución es la norma. Si enmiendas una, actualiza la otra
en el mismo cambio.

## Compilar y testear

`java` **no está en el PATH**. Cualquier `./gradlew` sin `JAVA_HOME` falla:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

./gradlew :app:assembleDebug        # APK debug
./gradlew :app:testDebugUnitTest    # tests unitarios (JVM)
./gradlew :app:lint                 # lint de Android

# un solo test o una sola clase
./gradlew :app:testDebugUnitTest --tests "*HomeViewModelTest"
./gradlew :app:testDebugUnitTest --tests "*HomeViewModelTest.registra*"

# comprobar que Firebase resuelve el google-services.json
./gradlew :app:processDebugGoogleServices
```

Los resultados de test en XML quedan en `app/build/test-results/testDebugUnitTest/`;
son más rápidos de leer que el HTML cuando algo falla.

## Arquitectura

MVVM con tres capas y una regla de dependencia estricta: **`ui → domain ← data`**.
Todo cuelga de `app/src/main/java/com/jrblanco/calculadoradejoyeros2021/`.

| Capa | Qué contiene | Qué tiene prohibido importar |
|---|---|---|
| `domain/` | `model/`, `repository/` (interfaces), `usecase/` | `android.*`, `androidx.*`, `com.google.firebase.*`, `data.*` |
| `data/` | `source/remote/`, `source/local/`, `repository/` (implementaciones) | `ui.*` |
| `ui/` | `navigation/`, `theme/`, `components/` (reutilizables), `placeholder/`, y una carpeta por pantalla | `data.*` |
| `core/` | `di/` (módulos Koin), `ui/UiState.kt`, `util/DispatcherProvider.kt` | — |

Los SDK externos están confinados: **`FirebaseAnalyticsDataSource` es el único
fichero del proyecto que importa `com.google.firebase.*`** (junto a `core/di/FirebaseModule.kt`,
que los registra). Todo lo demás pasa por `domain/repository/AnalyticsRepository`.

La capa `domain/` dejó de estar vacía con la feature 004: el motor de aleaciones de oro
vive en `domain/model/` (recetas en `RecetasOro` como **única fuente de verdad**, con
`BigDecimal` construido desde literales `String`) y `domain/usecase/` (cálculo directo e
inverso, registrados en `domainModule` con `factoryOf`). El motor no redondea pasos
intermedios y sus divisiones redondean **a favor de la ley** (nunca por debajo de la
objetivo); el redondeo a 3 decimales es exclusivo del ViewModel.

`ui/home/` es la pantalla de referencia: copia su forma al crear una nueva. `ui/info/`
es el segundo ejemplo, con tarjetas propias de pantalla y apertura de enlaces externos.
`ui/oro/` es el tercero: formulario reactivo sobre un motor de dominio, con el estado
ya formateado en el `UiState`.

### Componentes compartidos

`ui/components/` tiene el armazón que usan todas las pantallas salvo la portada:

- **`JewelryScaffold`** — barra superior, contenido y barra inferior opcional. **Cada
  pantalla declara aquí su propio *chrome***; no hay un `Scaffold` global que deduzca
  las barras husmeando la ruta actual.
- **`JewelryTopBar`** — sin `title` pinta el logo centrado (zonas principales); con
  `title` y `onBack` pinta flecha y nombre de sección. `onInfo` es **nulable**: la
  pantalla de información lo pasa a `null` y el icono se cambia por un hueco de 48 dp,
  porque un atajo a la pantalla en la que ya estás no significa nada.
- **`JewelryBottomBar`** — solo en Home, Favoritos y Ajustes. `MainTab` es su enum de
  destinos.
- **`ModuleCard`** — tarjeta del menú, con color de acento por módulo.
- **`DiamondDivider`** (en `Ornamentos.kt`) — filete dorado con rombo al centro. Nació
  privado en la portada; lo comparten ahora la portada y las tarjetas de información.
  `widthFraction` lo ajusta al hueco: 0.7 en pantalla completa, 1 dentro de una tarjeta.
- **`TarjetaAcento`** (en `Tarjetas.kt`) — envoltorio de tarjeta con degradado y borde
  del color de acento (dorado por defecto). Nació privada en Info como `TarjetaDorada`;
  la comparten Info y la calculadora de oro (que la usa también en teal).
- **`SelectorSegmentado`** — fila de opciones excluyentes con píldora degradada y check
  en el acento. Hecho a mano: `SegmentedButton` de Material impone su geometría.

`JewelryBottomBar` y el botón de la portada **no usan los componentes de Material**:
`NavigationBar` impone su propia altura y una píldora tras el icono activo, y `Button`
impone un contenedor opaco que taparía el fondo. Ambos están escritos a mano a
propósito.

### Iconos

**`material-icons` no está en el classpath**: Material 3 1.4.0 dejó de arrastrarlo, así
que `Icons.Default.*` no compila. Los iconos son vectores propios en `res/drawable`
(`ic_home`, `ic_favoritos`, `ic_ajustes`, `ic_chevron`, `ic_info`, `ic_atras`,
`ic_linkedin`, `ic_instagram`, `ic_enlace_externo`), de trazo
1.5–1.8 y tintados en tiempo de ejecución con `Icon(tint = ...)`. Si necesitas uno
nuevo, dibújalo ahí en lugar de añadir la librería, que está deprecada.

### Pantallas aún sin desarrollar

`ui/placeholder/PlaceholderScreen` es **un composable parametrizado** que sirve a los
cinco destinos pendientes. Recibe `title` (traducible) y `analyticsName` (identificador
estable para telemetría, que no debe traducirse). Cuando un destino reciba su feature
real, cambia solo su cableado en `AppNavHost`.

### Contrato de ViewModel

- Expone **un único** `StateFlow` inmutable de un data class propio de la pantalla
  (`HomeUiState`). Nada de `LiveData` ni de estado mutable público.
- No importa `androidx.compose.*`: no conoce a su vista.
- Recibe todo por constructor. Para corrutinas usa el `DispatcherProvider` inyectado,
  nunca `Dispatchers.IO` directo — es lo que permite testearlo con `TestDispatcher`.

### Contrato de pantalla

Cada pantalla se parte en dos Composables: uno resuelve el ViewModel con
`koinViewModel()` y otro pinta, sin estado y con `@Preview`. Ver `ui/home/HomeScreen.kt`.

### Navegación

Rutas **type-safe** con `@Serializable` en `ui/navigation/Routes.kt`, registradas con
`composable<Route.X>` en `AppNavHost.kt`. No se usan rutas como String.

## Añadir dependencias a Koin

1. Registra en el módulo que toque de `core/di/` (`coreModule`, `dataModule`,
   `domainModule`, `viewModelModule`).
2. Los repositorios van **siempre por su interfaz de dominio**:
   `single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }`.
   Nunca registres la implementación como tipo público.
3. Los ViewModel van con `viewModelOf(::MiViewModel)`.
4. Si creas un **módulo nuevo**, añádelo a `featureModules` en `core/di/AppModule.kt`.
   Eso basta para que `KoinModulesTest` lo verifique automáticamente.

`KoinModulesTest` recorre el grafo con `verify()` y falla si a algún constructor le
falta una dependencia. `firebaseModule` queda fuera de ese `verify()` a propósito:
sus definiciones nacen de fábricas estáticas (`Firebase.analytics`), no de
constructores, así que la reflexión acabaría inspeccionando tipos internos de Google
Play Services. Sus tipos entran como `extraTypes`.

## Dependencias y versiones

**Todo pasa por `gradle/libs.versions.toml`.** Nunca escribas coordenadas ni versiones
sueltas en un `build.gradle.kts`.

- Cuando hay BoM (Firebase, Compose, Koin) se usa, y los artefactos se declaran
  **sin versión**.
- **La versión de Kotlin la fija AGP**: AGP 9.3.1 trae Kotlin integrado 2.2.10 y por
  eso no existe el plugin `kotlin-android` en este proyecto. No añadas ese plugin ni
  subas `kotlin` por libre en el catálogo.
- Compose BOM 2026.08.00 exige `compileSdk 37`. `targetSdk` se queda en 36 a
  propósito: subirlo opta a comportamientos nuevos de runtime y es una decisión
  aparte que requiere probar la app.
- Las APIs `.ktx` de Firebase **ya no existen** en el BoM 34: los import correctos son
  `com.google.firebase.Firebase`, `com.google.firebase.analytics.analytics` y
  `com.google.firebase.crashlytics.crashlytics`.

## Firebase

`app/google-services.json` está **en `.gitignore` y no se commitea**. Un clon nuevo no
compila hasta descargarlo de la consola de Firebase (proyecto `calculadora-de-joyeros`)
y dejarlo en `app/`.

El `applicationId` es `com.jrblanco.calculadoradejoyeros2021` aunque el repo se llame
`...2026`: el `google-services.json` está registrado contra ese package y cambiarlo
obliga a dar de alta una app Android nueva en Firebase.

## Builds de release

`release` tiene R8 activado (`optimization { enable = true }`). El bloque
`uploadCrashlyticsMappingFileRelease` corre **dentro de `assembleRelease`**, así que el
mapping se sube a Crashlytics solo y los stack traces de producción llegan
desofuscados con nombre de fichero y número de línea.

- **No añadas `-keepattributes SourceFile,LineNumberTable`**: R8 ya guarda esa
  información en el propio mapping. Está verificado con `retrace`, y esa regla sería
  ruido heredado de ProGuard.
- Las reglas de R8 van en `app/src/main/keepRules/*.keep`. AGP 9 las recoge solas: en
  este proyecto no existe `proguardFiles` ni `proguard-rules.pro`.
- **No hay `signingConfig` de release todavía**: `assembleRelease` produce
  `app-release-unsigned.apk`. Para probar un release en el emulador hay que firmarlo a
  mano; hace falta un keystore antes de poder publicar.

```bash
# probar un release localmente (firma con la clave de debug, no vale para publicar)
$ANDROID_HOME/build-tools/37.0.0/apksigner sign --ks ~/.android/debug.keystore \
  --ks-pass pass:android --key-pass pass:android --out /tmp/rel.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk

# desofuscar un stack trace a mano
$ANDROID_HOME/cmdline-tools/latest/bin/retrace \
  app/build/outputs/mapping/release/mapping.txt traza.txt
```

## Commits

Conventional Commits (`feat:`, `fix:`, `build:`, `test:`, `docs:`, `chore:`,
`refactor:`), en español. Es también lo que genera `/speckit-git-commit`.
