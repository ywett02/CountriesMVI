package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countries.mvi.business.interactor.CountryListInteractor
import com.jurcikova.ivet.countries.mvi.common.consumeEach
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.InitialIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.SwipeToRefresh
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.LoadCountriesResult
import com.strv.ktools.inject
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.channels.*

class CountryListViewModel : BaseViewModel<CountryListIntent, CountryListAction, CountryListResult, CountryListViewState>() {

    private val countryListInteractor by inject<CountryListInteractor>()
    private var processedInitialIntent = false

    override val state = ConflatedBroadcastChannel(CountryListViewState.idle())

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

    override suspend fun processIntents(channel: Channel<CountryListIntent>) {
        channel
                .filter { intent ->
                    intentFilter(intent)
                }
                .map { intent ->
                    logD("intent: $intent")
                    actionFromIntent(intent)
                }
                .flatMap { action ->
                    logD("action: $action")
                    countryListInteractor.processAction(action)
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

    private fun intentFilter(intent: CountryListIntent): Boolean =
            if (intent is InitialIntent && !processedInitialIntent) {
                processedInitialIntent = true
                true
            } else !(intent is InitialIntent && processedInitialIntent)

}