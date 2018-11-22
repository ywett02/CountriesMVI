package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import com.jurcikova.ivet.countries.mvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction.LoadCountriesByNameAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchIntent.SearchIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult.LoadCountriesByNameResult
import com.strv.ktools.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.flatMap
import kotlinx.coroutines.channels.map

class CountrySearchViewModel(private val countrySearchInteractor: CountrySearchInteractor) : BaseViewModel<CountrySearchIntent, CountrySearchAction, CountrySearchResult, CountrySearchViewState>() {

	@ExperimentalCoroutinesApi
	override val state: ConflatedBroadcastChannel<CountrySearchViewState> = ConflatedBroadcastChannel(CountrySearchViewState.idle())

	override val reduce =
		{ previousState: CountrySearchViewState, result: CountrySearchResult ->
			when (result) {
				is LoadCountriesByNameResult.NotStarted -> {
					previousState.copy(
						isLoading = false,
						error = null,
						searchNotStartedYet = true,
						countries = emptyList()
					)
				}
				is LoadCountriesByNameResult.Success -> {
					previousState.copy(
						isLoading = false,
						searchNotStartedYet = false,
						error = null,
						countries = result.countries
					)
				}
				is LoadCountriesByNameResult.Failure -> {
					previousState.copy(isLoading = false, searchNotStartedYet = false, error = result.error)
				}
				is LoadCountriesByNameResult.InProgress -> {
					previousState.copy(isLoading = true, searchNotStartedYet = false, searchQuery = result.searchQuery, error = null)
				}
			}
		}

	@ExperimentalCoroutinesApi
	@ObsoleteCoroutinesApi
	override suspend fun CoroutineScope.processIntents(channel: Channel<CountrySearchIntent>) = state.run {
		channel
			.map { intent ->
				logD("intent: $intent")
				actionFromIntent(intent)
			}
			.flatMap { action ->
				logD("action: $action")
				countrySearchInteractor.run {
					processAction(action)
				}
			}.consumeEach { result ->
				logD("result: $result")
				offer(reduce(value, result))
			}
	}

	override fun actionFromIntent(intent: CountrySearchIntent): CountrySearchAction =
		when (intent) {
			is SearchIntent -> LoadCountriesByNameAction(intent.searchQuery)
		}
}


