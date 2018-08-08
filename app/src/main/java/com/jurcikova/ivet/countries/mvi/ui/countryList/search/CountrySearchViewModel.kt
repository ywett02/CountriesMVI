package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import com.jurcikova.ivet.countries.mvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.strv.ktools.inject
import com.strv.ktools.logD
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class CountrySearchViewModel : BaseViewModel<CountrySearchIntent, CountrySearchAction, CountrySearchResult, CountrySearchViewState>() {
    private val countrySearchInteractor by inject<CountrySearchInteractor>()

    override val reducer = BiFunction { previousState: CountrySearchViewState, result: CountrySearchResult ->
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
        }
    }

    override val statesObservable: Observable<CountrySearchViewState> = intentsSubject
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

    override fun actionFromIntent(intent: CountrySearchIntent): CountrySearchAction {
        return when (intent) {
            is CountrySearchIntent.SearchIntent -> CountrySearchAction.LoadCountriesByNameAction(intent.searchQuery)
        }
    }
}


