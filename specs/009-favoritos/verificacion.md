# Verificación: Favoritos

**Feature**: `009-favoritos` | **Fecha**: 2026-08-26 | **Rama**: `009-favoritos`

Qué se comprobó de verdad, qué queda pendiente y en qué se desvió la implementación del plan.

## Puertas automáticas

| Puerta | Resultado |
|---|---|
| `./gradlew :app:testDebugUnitTest` | **440 tests, 0 fallos** |
| `./gradlew :app:lint` | **en verde** (`MissingTranslation` / `ExtraTranslation`) |
| `./gradlew :app:assembleDebug` | **en verde** |
| `./gradlew :app:compileDebugAndroidTestKotlin` | **en verde** |
| `./gradlew :app:connectedDebugAndroidTest` | **116 de 124 en verde**; los 8 fallos son **preexistentes** (ver abajo) |

Los 20 tests instrumentados de esta feature (11 de pantalla, 9 de Room) pasan todos.

### Los 8 fallos instrumentados son de antes de la feature

El emulador de esta máquina está en **`en-US`**, y ocho tests de otras features dependen del idioma
del dispositivo: `WelcomeScreenTest` busca literales en español, el helper de
`PreciosMetalesScreenTest` formatea cadenas con y sin argumentos, y dos tests de botones no
encuentran su nodo con las etiquetas inglesas.

**No es una suposición**: se creó un *worktree* de git en `e72169a` —el commit anterior a esta
feature— y **los mismos tests fallan igual** allí. Los cuatro comprobados
(`PesoChapasScreenTest.losBotonesPropaganSusCallbacks`,
`SoldadurasScreenTest.conFamilia_losBotonesExistenYPropagan` y los dos de `WelcomeScreenTest`)
fallan en el código base. Es deuda anterior, y arreglarla es una feature aparte: hay que dejar de
depender del locale del dispositivo en los tests instrumentados.

`persist.sys.locale` no se puede cambiar en este AVD (`adbd cannot run as root in production
builds`), exactamente como advierte `CLAUDE.md`.

## R1 verificada compilando, antes de escribir el plan

La única incógnita capaz de tumbar la feature era si KSP funciona con el Kotlin que AGP 9 lleva
dentro. Se resolvió con un spike de build desechable **antes** de redactar `plan.md`, no después:

- `:app:kspDebugKotlin` en verde, con `FavoritosDao_Impl.kt` y `FavoritosDatabase_Impl.kt` generados
- `app/schemas/…FavoritosDatabase/1.json` exportado por el plugin `androidx.room`
- `:app:assembleDebug` y `:app:lint` en verde, con la caché de configuración funcionando
- **`room-ktx` no hace falta**: `withTransaction`, `databaseBuilder` y `setJournalMode` resuelven
  desde `room-runtime`

Combinación confirmada: **Room 2.8.4 + KSP 2.3.11 + AGP 9.3.1 (Kotlin 2.2.10)**. El spike se
deshizo por completo antes de empezar la implementación real.

## Recorrido manual en el emulador

Hecho con `adb input` y capturas, con la app en inglés (el locale del emulador, con «Automático»):

| Paso | Resultado |
|---|---|
| Guardar con el campo vacío | No se crea `favoritos.db`: **el almacén no se toca** |
| 30 g de 18 K blanco → «Save to favourites» | `favoritos.db` creado (28 KB) con **una** fila |
| La fila real | `firma = oro\|v1\|masa=30\|color=BLANCO\|ley=LEY_18K` — idéntica al test dorado |
| El esquema real | `CREATE UNIQUE INDEX index_favoritos_firma ON favoritos (firma)` |
| El journal | `favoritos.db-journal`, **sin `-wal` ni `-shm`**: TRUNCATE confirmado |
| Pulsar «Guardar» una segunda vez | **Sigue habiendo una sola fila** (FR-006, FR-009) |
| La tarjeta de Favoritos | Imagen de sección, «GOLD alloys», título «18 K · White · 30 gr», las tres cifras, el total y «Saved on Aug 26, 2026» |
| Las cifras de la tarjeta | **3,939 / 1,614 / 4,408 / 39,960**, idénticas a las de la calculadora (SC-006) |
| Pulsar la tarjeta | Abre la calculadora con 30, 18 K y White puestos, y el resultado calculado |
| Pulsar la estrella | Diálogo «Remove from favourites?» nombrando «18 K · White · 30 gr» |
| Cancelar | **1 fila**: no se quita nada |
| Confirmar | **0 filas**, y la pantalla muestra la invitación con el nombre del botón |

Capturas en el directorio de trabajo de la sesión (portada, home, calculadora, tarjeta, diálogo y
estado vacío).

## Pendiente de verificar

Nada de esto está roto: **no se ha comprobado**, y hace falta un humano o un emulador distinto.

- **Los cinco idiomas** (pasos 21–24 del `quickstart.md`). Se vio la app en inglés y las cadenas
  nuevas salieron traducidas, pero no se recorrió alemán, francés e italiano buscando desbordes. El
  alemán es el peor caso, y los dos botones del diálogo a `weight(1f)` con «Abbrechen» y «Entfernen»
  son el punto a mirar.
- **TalkBack** (paso 25 y SC-008). El `semantics(mergeDescendants = true)` de la estrella está en su
  sitio y el test instrumentado comprueba que la estrella propaga lo suyo y no abre el favorito, pero
  el recorrido con lector de pantalla real no se ha hecho.
- **Tamaño de letra del sistema al doble** (paso 24).
- **Volumen con 50 favoritos** (paso 29, SC-009). El mapeo corre en `dispatchers.main`; si no
  cumpliera, la salida ya está prevista: `.flowOn(dispatchers.default)`.
- **Copia de seguridad y móvil nuevo** (pasos 26–27, FR-033).
- **`assembleRelease` firmado a mano** (paso 28). Es el que descarta que R8 rompa el discriminador de
  tipo, y no hay `signingConfig` de release en el proyecto.
- **Las otras cuatro calculadoras** se probaron por test (unitario e instrumentado) pero no a mano en
  el emulador: solo se recorrió la de oro de punta a punta.
- **Abrir un favorito de chapa sin que se componga `PreciosMetalesSection`** (paso 14): el diseño lo
  garantiza por el orden del `when`, y hay test del ViewModel, pero no se miró el log de red.

## Desviaciones respecto a `tasks.md`

1. **Las cadenas se añadieron todas de una vez** (T034, T042, T067 y T076 juntas) en lugar de una
   tanda por historia. Motivo: `TraduccionesTest` y `lint` fallan con un fichero a medias, así que
   partirlo en cuatro tandas habría dejado la puerta en rojo entre tarea y tarea sin ganar nada.
2. **`TarjetaFavorito` se escribió completa de una vez** (T048 + T069 + T078): la estrella, la imagen
   de sección, el acento, el recorte a tres y la fecha son un solo fichero, y escribirlo tres veces
   era trabajo tirado.
3. **T046 no estaba en el plan original**: la añadió el `/speckit-analyze`, que detectó que nueve
   mapeos enum→recurso eran `private` de tres pantallas ajenas y que la tarea del título **no era
   implementable** sin promoverlos. Se crearon `ui/oro/PresentacionOro.kt` y
   `ui/plata/PresentacionPlata.kt`, y seis mapeos más bajaron a `PresentacionSoldadura.kt`. Se añadió
   además `etiquetaModoRes` / `etiquetaModoBaseRes`, que no existían: las cuatro claves
   `soldadura_modo_*` se usaban en línea.
4. **`ResumenFavorito.Chapa` no lleva pureza ni densidad**, y `FormatoFavoritos` ganó un
   `tresDecimales` que el plan no nombraba: la tarjeta de chapa muestra volumen y metal fino, y los
   dos van a tres decimales como en su calculadora.
5. **`ResultadoGuardado` ganó `analyticsId`** (`"nuevo"` / `"repetido"`), que el plan daba por hecho
   sin decir dónde vivía.
6. **El `quickstart.md` se corrigió después de usarlo**: las imágenes de emulador no traen `sqlite3`,
   así que los dos bloques que lo invocaban en el dispositivo ahora se traen la base con
   `run-as … cat` y la consultan con `python3`.
7. **Un test se escribió con la expectativa equivocada** y se corrigió con el documento delante: se
   asumió que 100 g de plata a 950‰ daban 105,26 de masa final, cuando el motor parte de plata de
   999‰ y da 105,157894…. El código estaba bien; la expectativa, no.

## Estado

Las 88 tareas de `tasks.md` están cerradas salvo lo que la sección «Pendiente de verificar» enumera,
que es verificación manual y no implementación. La feature está completa y las cuatro puertas
automáticas están en verde.
