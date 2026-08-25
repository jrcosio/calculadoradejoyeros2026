# Contrato: el fichero de recursos y sus traducciones

**Feature**: `008-ajustes-idioma` | **Vigilado por**: `TraduccionesTest` (JVM) y `./gradlew :app:lint`

Cinco ficheros que tienen que decir lo mismo en cinco idiomas. Este es el contrato que lo hace
verificable en vez de confiable.

## Ficheros

| Fichero | Idioma | Papel |
|---|---|---|
| `app/src/main/res/values/strings.xml` | español | **fuente de verdad**: aquí se añade y se borra |
| `app/src/main/res/values-en/strings.xml` | inglés | traducción |
| `app/src/main/res/values-fr/strings.xml` | francés | traducción |
| `app/src/main/res/values-de/strings.xml` | alemán | traducción |
| `app/src/main/res/values-it/strings.xml` | italiano | traducción |

El español no tiene carpeta propia: es el fichero base, y por eso es también lo que ve un dispositivo
en un idioma no soportado sin que la app haga nada (FR-009).

## Reglas

1. **Toda clave sin `translatable="false"` existe en los cinco ficheros.** Ni una menos.
2. **Ninguna clave con `translatable="false"` aparece en un `values-xx`.** Traducir una marca es un
   fallo, no una mejora.
3. **Ninguna clave sobra**: un `values-xx` no puede declarar algo que no esté en `values/`.
4. **Los marcadores de formato se conservan exactamente**: el mismo conjunto de `%1$s`…`%4$s` y
   `%1$d` en cada idioma. Se pueden **reordenar** dentro de la frase (para eso son posicionales),
   pero no se puede añadir, quitar ni cambiar de tipo.
5. **El `%%` se conserva** donde lo haya (`chapas_pureza_formato`).
6. **Los `\n` se conservan** donde los haya (`welcome_title`, `info_perfil_etiquetas`).
7. **Los símbolos se copian tal cual**: `‰`, `³`, `€`, `…`, `·`, `«»`.
8. **El orden y los comentarios de sección se replican** en las cuatro traducciones, con los mismos
   bloques que el fichero base. No es cosmético: es lo que permite revisar dos ficheros en paralelo.

## Cadenas no traducibles (33)

Llevan `translatable="false"` en `values/strings.xml` y **no** se copian a los `values-xx`. La regla
que decide: **no queda ni una palabra dentro**. Un nombre propio, un símbolo del SI, una cifra con
su quilate o una plantilla de formato no se traducen; en cuanto hay una palabra, sí.

| Grupo | Cadenas | Motivo |
|---|---|---|
| Marca | `app_name`, `welcome_title` | Nombre comercial de la app |
| Personas y empresas | `info_perfil_nombre`, `info_perfil_etiquetas`, `info_blanco_joyeros_titulo` | Nombres propios; las etiquetas del perfil ya están en inglés técnico |
| Redes y proveedor | `info_linkedin_titulo`, `info_instagram_titulo`, `precios_fuente_nombre` | Marcas de terceros |
| Jerga de mercado | `precios_detalle_ask`, `precios_detalle_bid` | «Ask» y «Bid» se usan igual en los cinco idiomas |
| Símbolos de unidad | `unidad_gramos`, `unidad_milimetros`, `unidad_cm3`, `unidad_g_cm3`, `unidad_euro_gramo`, `unidad_euro_kilo`, `unidad_euro_onza`, `unidad_euro_libra` | Símbolos, no palabras |
| Cifras y quilates | `oro_ley_18k`, `oro_ley_14k`, `oro_ley_12k`, `oro_ley_9k`, `plata_ley_950`, `plata_ley_900` | Solo la cifra y la K; las dos leyes de plata con «(ley)» **sí** se traducen |
| Plantillas de formato | `precios_mercado_metal`, `chapas_pureza_formato`, `chapas_ley_milesimas`, `chapas_dibujo_medida` | Solo marcadores, paréntesis y símbolos |
| Nombres de idioma | `idioma_es`, `idioma_en`, `idioma_fr`, `idioma_de`, `idioma_it` | Endónimos: el joyero debe reconocer el suyo aunque la app esté en otro idioma |

Total: **33 no traducibles y 184 traducibles** de las 217 cadenas del fichero base (los `values-xx`
tienen 184 cadenas cada uno).

**Dos cadenas se partieron** para poder traducir la palabra sin tocar el nombre propio, que es el
mismo patrón en los dos casos:

```xml
<string name="precios_fuente">Fuente: %1$s</string>
<string name="precios_fuente_nombre" translatable="false">Metal Sentinel</string>

<string name="welcome_developer">Desarrollado por %1$s</string>
<!-- el nombre sale de info_perfil_nombre, que ya era no traducible -->
```

En `PreciosMetalesContent.kt` y en `WelcomeScreen.kt` el nombre entra por parámetro. La segunda se
descubrió en el emulador: con la app en inglés, la portada seguía diciendo «Desarrollado por José
Ramón Blanco», que es exactamente la clase de isla en español que FR-015 prohíbe.

## Cadenas que se traducen aunque parezcan invariables

| Clave | Español | Por qué se traduce |
|---|---|---|
| `nav_home` | Home | Es una etiqueta de navegación, no una marca. Coincidirá en inglés e italiano, y eso es correcto |
| `plata_ley_925`, `plata_ley_800` | 925 (ley), 800 (ley) | «(ley)» es la ley de contraste española y cambia en cada idioma; la cifra no |
| `soldadura_familia_*`, `chapas_familia_*` | ORO, PLATA, CLÁSICA… | Palabras, no símbolos. Se mantienen en mayúsculas |
| `metal_*` | Oro, Plata, Latón… | Nombres de metal, traducibles. El **símbolo químico** (Au, Ag) no está en recursos: vive en el enum |
| `oro_aviso_12k`, `plata_aviso_950`, `plata_aviso_900` | «En España…» | Se traducen **manteniendo la referencia a España** (FR-017): describen la ley española |
| `soldadura_aviso_seguridad` | humos de cadmio y zinc | Texto de seguridad: traducción literal y completa, sin resumir |

## Cadenas nuevas de esta feature

```xml
<!-- Ajustes: idioma -->
<string name="ajustes_seccion_idioma">Idioma de la aplicación</string>
<string name="ajustes_idioma_descripcion">Elige el idioma en el que quieres ver la aplicación.</string>
<string name="ajustes_idioma_automatico">Automático</string>
<string name="ajustes_idioma_automatico_detalle">Sigue al idioma del dispositivo · %1$s</string>
<string name="ajustes_idioma_activo">Idioma seleccionado</string>
<string name="idioma_es" translatable="false">Español</string>
<string name="idioma_en" translatable="false">English</string>
<string name="idioma_fr" translatable="false">Français</string>
<string name="idioma_de" translatable="false">Deutsch</string>
<string name="idioma_it" translatable="false">Italiano</string>
```

`ajustes_idioma_activo` es solo para el lector de pantalla: la marca visual de la fila activa no dice
nada por sí sola a quien no ve.

## Verificación automática

**`TraduccionesTest`** (JVM, `app/src/test/.../recursos/`) parsea los cinco ficheros desde
`src/main/res/` y comprueba las reglas 1–6 con un mensaje de fallo que nombra la clave y el idioma. Es
la red rápida: corre en cada `./gradlew :app:testDebugUnitTest`.

**`./gradlew :app:lint`** es la segunda red, desde fuera del proyecto: `MissingTranslation` cubre la
regla 1, `ExtraTranslation` las reglas 2 y 3. Pasa a ser puerta de calidad de esta feature.

Las dos redes se solapan a propósito: `lint` conoce el sistema de recursos de Android mejor que
cualquier test propio, pero no comprueba los marcadores de formato ni los saltos de línea, y tarda
mucho más.
