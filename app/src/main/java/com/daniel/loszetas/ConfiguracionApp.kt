package com.daniel.loszetas.utils

import android.content.Context
import java.text.NumberFormat
import java.util.*

object ConfiguracionApp {

    private const val PREFS_NAME = "app_preferences"
    private const val KEY_MONEDA = "moneda"
    private const val KEY_IDIOMA = "idioma"

    // Monedas disponibles
    data class Moneda(
        val codigo: String,
        val nombre: String,
        val simbolo: String,
        val locale: Locale
    )

    val MONEDAS_DISPONIBLES = listOf(
        Moneda("CLP", "Peso Chileno", "$", Locale("es", "CL")),
        Moneda("BOB", "Boliviano", "Bs", Locale("es", "BO")),
        Moneda("USD", "Dólar Estadounidense", "$", Locale("en", "US")),
        Moneda("EUR", "Euro", "€", Locale("es", "ES")),
        Moneda("MXN", "Peso Mexicano", "$", Locale("es", "MX")),
        Moneda("ARS", "Peso Argentino", "$", Locale("es", "AR")),
        Moneda("COP", "Peso Colombiano", "$", Locale("es", "CO")),
        Moneda("PEN", "Sol Peruano", "S/", Locale("es", "PE")),
        Moneda("BRL", "Real Brasileño", "R$", Locale("pt", "BR"))
    )

    // Idiomas disponibles
    data class Idioma(
        val codigo: String,
        val nombre: String,
        val locale: Locale
    )

    val IDIOMAS_DISPONIBLES = listOf(
        Idioma("es", "Español", Locale("es", "ES")),
        Idioma("en", "English", Locale("en", "US")),
        Idioma("pt", "Português", Locale("pt", "BR"))
    )

    // Guardar moneda
    fun guardarMoneda(context: Context, codigoMoneda: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MONEDA, codigoMoneda).apply()
    }

    // Obtener moneda actual
    fun obtenerMoneda(context: Context): Moneda {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val codigo = prefs.getString(KEY_MONEDA, "CLP") ?: "CLP"
        return MONEDAS_DISPONIBLES.find { it.codigo == codigo } ?: MONEDAS_DISPONIBLES[0]
    }

    // Guardar idioma
    fun guardarIdioma(context: Context, codigoIdioma: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_IDIOMA, codigoIdioma).apply()
    }

    // Obtener idioma actual
    fun obtenerIdioma(context: Context): Idioma {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val codigo = prefs.getString(KEY_IDIOMA, "es") ?: "es"
        return IDIOMAS_DISPONIBLES.find { it.codigo == codigo } ?: IDIOMAS_DISPONIBLES[0]
    }

    // Formatear moneda según configuración
    fun formatearMoneda(context: Context, monto: Double): String {
        val moneda = obtenerMoneda(context)
        val formatter = NumberFormat.getCurrencyInstance(moneda.locale)
        return formatter.format(monto)
    }

    // Obtener símbolo de moneda
    fun obtenerSimboloMoneda(context: Context): String {
        return obtenerMoneda(context).simbolo
    }
}