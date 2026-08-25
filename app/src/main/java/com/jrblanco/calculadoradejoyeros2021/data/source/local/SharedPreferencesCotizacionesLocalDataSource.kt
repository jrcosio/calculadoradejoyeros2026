package com.jrblanco.calculadoradejoyeros2021.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.domain.model.InstantaneaCotizaciones
import kotlinx.coroutines.withContext

/**
 * La caché de cotizaciones en `SharedPreferences`: **una sola clave** con el JSON completo, de
 * modo que la escritura es atómica y no puede quedar un instante sin sus resultados. Se escribe
 * como mucho una vez por hora y nadie la observa, así que no hace falta DataStore.
 *
 * El fichero `cotizaciones.xml` está excluido de las reglas de backup: es un dato derivado con
 * fecha. Pegamento de veinte líneas sobre el SDK; la lógica está en [CodificadorInstantanea].
 */
class SharedPreferencesCotizacionesLocalDataSource(
    private val context: Context,
    private val dispatchers: DispatcherProvider,
) : CotizacionesLocalDataSource {

    private val codificador = CodificadorInstantanea()

    private val preferencias: SharedPreferences by lazy {
        context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE)
    }

    override suspend fun leer(): InstantaneaCotizaciones? = withContext(dispatchers.io) {
        val texto = preferencias.getString(CLAVE, null) ?: return@withContext null
        val instantanea = codificador.decodificar(texto)
        // Lo que no se entiende se borra: mejor volver a consultar que arrastrar basura.
        if (instantanea == null) preferencias.edit().remove(CLAVE).commit()
        instantanea
    }

    override suspend fun guardar(instantanea: InstantaneaCotizaciones) {
        withContext(dispatchers.io) {
            preferencias.edit().putString(CLAVE, codificador.codificar(instantanea)).commit()
        }
    }

    companion object {
        const val FICHERO = "cotizaciones"
        const val CLAVE = "instantanea_json"
    }
}
