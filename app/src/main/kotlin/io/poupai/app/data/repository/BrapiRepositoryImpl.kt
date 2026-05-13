package io.poupai.app.data.repository

import io.poupai.app.BuildConfig
import io.poupai.app.data.remote.api.BrapiApi
import io.poupai.app.domain.repository.BrapiRepository
import javax.inject.Inject

class BrapiRepositoryImpl @Inject constructor(
    private val brapiApi: BrapiApi,
) : BrapiRepository {

    override suspend fun getQuotes(tickers: List<String>): Map<String, Double> {
        if (tickers.isEmpty()) return emptyMap()
        return try {
            val joined = tickers.joinToString(",")
            val response = brapiApi.getQuote(
                tickers = joined,
                token = BuildConfig.BRAPI_TOKEN,
            )
            response.results
                ?.filter { it.symbol != null && it.regularMarketPrice != null }
                ?.associate { it.symbol!! to it.regularMarketPrice!! }
                ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}