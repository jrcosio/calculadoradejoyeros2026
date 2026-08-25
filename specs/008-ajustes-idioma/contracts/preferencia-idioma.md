# Contrato: la preferencia de idioma persistida

**Feature**: `008-ajustes-idioma` | **Consumidor único**: `data/source/local/DataStoreAjustesLocalDataSource`

El único dato que esta feature escribe en el dispositivo. Es un contrato con el futuro: lo que hoy se
guarda tiene que seguir entendiéndose cuando la app cambie, y lo que no se entienda no puede romper
el arranque.

## Almacén

| Aspecto | Valor |
|---|---|
| Tecnología | Preferences DataStore (`androidx.datastore:datastore-preferences`) |
| Nombre | `ajustes` |
| Ruta real | `/data/data/com.jrblanco.calculadoradejoyeros2021/files/datastore/ajustes.preferences_pb` |
| Instancia | única por proceso, garantizada por el `single` de Koin |
| Copia de seguridad | **incluido** (las reglas de `res/xml/` solo excluyen `cotizaciones.xml`) |

No se toca el fichero `cotizaciones` de `SharedPreferences` (feature 007): son dos almacenes con
dueños distintos y ciclos de vida distintos.

## Claves

| Clave | Tipo | Valores válidos | Ausente significa |
|---|---|---|---|
| `idioma` | `String` | `es`, `en`, `fr`, `de`, `it` | «Automático»: la app sigue al idioma del dispositivo |

Una sola clave. El valor es la `etiquetaBcp47` de `IdiomaApp`, en minúsculas y sin región: es la misma
cadena que nombra la carpeta de recursos (`values-de` ↔ `de`), de modo que no hay dos tablas de
conversión que puedan desincronizarse.

## Operaciones

```
leer   : Flow<IdiomaApp?>          — emite al suscribirse y en cada cambio
guardar: (IdiomaApp?) -> Unit      — suspend; null elimina la clave
```

- **Guardar un idioma**: escribe `idioma = <etiqueta>`.
- **Guardar «Automático»** (`null`): **elimina la clave**. No se escribe un valor centinela como
  `auto`: la ausencia ya significa eso, y así el estado inicial y el elegido a mano son idénticos —lo
  que hace que FR-012 no necesite migración ninguna.

## Tolerancia a lo que no se entiende

| Situación | Comportamiento | Requisito |
|---|---|---|
| Clave ausente | `null` → la app sigue al dispositivo | FR-008 |
| Valor `pt`, `xx` o cualquier etiqueta desconocida | `null`, **sin borrar el valor guardado** | FR-014 |
| Valor con región (`es-ES`) | se resuelve a `ESPANOL` por `IdiomaApp.desdeEtiqueta` | FR-010 |
| Valor vacío o en blanco | `null` | FR-014 |
| `IOException` al leer (fichero corrupto, sin permisos) | se emite vacío y la app sigue al dispositivo, sin fallo visible | FR-014 |

**El valor desconocido no se borra**, al contrario que la caché de cotizaciones de la 007, que sí
descarta lo que no entiende. La diferencia es de naturaleza del dato: la caché es derivada y se puede
volver a pedir, mientras que esto es una decisión del joyero. Si una versión futura añade un sexto
idioma y el joyero vuelve a esta, su elección le espera intacta.

## Compatibilidad hacia delante

- Añadir un idioma = añadir un valor al enum y su carpeta de recursos. Las versiones anteriores lo
  leerán como desconocido y seguirán al dispositivo: degradación limpia, sin migración.
- Añadir un ajuste nuevo (tema, unidad por defecto) = una clave nueva en el mismo fichero. Las
  versiones anteriores la ignoran.
- No hay número de versión del formato: con una sola clave de tipo cadena y tolerancia a lo
  desconocido no aporta nada. (La 007 sí lo lleva porque persiste un objeto compuesto.)

## Qué se prueba y cómo

El data source es pegamento sobre el SDK y no se prueba en JVM, igual que
`SharedPreferencesCotizacionesLocalDataSource` (misma decisión, mismo motivo). Lo que se prueba es:

- `IdiomaAppTest`: la conversión etiqueta ↔ idioma, que es donde está toda la lógica del contrato.
- `PreferenciasRepositoryImplTest` con `FakeAjustesLocalDataSource`: que guardar y leer devuelven lo
  mismo y que `null` viaja entero.
- Verificación manual en el emulador (quickstart, pasos 3 y 4): cerrar la app y reabrirla, y cambiar
  el idioma del sistema con una elección guardada.
