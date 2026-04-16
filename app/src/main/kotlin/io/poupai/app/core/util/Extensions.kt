package io.poupai.app.core.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import android.util.Patterns
import java.util.Locale

/**
 * Extensões utilitárias para uso em todo o app.
 */

// ─── Formatação monetária BR ───
fun Double.toBRL(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return format.format(this)
}

fun Double.toPercentage(): String = String.format("%.1f%%", this)

// ─── Formatação de datas ───
fun Date.toDisplayFormat(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))
    return sdf.format(this)
}

fun Date.toShortFormat(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return sdf.format(this)
}

// ─── Validações ───
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.length >= 6
}
