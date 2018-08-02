package com.jurcikova.ivet.triptodomvi.ui.countryList.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import com.jurcikova.ivet.triptodomvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.triptodomvi.mvibase.MviViewModel
import com.strv.ktools.inject
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class CountrySearchViewModel : ViewModel(), MviViewModel<CountrySearchIntent, CountrySearchViewState> {
    private val countrySearchInteractor by inject<CountrySearchInteractor>()

    /**
     * The Reducer is where [MviViewState], that the [MviView] will use to
     * render itself, are created.
     * It takes the last cached [MviViewState], the latest [MviResult] and
     * creates a new [MviViewState] by only updating the related fields.
     * This is basically like a big switch statement of all possible types for the [MviResult]
     */
    private val reducer = BiFunction { previousState: CountrySearchViewState, result: CountrySearchResult ->
        when (result) {
            is CountrySearchResult.LoadCountriesByNameResult.NotStarted -> {
                previousState.copy(
                        isLoading = false,
                        error = null,
                        searchNotStartedYet = true,
                        countries = emptyList()
                )
            }
            is CountrySearchResult.LoadCountriesByNameResult.Success -> {
                previousState.copy(
                        isLoading = false,
                        searchNotStartedYet = false,
                        error = null,
                        countries = result.countries
                )
            }
            is CountrySearchResult.LoadCountriesByNameResult.Failure -> previousState.copy(isLoading = false, error = result.error, countries = emptyList())
            is CountrySearchResult.LoadCountriesByNameResult.InProgress -> previousState.copy(isLoading = true, searchQuery = result.searchQuery, error = null)
        }
    }

    /**
     * Proxy subject used to keep the stream alive even after the UI gets recycled.
     * This is basically used to keep ongoing events and the last cached State alive
     * while the UI disconnects and reconnects on config changes.
     */
    private val intentsSubject: PublishSubject<CountrySearchIntent> = PublishSubject.create()
    /**
     * Compose all components to create the stream logic
     */
    private val statesObservable: Observable<CountrySearchViewState> = intentsSubject
            .map(this::actionFromIntent)
            .compose(countrySearchInteractor.actionProcessor)
            // Cache each state and pass it to the reducer to create a new state from
            // the previous cached one and the latest Result emitted from the action processor.
            // The Scan operator is used here for the caching.
            .scan(CountrySearchViewState.idle(), reducer)
            // When a reducer just emits previousState, there's no reason to call render. In fact,
            // redrawing the UI in cases like this can cause jank (e.g. messing up snackbar animations
            // by showing the same snackbar twice in rapid succession).
            .distinctUntilChanged()
            // Emit the last one event of the stream on subscription
            // Useful when a View rebinds to the ViewModel after rotation.
            .replay(1)
            // Create the stream on creation without waiting for anyone to subscribe
            // This allows the stream to stay alive even when the UI disconnects and
            // match the stream's lifecycle to the ViewModel's one.
            .autoConnect(0)

    override fun states(): LiveData<CountrySearchViewState> =
            LiveDataReactiveStreams.fromPublisher(statesObservable.toFlowable(BackpressureStrategy.BUFFER))

    override fun processIntents(intents: Observable<CountrySearchIntent>) {
        intents.subscribe(intentsSubject)
    }

    /**
     * Translate an [MviIntent] to an [MviAction].
     * Used to decouple the UI and the business logic to allow easy testings and reusability.
     */
    private fun actionFromIntent(intent: CountrySearchIntent): CountrySearchAction {
        return when (intent) {
            is CountrySearchIntent.SearchIntent -> CountrySearchAction.LoadCountriesByNameAction(intent.searchQuery)
        }
    }
}


