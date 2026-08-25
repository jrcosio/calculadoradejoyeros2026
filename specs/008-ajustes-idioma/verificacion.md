# Verificación de la feature 008 y desviaciones del plan

**Fecha**: 2026-08-25 · **Rama**: `008-ajustes-idioma` · **Emulador**: Pixel 10 (AVD), Android 17,
imagen de producción (sin root), dispositivo en `en-US`

Lo que se comprobó, cómo, y en qué se apartó la implementación del plan. Mismo papel que el
documento equivalente de la 007.

## Puertas automáticas

| Puerta | Resultado |
|---|---|
| `./gradlew :app:testDebugUnitTest` | **341 tests, 0 fallos** (28 nuevos de esta feature, `TraduccionesTest` incluido con sus 8) |
| `./gradlew :app:lint` | **0 errores, 19 avisos**, todos anteriores a esta feature. Ni un `MissingTranslation` ni un `ExtraTranslation` |
| `./gradlew :app:assembleDebug` | En verde |
| `./gradlew :app:compileDebugAndroidTestKotlin` | En verde |
| `./gradlew :app:connectedDebugAndroidTest` (los dos tests nuevos) | **9 tests, 0 fallos** |
| `./gradlew :app:assembleRelease` + firma de debug | En verde; el selector funciona en el APK optimizado con R8 |

## Verificación en el emulador

| # | Comprobado | Resultado |
|---|---|---|
| 1 | Instalación limpia con el dispositivo en inglés | Portada, menú y calculadoras en inglés sin configurar nada (SC-001, FR-008) |
| 2 | Ajustes, primera visita | «Automatic» marcado, «Follows the device language · English», las cinco banderas con su endónimo (FR-002…FR-005) |
| 3 | Tocar la bandera alemana | Título, sección, descripción y las tres etiquetas de la barra inferior pasan a alemán **sin salir de la pantalla** (SC-002, FR-006) |
| 4 | Recorrido en alemán: portada, menú, plata, soldaduras, herramientas, precios, chapas | Ni una isla en español; el cálculo de 25 g → «Kupfer 2,000 gr / Silber 925 insgesamt: 27,000 gr» (SC-003, FR-015) |
| 5 | Barra inferior, botones dorados y tarjetas del menú en alemán | Nada cortado. «In Favoriten speichern» cabe en una línea por el autoajuste nuevo (SC-004, FR-021) |
| 6 | Cierre forzado y reapertura | Sigue en alemán ya en la portada, sin destello de otro idioma (SC-005, FR-007, FR-013) |
| 7 | El sistema entrega francés (`cmd locale`) con alemán elegido | La app sigue en alemán; «Automatisch» muestra «Français» como detectado (SC-005, FR-011) |
| 8 | Tocar «Automatisch» | La app pasa a francés al instante y «Automatique» queda marcado (SC-006, FR-012) |
| 9 | Dispositivo en portugués (`pt-PT`) y datos borrados | La app abre en español, «Automático · Español» (FR-009) |
| 10 | Precios: cambiar de idioma con las cotizaciones cargadas | No se recarga («Saved data from the last request», misma hora) y la fecha pasa a «Updated Aug 25, 2026 · 7:20 PM» (FR-019, FR-023) |
| 11 | Tamaño de letra del sistema al doble, en alemán | Todo legible y sin cortes tras el arreglo de la barra inferior (SC-004) |
| 12 | Telemetría (logcat con `debug.firebase.analytics.app`) | `screen_view` con `ga_screen=ajustes` —el nombre del placeholder— y `ajustes_idioma` con `idioma=it` y `idioma=automatico` (FR-024) |
| 13 | APK de release firmado con la clave de debug | El selector y DataStore funcionan con R8 activo |

## Desviaciones del plan de tareas

1. **T034 (la acción «Automático») se implementó con T021** y T030 con T024: separar el `when` del
   ViewModel y la fila de la pantalla en dos pasadas habría sido trabajo artificial sobre el mismo
   fichero. Los tests de US2 y US3 sí se escribieron en su fase.
2. **T042 (el test de paridad) se adelantó a T039–T041**: escribirlo antes de tener los cuatro
   ficheros lo habría dejado en rojo, y el orden de dependencias de `tasks.md` ya lo pedía después.
   El alemán se validó con el mismo test, no a ojo.
3. **T026 descubrió una cadena mal clasificada**: `welcome_developer` («Desarrollado por José Ramón
   Blanco») estaba marcada entera como no traducible, así que la portada en inglés mostraba una
   frase en español. Se partió en `%1$s` + `info_perfil_nombre`, igual que `precios_fuente`. El
   recuento de no traducibles pasó de 21 previstas a **33 reales**, con una regla explícita en el
   contrato: no se traduce lo que no lleva ni una palabra dentro.
4. **Dos hallazgos de `lint` obligaron a cambios que el plan no preveía**:
   - `LocalContextConfigurationRead` (error): `ProveedorIdioma` leía la configuración del contexto en
     vez de `LocalConfiguration`, y con eso un cambio real del sistema —el tamaño de letra— dejaba el
     contexto localizado con la configuración vieja. Corregido (R2).
   - `AppBundleLocaleChanges` (aviso): sin `bundle { language { enableSplit = false } }`, un App
     Bundle publicado en Play instalaría solo el idioma del dispositivo y la feature no funcionaría en
     producción. Añadido (R13).
5. **La fecha de precios no seguía al idioma elegido** (R12): `DateUtils.formatDateTime` toma el
   orden de la fecha de `Locale.getDefault()`. Se cambió a `DateFormat.getMediumDateFormat(contexto)`
   y `getTimeFormat(contexto)`. Es un cambio en un fichero de la 007 que el plan no listaba.
6. **`JewelryBottomBar` necesitó más que autoajuste**: con la fuente al doble, «Einstellungen» se
   cortaba incluso a 8 sp. Las pestañas pasan de 96 dp fijos a un tercio del ancho cada una
   (`weight(1f)`, con 96 dp de mínimo) y el autoajuste baja a 6 sp, como `SelectorSegmentado`. De
   paso, la zona pulsable crece.
7. **La bandera de España va sin escudo y la británica está redibujada**, como preveía el plan:
   `espana.svg` trae 172 paths, 94 `<ellipse>` y 7 `<use>`, y `<use>` no existe en VectorDrawable.
   Las cinco banderas se ven correctas en el emulador.

## Lo que no se pudo verificar y por qué

- **Cambiar el idioma del sistema entero** (no el que se entrega a la app): la imagen del emulador es
  de producción y `adb root` no está disponible, así que `setprop persist.sys.locale` falla en
  silencio. Se usó `cmd locale set-app-locales`, que cambia justo lo que lee `IdiomaSistemaJvm`
  (`Locale.getDefault()`), de modo que la regla de precedencia y el respaldo quedan comprobados con
  datos reales del sistema.
- **TalkBack**: no se activó el lector de pantalla. Lo que sí está cubierto es la semántica que
  necesita: `AjustesScreenTest` comprueba que solo una fila está seleccionada (`Role.RadioButton`
  dentro de `selectableGroup`), y el check de la fila activa lleva `contentDescription`
  (`ajustes_idioma_activo`). Las descripciones de imagen se traducen y `TraduccionesTest` lo vigila.
- **La copia de seguridad con `bmgr`**: no se ejecutó el ciclo completo de respaldo y restauración.
  Lo que se comprobó es que el fichero de DataStore no aparece en ninguna exclusión de
  `res/xml/backup_rules.xml` ni de `res/xml/data_extraction_rules.xml`, que es la condición de
  FR-025, y ambos ficheros lo dicen ahora en un comentario para que nadie lo excluya por inercia.

## Deuda conocida que esta feature no aborda

- **El formato numérico no está localizado** (decisión del autor, FR-020): en inglés se sigue viendo
  «127,89 €/g» con coma decimal. Alemán, francés e italiano coinciden con el español, así que solo
  chirría en inglés. Feature aparte.
- **La moneda sigue cableada al euro**, en los recursos y en la petición al proveedor.
- **`unidad_gramos` vale «gr»** y el símbolo del SI es «g». Cambiarlo se ve en las cinco
  calculadoras y no pertenece a esta feature.
- **No hay integración con el selector de idioma del sistema** (API 33+): sería una segunda fuente de
  verdad, y la spec no la pide (R9).
