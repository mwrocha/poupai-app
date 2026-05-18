package io.poupai.app.core.util

import io.poupai.app.domain.model.Investment
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Status de atualização do valor de um ativo, derivado da data do snapshot mais recente
 * no histórico de profitability (que é populado a cada ATUALIZACAO_VALOR).
 */
enum class StaleStatus {
    /** Atualizado há menos de 15 dias — sem alerta. */
    FRESH,

    /** Atualizado entre 15 e 30 dias — alerta amarelo. */
    OLD,

    /** Atualizado há mais de 30 dias — alerta vermelho. */
    VERY_OLD,

    /** Nunca recebeu uma atualização de valor (history vazio). */
    NO_UPDATES,
}

data class StaleInfo(
    val status: StaleStatus,
    val daysAgo: Int?,
    val lastUpdate: String?,
)

fun Investment.computeStaleInfo(): StaleInfo {
    val lastUpdate = history.maxByOrNull { it.date }?.date
        ?: return StaleInfo(StaleStatus.NO_UPDATES, null, null)
    val days = runCatching {
        ChronoUnit.DAYS.between(LocalDate.parse(lastUpdate), LocalDate.now()).toInt()
    }.getOrNull() ?: return StaleInfo(StaleStatus.FRESH, null, lastUpdate)
    val status = when {
        days < 15 -> StaleStatus.FRESH
        days < 31 -> StaleStatus.OLD
        else -> StaleStatus.VERY_OLD
    }
    return StaleInfo(status, days, lastUpdate)
}

/** True quando o ativo merece atenção (precisa de atualização). */
fun StaleInfo.needsAttention(): Boolean =
    status == StaleStatus.OLD || status == StaleStatus.VERY_OLD
