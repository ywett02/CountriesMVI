package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countries.mvi.business.interactor.CountryListInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.InitialIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.SwipeToRefresh
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.LoadCountriesResult
import com.strv.ktools.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.flatMap
import kotlinx.coroutines.channels.map

class CountryListViewModel(val countryListInteractor: CountryListInteractor) : BaseViewModel<CountryListIntent, CountryListAction, CountryListResult, CountryListViewState>() {

	@ExperimentalCoroutinesApi
	override val state = ConflatedBroadcastChannel(CountryListViewState.idle())

	override val reduce =
		{ previousState: CountryListViewState, result: CountryListResult ->
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
		}

	@ObsoleteCoroutinesApi
	@ExperimentalCoroutinesApi
	override suspend fun CoroutineScope.processIntents(channel: Channel<CountryListIntent>) =
		state.run {
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
					offer(reduce(value, result))
				}
		}

	override fun actionFromIntent(intent: CountryListIntent) =
		when (intent) {
			is InitialIntent -> LoadCountriesAction(false)
			is SwipeToRefresh -> LoadCountriesAction(true)
		}
}