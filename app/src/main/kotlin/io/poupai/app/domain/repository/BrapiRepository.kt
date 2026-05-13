package io.poupai.app.domain.repository

// Mapa de ticker → preço atual
// Ex: { "BBDC3" -> 15.40, "PETR4" -> 38.20 }
interface BrapiRepository {
    suspend fun getQuotes(tickers: List<String>): Map<String, Double>
}