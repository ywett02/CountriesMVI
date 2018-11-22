package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.business.interactor.CountryDetailInteractor
import com.jurcikova.ivet.countries.mvi.ui.base.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction.AddToFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction.LoadCountryDetailAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction.RemoveFromFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.AddToFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.InitialIntent
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.RemoveFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult.AddToFavoriteResult
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult.LoadCountryDetailResult
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult.RemoveFromFavoriteResult
import com.strv.ktools.logD
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class CountryDetailViewModel(countryDetailInteractor: CountryDetailInteractor) : BaseViewModel<CountryDetailIntent, CountryDetailAction, CountryDetailResult, CountryDetailViewState>() {

	override val reducer = BiFunction { previousState: CountryDetailViewState, result: CountryDetailResult ->
		when (result) {
			is LoadCountryDetailResult -> when (result) {
				is LoadCountryDetailResult.Success -> previousState.copy(isLoading = false, country = result.country, initial = false)
				is LoadCountryDetailResult.Failure -> previousState.copy(isLoading = false, error = result.error, initial = false)
				is LoadCountryDetailResult.InProgress -> previousState.copy(isLoading = true, initial = false)
			}
			is AddToFavoriteResult -> when (result) {
				is AddToFavoriteResult.Success -> previousState.copy(isLoading = false, country = previousState.country.also { it?.isFavorite = true }, message = MessageType.AddToFavorite, error = null)
				is AddToFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
				is AddToFavoriteResult.InProgress -> previousState.copy(isLoading = true)
				is AddToFavoriteResult.Reset -> previousState.copy(message = null)
			}
			is RemoveFromFavoriteResult -> when (result) {
				is RemoveFromFavoriteResult.Success -> previousState.copy(isLoading = false, country = previousState.country.also { it?.isFavorite = false }, message = MessageType.RemoveFromFavorite, error = null)
				is RemoveFromFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
				is RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
				is RemoveFromFavoriteResult.Reset -> previousState.copy(message = null)
			}
		}
	}

	override val statesLiveData: LiveData<CountryDetailViewState> =
		LiveDataReactiveStreams.fromPublisher(
			intentsSubject
				.map(this::actionFromIntent)
				.doOnNext { action ->
					logD("action: $action")
				}
				.compose(countryDetailInteractor.actionProcessor)
				// Cache each state and pass it to the reducer to create a new state from
				// the previous cached one and the latest Result emitted from the action processor.
				// The Scan operator is used here for the caching.
				.scan(CountryDetailViewState.idle(), reducer)
				// When a reducer just emits previousState, there's no reason to call render. In fact,
				// redrawing the UI in cases like this can cause jank (e.g. messing up snackbar animations
				// by showing the same snackbar twice in rapid succession).
				.distinctUntilChanged()
				// Emit the last one event of the stream on subscription
				// Useful when a View rebinds to the ViewModel after rotation.
				.replay(1)
				// Create the stream on creation without waiting for anyone to subscribe
				// This allows the stream to stay alive even when the UI disconnects and
				// match the stream's lifecycle to the ViewModel's one.
				.autoConnect(0)
				.toFlowable(BackpressureStrategy.BUFFER))

	override fun processIntents(intents: Observable<CountryDetailIntent>) =
		intents
			.doOnNext { intent ->
				logD("intent: $intent")
			}
			.subscribe(intentsSubject)

	override fun actionFromIntent(intent: CountryDetailIntent): CountryDetailAction =
		when (intent) {
			is InitialIntent -> LoadCountryDetailAction(intent.countryName)
			is AddToFavoriteIntent -> AddToFavoriteAction(intent.countryName)
			is RemoveFavoriteIntent -> RemoveFromFavoriteAction(intent.countryName)
		}
}