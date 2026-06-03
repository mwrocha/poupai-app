package io.poupai.app.core.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

// ─── Valores numéricos no padrão pt-BR (vírgula como separador decimal) ───
object NumberMasks {

    /**
     * Normaliza um input numérico para o padrão pt-BR enquanto o usuário digita:
     * converte ponto em vírgula, mantém só dígitos e no máximo uma vírgula.
     *
     *  "100.75" → "100,75"   (ponto vira vírgula)
     *  "10,5"   → "10,5"
     *  "1,2,3"  → "1,23"     (mantém apenas a primeira vírgula)
     */
    fun decimal(input: String): String {
        var hasComma = false
        val sb = StringBuilder()
        for (c in input.replace('.', ',')) {
            when {
                c.isDigit() -> sb.append(c)
                c == ',' && !hasComma -> {
                    sb.append(c)
                    hasComma = true
                }
            }
        }
        return sb.toString()
    }

    /** Double → string de input pt-BR (vírgula, sem ".0" supérfluo). 0.0 → "". */
    fun fromDouble(value: Double): String {
        if (value == 0.0) return ""
        return java.math.BigDecimal.valueOf(value)
            .stripTrailingZeros()
            .toPlainString()
            .replace('.', ',')
    }

    /** String pt-BR (com vírgula) → Double. Null se inválido. */
    fun parse(input: String): Double? = input.trim().replace(",", ".").toDoubleOrNull()
}

// ─── CPF: 000.000.000-00 ───
class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(11)
        val masked = buildString {
            digits.forEachIndexed { index, char ->
                if (index == 3 || index == 6) append('.')
                if (index == 9) append('-')
                append(char)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                return when {
                    o <= 3 -> o
                    o <= 6 -> o + 1
                    o <= 9 -> o + 2
                    else -> o + 3
                }.coerceIn(0, masked.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val o = offset.coerceIn(0, masked.length)
                return when {
                    o <= 3 -> o
                    o <= 7 -> o - 1
                    o <= 11 -> o - 2
                    else -> o - 3
                }.coerceIn(0, digits.length)
            }
        }

        return TransformedText(AnnotatedString(masked), offsetMapping)
    }
}

// ─── Telefone: (00) 00000-0000 ───
// O usuário digita apenas os números, sem o +55
class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(11)
        val masked = buildString {
            digits.forEachIndexed { index, char ->
                when (index) {
                    0 -> append("(")
                    2 -> append(") ")
                    7 -> append("-")
                }
                append(char)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                return when {
                    o == 0 -> 0
                    o <= 2 -> o + 1   // após "("
                    o <= 7 -> o + 3   // após "(XX) "
                    o <= 11 -> o + 4  // após "(XX) XXXXX-"
                    else -> masked.length
                }.coerceIn(0, masked.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val o = offset.coerceIn(0, masked.length)
                return when {
                    o <= 1 -> 0
                    o <= 3 -> o - 1
                    o <= 9 -> o - 3
                    o <= 15 -> o - 4
                    else -> digits.length
                }.coerceIn(0, digits.length)
            }
        }

        return TransformedText(AnnotatedString(masked), offsetMapping)
    }
}