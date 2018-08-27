package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import com.jurcikova.ivet.countries.mvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.countries.mvi.common.consumeEach
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.strv.ktools.inject
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.flatMap
import kotlinx.coroutines.experimental.channels.map

class CountrySearchViewModel : BaseViewModel<CountrySearchIntent, CountrySearchAction, CountrySearchResult, CountrySearchViewState>() {

    private val countrySearchInteractor by inject<CountrySearchInteractor>()

    override val state: ConflatedBroadcastChannel<CountrySearchViewState> = ConflatedBroadcastChannel(CountrySearchViewState.idle())

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
            }

    override suspend fun processIntents(channel: Channel<CountrySearchIntent>) {
        channel
                .map { intent ->
                    logD("intent: $intent")
                    actionFromIntent(intent)
                }
                .flatMap { action ->
                    logD("action: $action")
                    countrySearchInteractor.processAction(action)
                }.consumeEach { result ->
                    logD("result: $result")
                    state.offer(reducer(state.value, result))
                }
    }

    override fun actionFromIntent(intent: CountrySearchIntent): CountrySearchAction =
            when (intent) {
                is CountrySearchIntent.SearchIntent -> CountrySearchAction.LoadCountriesByNameAction(intent.searchQuery)
            }
}


