package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countries.mvi.business.interactor.CountryListInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.InitialIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.SwipeToRefresh
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.LoadCountriesResult
import com.strv.ktools.inject
import com.strv.ktools.logMe
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consume

class CountryListViewModel : BaseViewModel<CountryListIntent, CountryListAction, CountryListResult, CountryListViewState>() {

    private val countryListInteractor by inject<CountryListInteractor>()

    override fun reducer(previousState: CountryListViewState, result: CountryListResult) =
            when (result) {
                is LoadCountriesResult -> when (result) {
                    is LoadCountriesResult.Success -> {
                        previousState.copy(
                                isLoading = false,
                                isRefreshing = false,
                                countries = result.countries

                        )
                    }
                    is LoadCountriesResult.Failure -> previousState.copy(isLoading = false, isRefreshing = false, error = result.error)
                    is LoadCountriesResult.InProgress -> {
                        if (result.isRefreshing) {
                            previousState.copy(isLoading = false, isRefreshing = true)
                        } else previousState.copy(isLoading = true, isRefreshing = false)
                    }
                }
            }

    override val intentProcessor = actor<CountryListIntent> {
        var containsInitialIntent = false

        for (intent in channel) {
            if (intent === InitialIntent && !containsInitialIntent) {
                containsInitialIntent = true
                actions.send(actionFromIntent(intent))
            } else if (intent !== CountryListIntent.InitialIntent) {
                actions.send(actionFromIntent(intent))
            }
        }
    }

    override val actions = actor<CountryListAction> {
        for (action in channel) {
            action.logMe()

            countryListInteractor.processAction(action).consume {
                for (result in this) {
                    result.logMe()
                    state.offer(reducer(state.value, result))
                }
            }
        }
    }

    override val state = ConflatedBroadcastChannel(CountryListViewState.idle())

    override fun actionFromIntent(intent: CountryListIntent): CountryListAction {
        return when (intent) {
            is InitialIntent -> LoadCountriesAction(false)
            is SwipeToRefresh -> LoadCountriesAction(true)
        }
    }
}