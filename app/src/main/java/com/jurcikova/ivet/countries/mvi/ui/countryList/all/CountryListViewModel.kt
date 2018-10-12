package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countries.mvi.business.interactor.CountryListInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.InitialIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.SwipeToRefresh
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.LoadCountriesResult
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.*

class CountryListViewModel(val countryListInteractor: CountryListInteractor) : BaseViewModel<CountryListIntent, CountryListAction, CountryListResult, CountryListViewState>(), CoroutineScope {

    override val state = ConflatedBroadcastChannel(CountryListViewState.idle())

    override fun reducer(previousState: CountryListViewState, result: CountryListResult) =
            when (result) {
                is LoadCountriesResult -> when (result) {
                    is LoadCountriesResult.Success -> {
                        previousState.copy(
                                isLoading = false,
                                isRefreshing = false,
                                countries = result.countries,
                                initial = false
                        )
                    }
                    is LoadCountriesResult.Failure -> previousState.copy(isLoading = false, isRefreshing = false, error = result.error, initial = false)
                    is LoadCountriesResult.InProgress -> {
                        if (result.isRefreshing) {
                            previousState.copy(isLoading = false, isRefreshing = true)
                        } else previousState.copy(isLoading = true, isRefreshing = false)
                    }
                }
            }

    override suspend fun processIntents(channel: Channel<CountryListIntent>) {
        channel
                .map { intent ->
                    logD("intent: $intent")
                    actionFromIntent(intent)
                }
                .flatMap { action ->
                    logD("action: $action")
                    countryListInteractor.run {
                        processAction(action)
                    }
                }.consumeEach { result ->
                    logD("result: $result")
                    state.offer(reducer(state.value, result))
                }
    }

    override fun actionFromIntent(intent: CountryListIntent) =
            when (intent) {
                is InitialIntent -> LoadCountriesAction(false)
                is SwipeToRefresh -> LoadCountriesAction(true)
            }
}