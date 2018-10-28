package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import com.jurcikova.ivet.countries.mvi.business.interactor.CountryDetailInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction.AddToFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction.LoadCountryDetailAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction.RemoveFromFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.AddToFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.InitialIntent
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.RemoveFromFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult.AddToFavoriteResult
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult.LoadCountryDetailResult
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult.RemoveFromFavoriteResult
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.flatMap
import kotlinx.coroutines.experimental.channels.map

class CountryDetailViewModel(val countryDetailInteractor: CountryDetailInteractor)
	: BaseViewModel<CountryDetailIntent, CountryDetailAction, CountryDetailResult, CountryDetailViewState>() {

	override val state = ConflatedBroadcastChannel(CountryDetailViewState.idle())

	override val reduce =
		{ previousState: CountryDetailViewState, result: CountryDetailResult ->
			when (result) {
				is LoadCountryDetailResult -> when (result) {
					is LoadCountryDetailResult.Success -> previousState.copy(isLoading = false, country = result.country, initial = false)
					is LoadCountryDetailResult.Failure -> previousState.copy(isLoading = false, error = result.error, initial = false)
					is LoadCountryDetailResult.InProgress -> previousState.copy(isLoading = true)
				}
				is AddToFavoriteResult -> when (result) {
					is AddToFavoriteResult.Success -> previousState.copy(isLoading = false, isFavorite = true, showMessage = true, initial = false)
					is AddToFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error, initial = false)
					is AddToFavoriteResult.InProgress -> previousState.copy(isLoading = true)
					is AddToFavoriteResult.Reset -> previousState.copy(showMessage = false)
				}
				is RemoveFromFavoriteResult -> when (result) {
					is RemoveFromFavoriteResult.Success -> previousState.copy(isLoading = false, isFavorite = false, showMessage = true, initial = false)
					is RemoveFromFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error, initial = false)
					is RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
					is RemoveFromFavoriteResult.Reset -> previousState.copy(showMessage = false)
				}
			}
		}

	override suspend fun processIntents(channel: Channel<CountryDetailIntent>) =
		state.run {
			channel
				.map { intent ->
					logD("intent: $intent")
					actionFromIntent(intent)
				}
				.flatMap { action ->
					logD("action: $action")
					countryDetailInteractor.run {
						processAction(action)
					}
				}.consumeEach { result ->
					logD("result: $result")
					offer(reduce(value, result))
				}
		}

	override fun actionFromIntent(intent: CountryDetailIntent): CountryDetailAction =
		when (intent) {
			is InitialIntent -> LoadCountryDetailAction(intent.countryName)
			is AddToFavoriteIntent -> AddToFavoriteAction(intent.countryName)
			is RemoveFromFavoriteIntent -> RemoveFromFavoriteAction(intent.countryName)
		}
}