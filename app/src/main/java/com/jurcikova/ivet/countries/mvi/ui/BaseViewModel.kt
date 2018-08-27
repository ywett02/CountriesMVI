package com.jurcikova.ivet.countries.mvi.ui

import android.arch.lifecycle.ViewModel
import com.jurcikova.ivet.countries.mvi.mvibase.*

abstract class BaseViewModel<I : MviIntent, A : MviAction, R : MviResult, S : MviViewState> : ViewModel(), MviViewModel<I, S> {

    /**
     * The Reducer is where [MviViewState], that the [MviView] will use to
     * render itself, are created.
     * It takes the last cached [MviViewState], the latest [MviResult] and
     * creates a new [MviViewState] by only updating the related fields.
     * This is basically like a big switch statement of all possible types for the [MviResult]
     */
    protected abstract fun reducer(previousState: S, result: R): S

    /**
     * Translate an [MviIntent] to an [MviAction].
     * Used to decouple the UI and the business logic to allow easy testings and reusability.
     */
    protected abstract fun actionFromIntent(intent: I): A
}