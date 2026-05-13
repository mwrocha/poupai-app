package io.poupai.app.data.remote.dto

data class BrapiQuoteResponse(
    val results: List<BrapiQuoteResult>?,
    val requestedAt: String?,
    val took: String?,
)

data class BrapiQuoteResult(
    val symbol: String?,
    val shortName: String?,
    val regularMarketPrice: Double?,
    val regularMarketChangePercent: Double?,
    val regularMarketChange: Double?,
    val regularMarketPreviousClose: Double?,
    val regularMarketOpen: Double?,
    val regularMarketDayHigh: Double?,
    val regularMarketDayLow: Double?,
    val regularMarketVolume: Long?,
    val currency: String?,
    val marketCap: Long?,
    val logourl: String?,
)