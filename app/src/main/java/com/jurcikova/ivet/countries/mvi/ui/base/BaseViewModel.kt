package com.jurcikova.ivet.countries.mvi.ui.base

import androidx.lifecycle.ViewModel
import com.jurcikova.ivet.countries.mvi.mvibase.MviAction
import com.jurcikova.ivet.countries.mvi.mvibase.MviIntent
import com.jurcikova.ivet.countries.mvi.mvibase.MviResult
import com.jurcikova.ivet.countries.mvi.mvibase.MviViewModel
import com.jurcikova.ivet.countries.mvi.mvibase.MviViewState
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.PublishSubject.create

abstract class BaseViewModel<I : MviIntent, A : MviAction, R : MviResult, S : MviViewState> : ViewModel(), MviViewModel<I, S> {

	/**
	 * The Reducer is where [MviViewState], that the [MviView] will use to
	 * render itself, are created.
	 * It takes the last cached [MviViewState], the latest [MviResult] and
	 * creates a new [MviViewState] by only updating the related fields.
	 * This is basically like a big switch statement of all possible types for the [MviResult]
	 */
	protected abstract val reducer: BiFunction<S, R, S>

	/**
	 * Proxy subject used to keep the stream alive even after the UI gets recycled.
	 * This is basically used to keep ongoing events and the last cached State alive
	 * while the UI disconnects and reconnects on config changes.
	 */
	protected val intentsSubject: PublishSubject<I> = create()

	/**
	 * Translate an [MviIntent] to an [MviAction].
	 * Used to decouple the UI and the business logic to allow easy testings and reusability.
	 */
	protected abstract fun actionFromIntent(intent: I): A
}