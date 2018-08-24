package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import com.jurcikova.ivet.countries.mvi.business.interactor.CountryDetailInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.strv.ktools.inject
import com.strv.ktools.logMe
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consume

class CountryDetailViewModel : BaseViewModel<CountryDetailIntent, CountryDetailAction, CountryDetailResult, CountryDetailViewState>() {

    private val countryDetailInteractor by inject<CountryDetailInteractor>()

    override fun reducer(previousState: CountryDetailViewState, result: CountryDetailResult) =
            when (result) {
                is CountryDetailResult.LoadCountryDetailResult -> when (result) {
                    is CountryDetailResult.LoadCountryDetailResult.Success -> previousState.copy(isLoading = false, country = result.country)
                    is CountryDetailResult.LoadCountryDetailResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is CountryDetailResult.LoadCountryDetailResult.InProgress -> previousState.copy(isLoading = true)
                }
                is CountryDetailResult.AddToFavoriteResult -> when (result) {
                    is CountryDetailResult.AddToFavoriteResult.Success -> previousState.copy(isLoading = false, isFavorite = true, showMessage = true)
                    is CountryDetailResult.AddToFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is CountryDetailResult.AddToFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                    is CountryDetailResult.AddToFavoriteResult.Reset -> previousState.copy(showMessage = false)
                }
                is CountryDetailResult.RemoveFromFavoriteResult -> when (result) {
                    is CountryDetailResult.RemoveFromFavoriteResult.Success -> previousState.copy(isLoading = false, isFavorite = false, showMessage = true)
                    is CountryDetailResult.RemoveFromFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is CountryDetailResult.RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                    is CountryDetailResult.RemoveFromFavoriteResult.Reset -> previousState.copy(showMessage = false)
                }
            }

    override val intentProcessor = actor<CountryDetailIntent> {
        var containsInitialIntent = false

        for (intent in channel) {
            if (intent is CountryDetailIntent.InitialIntent && !containsInitialIntent) {
                containsInitialIntent = true
                actions.send(actionFromIntent(intent))
            } else if (intent !is CountryDetailIntent.InitialIntent) {
                actions.send(actionFromIntent(intent))
            }
        }
    }

    override val actions = actor<CountryDetailAction> {
        for (action in channel) {
            action.logMe()

            countryDetailInteractor.processAction(action).consume {
                for (result in this) {
                    result.logMe()
                    state.offer(reducer(state.value, result))
                }
            }
        }
    }

    override val state = ConflatedBroadcastChannel(CountryDetailViewState.idle())

    override fun actionFromIntent(intent: CountryDetailIntent): CountryDetailAction {
        return when (intent) {
            is CountryDetailIntent.InitialIntent -> CountryDetailAction.LoadCountryDetailAction(intent.countryName)
            is CountryDetailIntent.AddToFavoriteIntent -> CountryDetailAction.AddToFavoriteAction(intent.countryName)
            is CountryDetailIntent.RemoveFromFavoriteIntent -> CountryDetailAction.RemoveFromFavoriteAction(intent.countryName)
        }
    }
}