package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import com.jurcikova.ivet.countries.mvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.strv.ktools.inject
import com.strv.ktools.logMe
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consume

class CountrySearchViewModel : BaseViewModel<CountrySearchIntent, CountrySearchAction, CountrySearchResult, CountrySearchViewState>() {
    private val countrySearchInteractor by inject<CountrySearchInteractor>()

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

    override val intentProcessor = actor<CountrySearchIntent> {
        for (intent in channel) {
            actions.send(actionFromIntent(intent))
        }
    }

    override val actions = actor<CountrySearchAction> {
        for (action in channel) {
            action.logMe()

            countrySearchInteractor.processAction(action).consume {
                for (result in this) {
                    result.logMe()
                    state.offer(reducer(state.value, result))
                }
            }
        }
    }

    override val state: ConflatedBroadcastChannel<CountrySearchViewState> = ConflatedBroadcastChannel(CountrySearchViewState.idle())

    override fun actionFromIntent(intent: CountrySearchIntent): CountrySearchAction {
        return when (intent) {
            is CountrySearchIntent.SearchIntent -> CountrySearchAction.LoadCountriesByNameAction(intent.searchQuery)
        }
    }
}


