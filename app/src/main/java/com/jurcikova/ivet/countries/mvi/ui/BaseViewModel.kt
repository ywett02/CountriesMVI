package com.jurcikova.ivet.countries.mvi.ui

import androidx.lifecycle.ViewModel
import com.jurcikova.ivet.countries.mvi.mvibase.MviAction
import com.jurcikova.ivet.countries.mvi.mvibase.MviIntent
import com.jurcikova.ivet.countries.mvi.mvibase.MviResult
import com.jurcikova.ivet.countries.mvi.mvibase.MviView
import com.jurcikova.ivet.countries.mvi.mvibase.MviViewModel
import com.jurcikova.ivet.countries.mvi.mvibase.MviViewState
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlin.coroutines.experimental.CoroutineContext

abstract class BaseViewModel<I : MviIntent, A : MviAction, R : MviResult, S : MviViewState> : ViewModel(), MviViewModel<I, S>, CoroutineScope {

	val job = Job()

	override val coroutineContext: CoroutineContext
		get() = Dispatchers.IO + job

	override fun onCleared() {
		super.onCleared()
		job.cancel()
	}

	/**
	 * The Reducer is where [MviViewState], that the [MviView] will use to
	 * render itself, are created.
	 * It takes the last cached [MviViewState], the latest [MviResult] and
	 * creates a new [MviViewState] by only updating the related fields.
	 * This is basically like a big switch statement of all possible types for the [MviResult]
	 */
	protected abstract val reduce: (S, R) -> S

	/**
	 * Translate an [MviIntent] to an [MviAction].
	 * Used to decouple the UI and the business logic to allow easy testings and reusability.
	 */
	protected abstract fun actionFromIntent(intent: I): A
}