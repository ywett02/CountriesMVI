package com.jurcikova.ivet.countries.mvi.mvibase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * Object that will subscribes to a [MviView]'s [MviIntent]s,
 * process it and emit a [MviViewState] back.
 *
 * @param I Top class of the [MviIntent] that the [MviViewModel] will be subscribing
 * to.
 * @param S Top class of the [MviViewState] the [MviViewModel] will be emitting.
 */
interface MviViewModel<I : MviIntent, S : MviViewState> {

	@ExperimentalCoroutinesApi
	val state: ConflatedBroadcastChannel<S>

	suspend fun CoroutineScope.processIntents(channel: Channel<I>)
}
