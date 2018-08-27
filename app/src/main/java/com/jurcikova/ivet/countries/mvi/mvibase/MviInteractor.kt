package com.jurcikova.ivet.countries.mvi.mvibase

import kotlinx.coroutines.experimental.channels.ReceiveChannel

interface MviInteractor<A : MviAction, R : MviResult> {

    suspend fun processAction(action:A): ReceiveChannel<R>
}