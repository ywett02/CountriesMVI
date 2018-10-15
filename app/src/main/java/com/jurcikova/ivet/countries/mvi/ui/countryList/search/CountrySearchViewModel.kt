package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.channels.*

class CountrySearchViewModel(val countrySearchInteractor: CountrySearchInteractor) : BaseViewModel<CountrySearchIntent, CountrySearchAction, CountrySearchResult, CountrySearchViewState>() {

    override val state: ConflatedBroadcastChannel<CountrySearchViewState> = ConflatedBroadcastChannel(CountrySearchViewState.idle())

    override suspend fun processIntents() = actor<CountrySearchIntent> {
        map { intent ->
            logD("intent: $intent")
            actionFromIntent(intent)
        }.flatMap { action ->
            logD("action: $action")
            countrySearchInteractor.run {
                processAction(action)
            }
        }.consumeEach { result ->
            logD("result: $result")
            state.send(reducer(state.value, result))
        }
    }

    override fun reducer(previousState: CountrySearchViewState, result: CountrySearchResult) =
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
                            isLoading = false, error = null, message = MessageType.AddToFavorite)
                    is CountrySearchResult.AddToFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is CountrySearchResult.AddToFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                    is CountrySearchResult.AddToFavoriteResult.Reset -> previousState.copy(message = null)
                }
                is CountrySearchResult.RemoveFromFavoriteResult -> when (result) {
                    is CountrySearchResult.RemoveFromFavoriteResult.Success -> previousState.copy(
                            isLoading = false, error = null, message = MessageType.RemoveFromFavorite)
                    is CountrySearchResult.RemoveFromFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is CountrySearchResult.RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                    is CountrySearchResult.RemoveFromFavoriteResult.Reset -> previousState.copy(message = null)
                }
            }

    override fun actionFromIntent(intent: CountrySearchIntent): CountrySearchAction =
            when (intent) {
                is CountrySearchIntent.SearchIntent -> CountrySearchAction.LoadCountriesByNameAction(intent.searchQuery)
                is CountrySearchIntent.AddToFavoriteIntent -> CountrySearchAction.AddToFavoriteAction(intent.countryName)
                is CountrySearchIntent.RemoveFromFavoriteIntent -> CountrySearchAction.RemoveFromFavoriteAction(intent.countryName)
            }
}


