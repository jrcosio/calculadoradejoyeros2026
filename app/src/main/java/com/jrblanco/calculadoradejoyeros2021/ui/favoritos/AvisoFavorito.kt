package com.jrblanco.calculadoradejoyeros2021.ui.favoritos

/**
 * Lo que hay que decirle al joyero después de pulsar «Guardar en favoritos».
 *
 * Viaja en el `UiState` de las cinco calculadoras como un campo nulable de un solo uso: el guardado
 * es `suspend`, así que la vista no puede saber el resultado en el momento del clic y hace falta que
 * el estado se lo cuente. La vista lo consume con `onAvisoFavoritoMostrado()`; sin eso, guardar dos
 * veces seguidas no volvería a lanzar el `Toast`.
 *
 * [SIN_DATOS] existe porque el botón está siempre activo: pulsarlo con el campo vacío no puede ser
 * silencio, que se lee como una app rota.
 */
enum class AvisoFavorito {
    GUARDADO,
    REPETIDO,
    SIN_DATOS,
}
