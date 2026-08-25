package com.jrblanco.calculadoradejoyeros2021.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.domain.model.IdiomaApp
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Los ajustes en Preferences DataStore: una sola clave con la etiqueta del idioma elegido.
 *
 * DataStore y no `SharedPreferences` —al contrario que la caché de cotizaciones de la 007— porque
 * esto **se observa**: de que el cambio llegue como emisión depende que la app se repinte al
 * instante, y la lectura del arranque no puede bloquear el hilo principal.
 *
 * El almacén se crea aquí dentro y no se inyecta, igual que
 * [SharedPreferencesCotizacionesLocalDataSource] se guarda sus `SharedPreferences`: `verify()` de
 * Koin solo inspecciona constructores del tipo primario, y un `DataStore` nacido de una fábrica
 * habría que meterlo en `extraTypes`, debilitando el test para todo el proyecto. El `single` de
 * Koin garantiza la instancia única por fichero que DataStore exige.
 *
 * Contrato completo en `specs/008-ajustes-idioma/contracts/preferencia-idioma.md`.
 */
class DataStoreAjustesLocalDataSource(
    private val context: Context,
    private val dispatchers: DispatcherProvider,
) : AjustesLocalDataSource {

    private val almacen: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(dispatchers.io + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile(FICHERO) },
        )
    }

    override val idioma: Flow<IdiomaApp?> = almacen.data
        // Un fichero ilegible no puede impedir arrancar: se sigue al dispositivo y ya está.
        .catch { causa -> if (causa is IOException) emit(emptyPreferences()) else throw causa }
        // Una etiqueta que esta versión no conoce se ignora, pero **no se borra**: si viene de una
        // versión con un sexto idioma y el joyero vuelve a ella, su elección le espera intacta.
        .map { preferencias -> IdiomaApp.desdeEtiqueta(preferencias[CLAVE_IDIOMA]) }

    override suspend fun guardarIdioma(idioma: IdiomaApp?) {
        almacen.edit { preferencias ->
            // «Automático» es la ausencia de clave, no un valor centinela: así el estado inicial y
            // el elegido a mano son el mismo, y no hay nada que migrar.
            if (idioma == null) {
                preferencias.remove(CLAVE_IDIOMA)
            } else {
                preferencias[CLAVE_IDIOMA] = idioma.etiquetaBcp47
            }
        }
    }

    companion object {
        const val FICHERO = "ajustes"
        val CLAVE_IDIOMA = stringPreferencesKey("idioma")
    }
}
