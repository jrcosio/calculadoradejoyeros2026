package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Los cinco idiomas de interfaz de la app, en el orden en que se pintan en Ajustes: primero el
 * español, que es el idioma de `values/` y el de partida.
 *
 * [etiquetaBcp47] hace tres papeles a la vez a propósito: nombra la carpeta de recursos
 * (`values-de` ↔ `de`), es el valor que se persiste y es el identificador de telemetría. Una sola
 * cadena y ninguna tabla de conversión que pueda desincronizarse.
 *
 * **No existe un valor «automático»**: seguir al idioma del dispositivo es la ausencia de
 * elección, y eso se representa con `null` (ver [SeleccionIdioma]). Un sexto valor del enum que
 * nunca puede llegar a `Locale.forLanguageTag` acabaría colándose en una conversión.
 */
enum class IdiomaApp(val etiquetaBcp47: String) {
    ESPANOL("es"),
    INGLES("en"),
    FRANCES("fr"),
    ALEMAN("de"),
    ITALIANO("it"),
    ;

    /** Identificador estable para telemetría: no se traduce y no cambia con el idioma. */
    val analyticsId: String get() = etiquetaBcp47

    companion object {
        /**
         * El idioma de `values/`: lo que ve quien tiene el dispositivo en un idioma que la app no
         * habla. Es español porque es el idioma en el que están escritos los textos originales, y
         * también lo que resolvería Android por su cuenta.
         */
        val PREDETERMINADO = ESPANOL

        /**
         * Traduce una etiqueta de idioma al valor del enum, o `null` si no está soportada.
         *
         * La región se ignora: `es`, `es-ES`, `es_MX`, `ES` y `es-419` son todos [ESPANOL]. La app
         * solo tiene recursos por idioma, así que distinguir variantes no cambiaría nada de lo que
         * el joyero ve.
         *
         * Devuelve `null` —y no [PREDETERMINADO]— para que decida quien llama: el idioma del
         * sistema cae al predeterminado, pero una preferencia guardada que no se entiende debe
         * comportarse como si no hubiera preferencia.
         */
        fun desdeEtiqueta(etiqueta: String?): IdiomaApp? {
            val principal = etiqueta?.trim()?.substringBefore('-')?.substringBefore('_')
                ?.lowercase()
                ?.takeIf { it.isNotEmpty() }
                ?: return null
            return entries.firstOrNull { it.etiquetaBcp47 == principal }
        }
    }
}
