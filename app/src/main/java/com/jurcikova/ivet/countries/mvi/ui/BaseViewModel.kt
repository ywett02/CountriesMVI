package com.jurcikova.ivet.countries.mvi.ui

import android.arch.lifecycle.ViewModel
import com.jurcikova.ivet.countries.mvi.mvibase.*
import com.jurcikova.ivet.countriesMVI.mvibase.MviIntent
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject


abstract class BaseViewModel<I : MviIntent, A: MviAction, R: MviResult, S : MviViewState> : ViewModel(), MviViewModel<I,S> {

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
    protected val intentsSubject: PublishSubject<I> = PublishSubject.create()

    /**
     * Compose all components to create the stream logic
     */
    protected abstract val statesObservable: Observable<S>

    /**
     * Translate an [MviIntent] to an [MviAction].
     * Used to decouple the UI and the business logic to allow easy testings and reusability.
     */
    protected abstract fun actionFromIntent(intent: I): A
}