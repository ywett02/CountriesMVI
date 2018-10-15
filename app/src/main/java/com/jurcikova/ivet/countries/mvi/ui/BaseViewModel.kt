package com.jurcikova.ivet.countries.mvi.ui

import androidx.lifecycle.ViewModel
import com.jurcikova.ivet.countries.mvi.mvibase.*
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlin.coroutines.experimental.CoroutineContext

abstract class BaseViewModel<I : MviIntent, A : MviAction, R : MviResult, S : MviViewState> : ViewModel(), MviViewModel<I, A, S>, CoroutineScope {

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
    protected abstract fun reducer(previousState: S, result: R): S

    /**
     * Translate an [MviIntent] to an [MviAction].
     * Used to decouple the UI and the business logic to allow easy testings and reusability.
     */
    protected abstract fun actionFromIntent(intent: I): A
}