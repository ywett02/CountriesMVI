package com.jurcikova.ivet.countries.mvi.mvibase

import androidx.lifecycle.LiveData
import io.reactivex.Observable

/**
 * Object that will subscribes to a [MviView]'s [MviIntent]s,
 * process it and emit a [MviViewState] back.
 *
 * @param I Top class of the [MviIntent] that the [MviViewModel] will be subscribing
 * to.
 * @param S Top class of the [MviViewState] the [MviViewModel] will be emitting.
 */
interface MviViewModel<I : MviIntent, S : MviViewState> {

	val statesLiveData: LiveData<S>

	fun processIntents(intents: Observable<I>)
}
