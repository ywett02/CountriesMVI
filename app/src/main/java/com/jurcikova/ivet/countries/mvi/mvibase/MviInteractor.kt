package com.jurcikova.ivet.countries.mvi.mvibase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface MviInteractor<A : MviAction, R : MviResult> {

	fun CoroutineScope.processAction(action: A): ReceiveChannel<R>
}