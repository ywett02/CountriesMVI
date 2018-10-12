package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import com.jurcikova.ivet.countries.mvi.business.interactor.CountryDetailInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.channels.*

class CountryDetailViewModel(val countryDetailInteractor: CountryDetailInteractor) : BaseViewModel<CountryDetailIntent, CountryDetailAction, CountryDetailResult, CountryDetailViewState>() {

    override val state = ConflatedBroadcastChannel(CountryDetailViewState.idle())

    override fun reducer(previousState: CountryDetailViewState, result: CountryDetailResult) =
            when (result) {
                is CountryDetailResult.LoadCountryDetailResult -> when (result) {
                    is CountryDetailResult.LoadCountryDetailResult.Success -> previousState.copy(isLoading = false, country = result.country, initial = false)
                    is CountryDetailResult.LoadCountryDetailResult.Failure -> previousState.copy(isLoading = false, error = result.error, initial = false)
                    is CountryDetailResult.LoadCountryDetailResult.InProgress -> previousState.copy(isLoading = true)
                }
                is CountryDetailResult.AddToFavoriteResult -> when (result) {
                    is CountryDetailResult.AddToFavoriteResult.Success -> previousState.copy(isLoading = false, isFavorite = true, showMessage = true, initial = false)
                    is CountryDetailResult.AddToFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error, initial = false)
                    is CountryDetailResult.AddToFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                    is CountryDetailResult.AddToFavoriteResult.Reset -> previousState.copy(showMessage = false)
                }
                is CountryDetailResult.RemoveFromFavoriteResult -> when (result) {
                    is CountryDetailResult.RemoveFromFavoriteResult.Success -> previousState.copy(isLoading = false, isFavorite = false, showMessage = true, initial = false)
                    is CountryDetailResult.RemoveFromFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error, initial = false)
                    is CountryDetailResult.RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                    is CountryDetailResult.RemoveFromFavoriteResult.Reset -> previousState.copy(showMessage = false)
                }
            }

    override suspend fun processIntents(channel: Channel<CountryDetailIntent>) {
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
                    state.offer(reducer(state.value, result))
                }
    }

    override fun actionFromIntent(intent: CountryDetailIntent): CountryDetailAction =
            when (intent) {
                is CountryDetailIntent.InitialIntent -> CountryDetailAction.LoadCountryDetailAction(intent.countryName)
                is CountryDetailIntent.AddToFavoriteIntent -> CountryDetailAction.AddToFavoriteAction(intent.countryName)
                is CountryDetailIntent.RemoveFromFavoriteIntent -> CountryDetailAction.RemoveFromFavoriteAction(intent.countryName)
            }
}