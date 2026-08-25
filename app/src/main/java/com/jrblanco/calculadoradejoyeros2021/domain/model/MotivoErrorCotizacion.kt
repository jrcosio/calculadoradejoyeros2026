package com.jrblanco.calculadoradejoyeros2021.domain.model

/**
 * Por qué no se pudo obtener la cotización de un metal. La pantalla lo traduce a un mensaje
 * comprensible; ninguno revela la credencial ni detalles técnicos del proveedor.
 */
enum class MotivoErrorCotizacion {
    /** La build no lleva credencial: no se llama a la red. */
    SIN_CREDENCIAL,

    /** HTTP 401 o 403: credencial rechazada o suscripción insuficiente. */
    CREDENCIAL_RECHAZADA,

    /** HTTP 404: ruta o parámetro que el proveedor no reconoce. */
    NO_ENCONTRADO,

    /** HTTP 429: cuota de consultas agotada; la política de caché espera cinco minutos. */
    LIMITE_ALCANZADO,

    /** HTTP 5xx: el proveedor no está disponible. */
    SERVIDOR,

    /** Sin red o tiempo de espera agotado. */
    SIN_CONEXION,

    /** JSON ilegible, símbolo ausente en la respuesta o moneda distinta de la pedida. */
    RESPUESTA_INVALIDA,

    /** Cualquier otra causa. */
    DESCONOCIDO,
    ;

    /** Identificador estable para telemetría: el nombre en minúsculas. */
    val analyticsId: String get() = name.lowercase()
}
