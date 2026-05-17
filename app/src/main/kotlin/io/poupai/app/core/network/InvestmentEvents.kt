package io.poupai.app.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object InvestmentEvents {
    private val _entriesChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val entriesChanged = _entriesChanged.asSharedFlow()

    fun notifyEntriesChanged() {
        _entriesChanged.tryEmit(Unit)
    }
}
