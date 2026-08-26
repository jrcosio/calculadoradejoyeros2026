# Quickstart: verificar Favoritos

**Feature**: `009-favoritos` | **Fecha**: 2026-08-26

Cómo comprobar que la feature funciona de punta a punta. No es una guía de implementación: eso está
en `tasks.md`.

## Requisitos

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

Un emulador o dispositivo arrancado para los tests instrumentados y la verificación manual.
`app/google-services.json` en su sitio (no está en git). Para Favoritos **no** hace falta
`RAPIDAPI_KEY`: sólo lo necesita la sub-herramienta de precios.

## Puertas automáticas

Las cinco tienen que quedar en verde antes de dar la feature por cerrada:

```bash
./gradlew :app:testDebugUnitTest              # dominio, datos, ViewModels y paridad de formato
./gradlew :app:lint                           # MissingTranslation / ExtraTranslation
./gradlew :app:assembleDebug
./gradlew :app:compileDebugAndroidTestKotlin
./gradlew :app:connectedDebugAndroidTest      # DAO, data source real y pantalla
```

Comprobaciones puntuales útiles durante el desarrollo:

```bash
# el procesador de Room corre y genera los _Impl
./gradlew :app:kspDebugKotlin

# el esquema exportado existe y está commiteado
find app/schemas -type f

# el test que legitima la duplicación del redondeo
./gradlew :app:testDebugUnitTest --tests "*FavoritosParidadFormatoTest"

# el test dorado de la firma canónica
./gradlew :app:testDebugUnitTest --tests "*CodificadorFavoritoTest"
```

Los resultados en XML quedan en `app/build/test-results/testDebugUnitTest/`; son más rápidos de
leer que el HTML cuando algo falla.

## Recorrido manual en el emulador

Lo que ningún test cubre. Con la app instalada (`./gradlew :app:installDebug`):

**Guardar (US1)**

1. Oro → 30 gr, 18 K, Blanco → «Guardar en favoritos». Debe avisar de que queda guardado.
2. Volver a pulsar sin cambiar nada. Debe avisar de que **ya estaba**, y Favoritos seguir con una
   sola tarjeta.
3. Limpiar y pulsar «Guardar en favoritos» con el campo vacío. Debe pedir que se complete el
   cálculo, y no añadir nada.
4. Guardar uno desde cada calculadora: plata, soldaduras (las tres familias), soldadura BASE y peso
   de chapas. Seis tarjetas en total.
5. Cerrar la app desde el selector de aplicaciones, volver a abrirla y entrar en Favoritos. Todo
   sigue.

**La lista (US4)**

6. Cada tarjeta lleva su imagen de sección, su nombre y su color: dorado en oro y soldaduras,
   plateado en plata, turquesa en chapas.
7. La tarjeta de oro muestra plata fina, cobre y paladio, y el peso total.
8. Guardar una soldadura BASE en **modo inverso** (peso final): produce cinco cifras, así que su
   tarjeta debe mostrar tres y decir que quedan dos.
9. Las tarjetas están ordenadas de la más reciente a la más antigua, y cada una muestra su fecha.

**Reabrir (US2)**

10. Pulsar la tarjeta de oro: llega a la calculadora con 30, 18 K y Blanco puestos y el resultado
    calculado.
11. Cambiar 30 por 60: el resultado se recalcula. Volver atrás → Favoritos, y la tarjeta sigue
    diciendo 30.
12. Reabrir, cambiar a 60 y «Guardar en favoritos»: aparece una tarjeta nueva y la de 30 se queda.
13. Pulsar la tarjeta de una soldadura de ORO LEY: llegan familia, dureza, color, **modo** y
    cantidad. Que el modo llegue bien es lo que distingue «tengo 10 gr de oro» de «quiero 10 gr de
    soldadura».
14. Pulsar la tarjeta de peso de chapas: abre Herramientas con la sub-herramienta de chapas ya
    elegida y las tres medidas puestas. **Y no debe consultar la API de precios** — revisar el log
    de red: `PRECIO METALES` no se ha compuesto.
15. Reabrir un favorito, editar la cantidad y **cambiar el tamaño de letra del sistema al máximo**
    sin salir de la pantalla. Lo editado debe seguir ahí: si vuelve al valor del favorito, el
    guardián de idempotencia está mal.

**Quitar (US3)**

16. Pulsar la estrella de una tarjeta: aparece la pregunta, nombrando ese favorito.
17. Cancelar: no se quita nada. Tocar fuera del diálogo: tampoco.
18. Confirmar: la tarjeta desaparece y las demás siguen.
19. Quitar todas: aparece la invitación de lista vacía, que nombra el botón «Guardar en favoritos».
20. Repetir un cálculo quitado y guardarlo: vuelve a entrar.

**Idiomas y accesibilidad**

21. Ajustes → Deutsch. Favoritos: nombres de sección, títulos, nombres de metal, fecha y los dos
    botones del diálogo en alemán, sin texto cortado. Los favoritos guardados con la app en español
    se leen en alemán.
22. Repetir en inglés, francés e italiano.
23. `adb shell cmd locale set-app-locales com.jrblanco.calculadoradejoyeros2021 --locales fr-FR` con
    «Automático» elegido: la app en francés.
24. Con el tamaño de letra del sistema al doble: la lista sigue legible y hace scroll; los títulos
    se cortan con puntos suspensivos, no desbordan.
25. Con TalkBack encendido: recorrer la lista (cada tarjeta se anuncia como una frase), abrir un
    favorito, y **quitar uno** — la estrella tiene que ser un nodo enfocable propio. Si TalkBack no
    la encuentra, la fusión de semántica se la ha tragado.

**Persistencia y release**

26. Reiniciar el dispositivo: los favoritos siguen.
27. `adb backup` / `adb restore`, o transferir a otro emulador: los favoritos viajan.
28. Firmar un `assembleRelease` a mano, instalarlo, guardar un favorito y reabrirlo: descarta que R8
    haya roto el discriminador de tipo.

```bash
./gradlew :app:assembleRelease
$ANDROID_HOME/build-tools/37.0.0/apksigner sign --ks ~/.android/debug.keystore \
  --ks-pass pass:android --key-pass pass:android --out /tmp/rel.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk
adb install -r /tmp/rel.apk
```

**Volumen (SC-009)**

29. Sembrar 50 favoritos y comprobar que la lista sigue siendo instantánea. Con la app instalada y
    al menos un favorito guardado a mano (para que la base exista):

Sin `sqlite3` en el dispositivo, la siembra se hace fuera y se devuelve el fichero. Cerrar la app
antes para que Room no tenga la base abierta:

```bash
PKG=com.jrblanco.calculadoradejoyeros2021
adb shell am force-stop $PKG
adb exec-out run-as $PKG cat databases/favoritos.db > /tmp/favoritos.db
python3 -c "
import sqlite3, json
c = sqlite3.connect('/tmp/favoritos.db')
for i in range(1, 51):
    datos = json.dumps({'version': 1, 'cantidad': str(i), 'ley': 'LEY_18K', 'color': 'AMARILLO'})
    c.execute('INSERT OR IGNORE INTO favoritos (tipo, firma, datosJson, guardadoEnEpochMillis) VALUES (?,?,?,?)',
              ('oro', f'oro|v1|masa={i}|color=AMARILLO|ley=LEY_18K', datos, 1787670000000 + i))
c.commit()
print('filas:', c.execute('SELECT COUNT(*) FROM favoritos').fetchone()[0])"
adb push /tmp/favoritos.db /data/local/tmp/favoritos.db
adb shell run-as $PKG cp /data/local/tmp/favoritos.db databases/favoritos.db
```

Abrir la pestaña Favoritos: el listado debe aparecer completo sin espera perceptible y desplazarse
sin tirones. Si no cumple, la salida ya está prevista en el diseño: mover el mapeo del ViewModel a
`dispatchers.default` con `.flowOn(...)`. Ojo: el `datosJson` de arriba tiene que coincidir con lo
que produzca `CodificadorFavorito`; si el formato del DTO cambia, este bucle hay que actualizarlo o
las 50 filas saldrán descartadas de la lista (que también es una prueba válida de FR-034).

## Inspeccionar la base a mano

Útil cuando una firma no cuadra. En un emulador con root de debug:

**Las imágenes de emulador no traen `sqlite3`** (verificado: `run-as: exec failed for sqlite3`),
así que la base se trae y se consulta en el ordenador:

```bash
PKG=com.jrblanco.calculadoradejoyeros2021
adb exec-out run-as $PKG cat databases/favoritos.db > /tmp/favoritos.db
python3 -c "
import sqlite3
for f in sqlite3.connect('/tmp/favoritos.db').execute(
        'SELECT id, tipo, firma, guardadoEnEpochMillis FROM favoritos ORDER BY id DESC'):
    print(f)"
```

La firma es legible a propósito (`oro|v1|masa=30|color=AMARILLO|ley=LEY_18K`): si dos favoritos que
deberían ser el mismo tienen firmas distintas, se ve en qué campo divergen sin descifrar un hash.
