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
                    is CountryDetailResult.LoadCountryDetailResult.Success -> previousState.copyState(isLoading = false, country = result.country)
                    is CountryDetailResult.LoadCountryDetailResult.Failure -> previousState.copyState(isLoading = false, error = result.error)
                    is CountryDetailResult.LoadCountryDetailResult.InProgress -> previousState.copy(isLoading = true)
                }
                is CountryDetailResult.AddToFavoriteResult -> when (result) {
                    is CountryDetailResult.AddToFavoriteResult.Success -> previousState.copyState(isLoading = false, isFavorite = true, showMessage = true)
                    is CountryDetailResult.AddToFavoriteResult.Failure -> previousState.copyState(isLoading = false, error = result.error)
                    is CountryDetailResult.AddToFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                    is CountryDetailResult.AddToFavoriteResult.Reset -> previousState.copyState(showMessage = false)
                }
                is CountryDetailResult.RemoveFromFavoriteResult -> when (result) {
                    is CountryDetailResult.RemoveFromFavoriteResult.Success -> previousState.copyState(isLoading = false, isFavorite = false, showMessage = true)
                    is CountryDetailResult.RemoveFromFavoriteResult.Failure -> previousState.copyState(isLoading = false, error = result.error)
                    is CountryDetailResult.RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                    is CountryDetailResult.RemoveFromFavoriteResult.Reset -> previousState.copyState(showMessage = false)
                }
            }

    override suspend fun processIntents(channel: Channel<CountryDetailIntent>) {
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

    private fun intentFilter(intent: CountryDetailIntent): Boolean =
            !(intent is CountryDetailIntent.InitialIntent && state.value.initialIntentProcessed)
}