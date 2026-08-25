package com.jrblanco.calculadoradejoyeros2021.ui.idioma

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import java.util.Locale

/**
 * Pinta [content] en el idioma indicado, sea el que sea el del dispositivo.
 *
 * Es la pieza que hace que tocar una bandera repinte la app entera **sin recrear la Activity**:
 * `stringResource` no lee `LocalContext`, lee `LocalResources`, que en Compose UI 1.12 es un
 * `compositionLocalWithComputedDefaultOf` derivado de `LocalConfiguration` y `LocalContext`. Al
 * proveer esos dos, el valor calculado se rehace y se invalidan todas las llamadas del subárbol.
 * Por eso **no** hay que proveer `LocalResources` a mano: se recalcula solo.
 *
 * `LocalContext` es un `staticCompositionLocalOf`, así que proveerlo recompone el subárbol
 * completo, que es justo lo que se busca en un cambio de idioma.
 *
 * De paso, los cinco `Toast` de las calculadoras y las fechas de `DateUtils` de la pantalla de
 * precios también salen traducidos, porque todos parten de `LocalContext`. Los enlaces externos
 * de la pantalla de información no se ven afectados: usan `LocalUriHandler`, que se resuelve por
 * encima de este proveedor y sigue atado a la Activity.
 *
 * Alternativas descartadas y sus motivos, en `specs/008-ajustes-idioma/research.md` (R2).
 */
@Composable
fun ProveedorIdioma(
    idioma: IdiomaApp,
    content: @Composable () -> Unit,
) {
    val base = LocalContext.current
    // La configuración se lee de `LocalConfiguration` y no de `base.resources.configuration`: la
    // segunda no invalida la composición, así que un cambio real del sistema —el tamaño de letra,
    // sobre todo— dejaría el contexto localizado con la configuración vieja. Al leerla de aquí, el
    // `remember` se rehace y el árbol entero se repinta con la configuración nueva y el idioma
    // elegido.
    val configuracionSistema = LocalConfiguration.current
    val contextoLocalizado = remember(base, configuracionSistema, idioma) {
        val configuracion = Configuration(configuracionSistema).apply {
            // setLocales y LocaleList son API 24, igual que el minSdk del proyecto.
            setLocales(LocaleList(Locale.forLanguageTag(idioma.etiquetaBcp47)))
        }
        base.createConfigurationContext(configuracion)
    }

    CompositionLocalProvider(
        LocalContext provides contextoLocalizado,
        LocalConfiguration provides contextoLocalizado.resources.configuration,
        content = content,
    )
}
