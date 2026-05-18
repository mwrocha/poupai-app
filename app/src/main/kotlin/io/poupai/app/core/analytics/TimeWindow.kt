package io.poupai.app.core.analytics

/**
 * Janelas temporais usadas em análises de performance.
 * Compartilhada entre AllocationScreen e InvestmentDetailScreen.
 */
enum class TimeWindow(val label: String, val months: Int?) {
    ONE_MONTH("1M", 1),
    THREE_MONTHS("3M", 3),
    SIX_MONTHS("6M", 6),
    TWELVE_MONTHS("12M", 12),
    ALL("Total", null),
}
