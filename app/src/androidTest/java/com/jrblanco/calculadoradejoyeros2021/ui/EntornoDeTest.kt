package com.jrblanco.calculadoradejoyeros2021.ui

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.test.platform.app.InstrumentationRegistry
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import com.jrblanco.calculadoradejoyeros2021.ui.idioma.ProveedorIdioma
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import java.util.Locale

// --- El entorno común de los tests instrumentados de pantalla. ---
//
// Existe porque la suite dependía del idioma del emulador: los textos esperados salían del
// `targetContext` y el árbol se pintaba con la configuración del dispositivo, así que la misma
// suite pasaba en un móvil en español y fallaba en uno en inglés. Aquí se fijan **las dos puntas**
// al mismo idioma, y hay que moverlas juntas: anclar solo el árbol dejaría las expectativas en el
// idioma del dispositivo, que es peor que no anclar nada.

/**
 * El idioma en el que se prueban las pantallas: el de `values/`, que es la fuente de verdad de las
 * traducciones. Así un literal con tildes en un test es legítimo y determinista.
 *
 * Que la app se vea bien en los otros cuatro es otra cosa y se comprueba a mano (los desbordes del
 * alemán no los caza un test de nodos) y con `TraduccionesTest` más `lint`.
 */
internal val IDIOMA_DE_TEST = IdiomaApp.ESPANOL

/**
 * Contexto en [IDIOMA_DE_TEST], para resolver los textos **esperados** de un test.
 *
 * Misma receta que `ui/idioma/ProveedorIdioma`: `createConfigurationContext` con `setLocales`, las
 * dos API 24 como el `minSdk`. Sustituye al `targetContext` pelado, que devolvía el idioma del
 * dispositivo.
 */
internal fun contextoDeTest(): Context {
    val base = InstrumentationRegistry.getInstrumentation().targetContext
    val configuracion = Configuration(base.resources.configuration).apply {
        setLocales(LocaleList(Locale.forLanguageTag(IDIOMA_DE_TEST.etiquetaBcp47)))
    }
    return base.createConfigurationContext(configuracion)
}

/**
 * Envuelve el contenido de un test como lo envuelve la app: tema, proveedor de idioma y dentro la
 * pantalla — el mismo orden que `MainActivity`. De aquí sale el texto **pintado**.
 *
 * Sustituye al `Calculadoradejoyeros2021Theme { … }` que los tests usaban directamente, que se
 * saltaba el proveedor y dejaba el árbol a merced del idioma del emulador.
 */
@Composable
internal fun EnIdiomaDeTest(content: @Composable () -> Unit) {
    Calculadoradejoyeros2021Theme {
        ProveedorIdioma(IDIOMA_DE_TEST) {
            content()
        }
    }
}
