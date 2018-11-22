package com.jurcikova.ivet.countries.mvi.mvibase

import kotlinx.coroutines.channels.Channel

/**
 * Object representing a UI that will
 * a) emit its intents to a view model,
 * b) subscribes to a view model for rendering its UI.
 *
 * @param I Top class of the [MviIntent] that the [MviView] will be emitting.
 * @param S Top class of the [MviViewState] the [MviView] will be subscribing to.
 */
interface MviView<I : MviIntent, in S : MviViewState> {

	val intents: Channel<I>

	/**
	 * Entry point for the [MviView] to render itself based on a [MviViewState].
	 * Reflects [MviViewState] to the screen
	 */
	fun render(state: S)
}
