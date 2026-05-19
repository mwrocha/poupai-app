package io.poupai.app.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Helpers para o formato de data brasileiro (dd-MM-yyyy) no app.
 *
 * Convenção:
 *  - Forms exibem e armazenam no state em `dd-MM-yyyy`
 *  - Repositories recebem `dd-MM-yyyy` mas convertem para ISO `yyyy-MM-dd`
 *    antes de mandar para o backend (contrato padrão de API).
 *  - Displays usam `isoToDisplay()` quando a data vier do backend.
 */
object DateFormatter {

    private val DISPLAY = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** ISO `yyyy-MM-dd` → display `dd-MM-yyyy`. Fallback ao original em erro. */
    fun isoToDisplay(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val parsed = runCatching { LocalDate.parse(iso, ISO) }.getOrNull() ?: return iso
        return parsed.format(DISPLAY)
    }

    /** Display `dd-MM-yyyy` → ISO `yyyy-MM-dd`. Null se inválido. */
    fun displayToIso(display: String): String? {
        val trimmed = display.trim()
        if (trimmed.isBlank()) return null
        return runCatching { LocalDate.parse(trimmed, DISPLAY).format(ISO) }.getOrNull()
    }

    /** Data de hoje no formato display. */
    fun todayDisplay(): String = LocalDate.now().format(DISPLAY)

    /** Data de hoje no formato ISO (uso interno/API). */
    fun todayIso(): String = LocalDate.now().format(ISO)

    /**
     * Aplica máscara `dd-MM-yyyy` automaticamente conforme o usuário digita.
     * Aceita só dígitos, insere os hífens nas posições corretas.
     *
     *  "1" → "1"
     *  "12" → "12"
     *  "120" → "12-0"
     *  "1205" → "12-05"
     *  "12052026" → "12-05-2026"
     */
    fun applyMask(input: String): String {
        val digits = input.filter { it.isDigit() }.take(8)
        return when {
            digits.length <= 2 -> digits
            digits.length <= 4 -> "${digits.substring(0, 2)}-${digits.substring(2)}"
            else -> "${digits.substring(0, 2)}-${digits.substring(2, 4)}-${digits.substring(4)}"
        }
    }

    /** True se o texto é uma data válida em formato display. */
    fun isValidDisplay(display: String): Boolean = displayToIso(display) != null
}
