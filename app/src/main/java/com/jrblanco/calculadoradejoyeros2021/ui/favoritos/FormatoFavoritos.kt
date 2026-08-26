package com.jrblanco.calculadoradejoyeros2021.ui.favoritos

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * El redondeo de vista de la pantalla de Favoritos. Hermano de `FormatoPrecios`: `internal object`,
 * JVM puro y sin Android, así que se prueba sin emulador.
 *
 * **Duplica a propósito** las políticas de las cuatro calculadoras, y no se unifica: `CLAUDE.md` lo
 * prohíbe por escrito porque son documentos técnicos distintos, y en plata el truncado no es una
 * preferencia sino la Ley 17/1985. Un helper parametrizado por política se leería como la
 * unificación que la norma impide.
 *
 * El guardián de esa duplicación es `FavoritosParidadFormatoTest`, que compara cadena a cadena la
 * salida de esta pantalla con la de cada calculadora. Es el mismo patrón que ya vigila que las
 * milésimas de `MaterialChapa` coincidan con las de `LeyOro`/`LeyPlata`.
 */
internal object FormatoFavoritos {

    /** Oro y las dos pantallas de soldaduras: a la media, 3 decimales (§17 de oro, §8.3 de soldaduras). */
    fun gramosMedia(valor: BigDecimal): String = valor.setScale(3, RoundingMode.HALF_UP).aTexto()

    /**
     * Plata: **trunca**. Con `HALF_UP`, 100 g de plata fina hacia 950‰ mostrarían 5,158 g de cobre y
     * la ley real caería a 949,999‰; truncar da 5,157 g y 950,008‰. La norma no admite tolerancia
     * en menos, y la cifra mostrada es la que el joyero pesa.
     */
    fun gramosPlata(valor: BigDecimal): String = valor.setScale(3, RoundingMode.DOWN).aTexto()

    /** Chapas: 2 decimales (§7, §21). Las densidades son orientativas; un tercer decimal fingiría precisión. */
    fun pesoChapa(valor: BigDecimal): String = valor.setScale(2, RoundingMode.HALF_UP).aTexto()

    /** Volumen y metal fino de una chapa: 3 decimales, como en su calculadora. */
    fun tresDecimales(valor: BigDecimal): String = valor.setScale(3, RoundingMode.HALF_UP).aTexto()

    /**
     * La cantidad que el joyero tecleó, sin escala impuesta: `30` → «30», `30.50` → «30,5».
     *
     * No es un resultado, es un eco de lo introducido: mostrar «30,000 gr» donde escribió «30»
     * fingiría una precisión de cálculo que no existe. Es también lo que vuelve al campo al reabrir
     * un favorito.
     */
    fun cantidadEntrada(valor: BigDecimal): String = valor.stripTrailingZeros().toPlainString().aComa()

    private fun BigDecimal.aTexto(): String = toPlainString().aComa()

    /** Coma decimal determinista, como en el resto de la app: el formato no se localiza. */
    private fun String.aComa(): String = replace('.', ',')
}
