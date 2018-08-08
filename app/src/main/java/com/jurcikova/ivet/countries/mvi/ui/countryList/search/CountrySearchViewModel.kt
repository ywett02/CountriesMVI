package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import com.jurcikova.ivet.countries.mvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.countries.mvi.mvibase.MviViewModel
import com.strv.ktools.inject
import com.strv.ktools.logD
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
            is CountrySearchResult.LoadCountriesByNameResult.Failure -> previousState.copy(isLoading = false, searchNotStartedYet = false, error = result.error)
            is CountrySearchResult.LoadCountriesByNameResult.InProgress -> previousState.copy(isLoading = true, searchNotStartedYet = false, searchQuery = result.searchQuery, error = null)
            is CountrySearchResult.LoadCountriesByNameResult.EmptyResult -> previousState.copy(isLoading = false, searchNotStartedYet = false, error = null, countries = emptyList())
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
            .doOnNext { action ->
                logD("action: $action")
            }
            //gate to the business logic
            .compose(countrySearchInteractor.actionProcessor)
            .scan(CountrySearchViewState.idle(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect()

    override fun states(): LiveData<CountrySearchViewState> =
            LiveDataReactiveStreams.fromPublisher(statesObservable.toFlowable(BackpressureStrategy.BUFFER))

    override fun processIntents(intents: Observable<CountrySearchIntent>) {
        intents
                .doOnNext { intent ->
                    logD("intent: $intent")
                }
                .subscribe(intentsSubject)
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


