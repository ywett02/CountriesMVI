package com.jurcikova.ivet.countries.mvi.mvibase

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel

interface MviInteractor<A : MviAction, R : MviResult> {

    fun CoroutineScope.processAction(action: A): ReceiveChannel<R>
}