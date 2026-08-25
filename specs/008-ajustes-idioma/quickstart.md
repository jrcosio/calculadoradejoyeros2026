# Quickstart: validar el idioma de la app de punta a punta

**Feature**: `008-ajustes-idioma` · **Fecha**: 2026-08-25

Guía de validación, no de implementación. Los criterios están en [spec.md](./spec.md)
(SC-001…SC-007); los contratos, en [contracts/](./contracts/).

## 0. Prerrequisitos

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

`app/google-services.json` presente, como en cualquier build del proyecto. La credencial
`RAPIDAPI_KEY` de la 007 no es imprescindible: sin ella la pantalla de precios muestra «servicio no
configurado», y ese mensaje también hay que verlo traducido.

## 1. Puertas automáticas

```bash
./gradlew :app:testDebugUnitTest    # IdiomaApp, SeleccionIdioma, los 2 casos de uso, el repositorio,
                                    # los 2 ViewModels con TestDispatcher, TraduccionesTest y KoinModulesTest
./gradlew :app:lint                 # MissingTranslation y ExtraTranslation: puerta de esta feature
./gradlew :app:assembleDebug
./gradlew :app:compileDebugAndroidTestKotlin
```

Resultados XML en `app/build/test-results/testDebugUnitTest/`, más rápidos de leer que el HTML.

Comprobación de que la traducción está completa **sin abrir el emulador** (cuenta de claves por
fichero; los cuatro números deben coincidir entre sí):

```bash
for f in en fr de it; do
  printf '%s: %s\n' "$f" "$(grep -c '<string ' app/src/main/res/values-$f/strings.xml)"
done
# y el base menos las no traducibles:
grep -c '<string ' app/src/main/res/values/strings.xml
grep -c 'translatable="false"' app/src/main/res/values/strings.xml
```

## 2. Verificación en emulador

Instalar el debug: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

Cambiar el idioma del sistema sin pelearse con los menús:

```bash
# El idioma que el sistema entrega a la app (API 33+, y **no necesita root**, al contrario que
# `setprop persist.sys.locale`, que en una imagen de producción falla en silencio):
adb shell cmd locale set-app-locales com.jrblanco.calculadoradejoyeros2021 --locales fr-FR
adb shell cmd locale get-app-locales com.jrblanco.calculadoradejoyeros2021
adb shell cmd locale set-app-locales com.jrblanco.calculadoradejoyeros2021 --locales ""  # quitarlo

# borrar la preferencia guardada de la app (vuelve al estado de recién instalada)
adb shell pm clear com.jrblanco.calculadoradejoyeros2021
# cierre forzado, sin borrar datos
adb shell am force-stop com.jrblanco.calculadoradejoyeros2021
# tamaño de letra del sistema al doble, para el barrido de desbordes
adb shell settings put system font_scale 2.0    # y `1.0` para volver
```

`cmd locale` cambia lo que `Locale.getDefault()` devuelve dentro de la app, que es exactamente lo
que lee `IdiomaSistemaJvm`: sirve igual que cambiar el idioma del móvil, y en un comando.

| # | Paso | Esperado | Criterio |
|---|---|---|---|
| 1 | Dispositivo en español, `pm clear`, abrir la app | Portada en español desde el primer texto | SC-001, FR-008 |
| 2 | Home → Ajustes | Título «Ajustes», sección «Idioma de la aplicación», fila «Automático» marcada con «Sigue al idioma del dispositivo · Español», debajo las cinco banderas con su nombre; barra inferior con la pestaña de Ajustes activa | FR-002…FR-005 |
| 3 | Tocar la bandera alemana | **Sin salir de la pantalla**: el título, la sección, «Automático» y las tres etiquetas de la barra inferior pasan a alemán; la marca de activo se mueve a la fila alemana | SC-002, FR-006 |
| 4 | Volver a Home y recorrer oro, plata, soldaduras, soldadura BASE, herramientas (las dos) e información | Todo en alemán, incluidos avisos, notas al pie, botones y mensajes de error | SC-003, FR-015 |
| 5 | En la misma pasada, mirar barra inferior, botones dorados y tarjetas del menú | Ninguna etiqueta cortada, partida a mitad de palabra ni desbordada | SC-004, FR-021 |
| 6 | Calculadora de plata: escribir 25, ir a Ajustes, cambiar a italiano y volver | Los 25 g y su resultado siguen ahí, sin alterarse | FR-022 |
| 7 | Herramientas → PRECIO METALES, esperar los precios, cambiar el idioma y volver | Los precios no se recargan, la fecha «Actualizado …» aparece con el mes en el idioma nuevo, y el proveedor no recibe consultas nuevas | FR-019, FR-023 |
| 8 | `am force-stop` y reabrir | Sigue en el idioma elegido, ya en la portada; **sin destello** de otro idioma en el primer fotograma | SC-005, FR-007, FR-013 |
| 9 | Cambiar el idioma del **sistema** a inglés y reabrir la app | Sigue en el idioma elegido: la elección manda | SC-005, FR-011 |
| 10 | Ajustes → «Automático» | La app pasa a inglés al instante; «Automático» queda marcado y muestra «English» como detectado | SC-006, FR-012 |
| 11 | `am force-stop`, poner el sistema en francés, reabrir | La app abre en francés: «Automático» siguió guardado | FR-012 |
| 12 | Poner el sistema en portugués (`pt-PT`), `pm clear`, abrir | La app abre en español y Ajustes muestra «Automático · Español» | FR-009, escenario 2 de la historia 2 |
| 13 | Recorrer los cinco idiomas mirando la lista de Ajustes | Los nombres se leen siempre «Español / English / Français / Deutsch / Italiano», y la marca de la app, los nombres propios, «Ask»/«Bid» y los símbolos no cambian nunca | FR-003, FR-018 |
| 14 | Ampliar el tamaño de letra del sistema al máximo con el idioma alemán activo | Etiquetas legibles, sin desbordes | SC-004, FR-021 |
| 15 | Con TalkBack, recorrer la lista de Ajustes y una calculadora | La fila activa se anuncia como seleccionada y las descripciones de imagen suenan en el idioma elegido | FR-016 |
| 16 | Panel de Firebase (DebugView) | `screen_view` con `"ajustes"` —el mismo nombre que emitía el placeholder— y `ajustes_idioma` con `idioma` = `de`/`en`/`automatico` | FR-024 |

## 3. Comprobación de la copia de seguridad (FR-025)

```bash
adb shell bmgr backupnow com.jrblanco.calculadoradejoyeros2021
adb shell pm clear com.jrblanco.calculadoradejoyeros2021
adb shell bmgr restore com.jrblanco.calculadoradejoyeros2021
```

Con un idioma elegido antes del respaldo, la app debe abrir en ese idioma tras la restauración. Si el
transporte de backup del emulador no está disponible, basta con verificar que
`files/datastore/ajustes.preferences_pb` **no** aparece en las exclusiones de
`res/xml/backup_rules.xml` ni de `res/xml/data_extraction_rules.xml`.

## 4. Tests instrumentados

```bash
./gradlew :app:connectedDebugAndroidTest --tests "*ProveedorIdiomaTest" --tests "*AjustesScreenTest"
```

`ProveedorIdiomaTest` es el que prueba el mecanismo: un texto envuelto en
`ProveedorIdioma(IdiomaApp.INGLES)` debe leerse en inglés aunque el dispositivo esté en español. Si
este test pasa, el paso 3 del emulador pasa.
