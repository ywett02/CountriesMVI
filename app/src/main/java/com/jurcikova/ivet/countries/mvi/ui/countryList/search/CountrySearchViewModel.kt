package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import com.jurcikova.ivet.countries.mvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListViewState
import com.strv.ktools.logD
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class CountrySearchViewModel(countrySearchInteractor: CountrySearchInteractor) : BaseViewModel<CountrySearchIntent, CountrySearchAction, CountrySearchResult, CountrySearchViewState>() {

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
            is CountrySearchResult.AddToFavoriteResult -> when (result) {
                is CountrySearchResult.AddToFavoriteResult.Success -> previousState.copy(
                        isLoading = false, error = null, message = CountryListViewState.MessageType.AddToFavorite)
                is CountrySearchResult.AddToFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                is CountrySearchResult.AddToFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                is CountrySearchResult.AddToFavoriteResult.Reset -> previousState.copy(message = null)
            }
            is CountrySearchResult.RemoveFromFavoriteResult -> when (result) {
                is CountrySearchResult.RemoveFromFavoriteResult.Success -> previousState.copy(
                        isLoading = false, error = null, message = CountryListViewState.MessageType.RemoveFromFavorite)
                is CountrySearchResult.RemoveFromFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                is CountrySearchResult.RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                is CountrySearchResult.RemoveFromFavoriteResult.Reset -> previousState.copy(message = null)
            }
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
            is CountrySearchIntent.AddToFavoriteIntent -> CountrySearchAction.AddToFavoriteAction(intent.countryName)
            is CountrySearchIntent.RemoveFromFavoriteIntent -> CountrySearchAction.RemoveFromFavoriteAction(intent.countryName)
        }
    }
}


