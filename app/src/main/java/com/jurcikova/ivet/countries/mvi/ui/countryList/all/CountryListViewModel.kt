package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.business.interactor.CountryListInteractor
import com.jurcikova.ivet.countries.mvi.ui.base.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.AddToFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.RemoveFromFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.UpdateCountryListAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.AddToFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.ChangeFilterIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.InitialIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.RemoveFromFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.SwipeToRefresh
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.AddToFavoriteResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.LoadCountriesResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.RemoveFromFavoriteResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.UpdateCountryListResult
import com.strv.ktools.logD
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class CountryListViewModel(
	val countryListInteractor: CountryListInteractor
) : BaseViewModel<CountryListIntent, CountryListAction, CountryListResult, CountryListViewState>() {

	override val reducer = BiFunction { previousState: CountryListViewState, result: CountryListResult ->
		when (result) {

			is LoadCountriesResult ->
				when (result) {
					is LoadCountriesResult.Success -> {
						previousState.copy(
							isLoading = false,
							isRefreshing = false,
							filterType = result.filterType,
							error = null,
							initial = false
						)
					}
					is LoadCountriesResult.Failure -> {
						previousState.copy(isLoading = false, error = result.error, initial = false)
					}
					is LoadCountriesResult.InProgress -> {
						if (result.isRefreshing) {
							previousState.copy(isLoading = false, isRefreshing = true)
						} else previousState.copy(isLoading = true, isRefreshing = false, initial = false)
					}
				}

			is AddToFavoriteResult ->
				when (result) {
					is AddToFavoriteResult.Success -> previousState.copy(
						isLoading = false, error = null, message = MessageType.AddToFavorite)
					is AddToFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
					is AddToFavoriteResult.InProgress -> previousState.copy(isLoading = true)
					is AddToFavoriteResult.Reset -> previousState.copy(message = null)
				}

			is RemoveFromFavoriteResult ->
				when (result) {
					is RemoveFromFavoriteResult.Success -> {
						previousState.copy(isLoading = false, error = null, message = MessageType.RemoveFromFavorite)
					}
					is RemoveFromFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
					is RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
					is RemoveFromFavoriteResult.Reset -> previousState.copy(message = null)
				}

			is UpdateCountryListResult ->
				when (result) {
					is UpdateCountryListResult.Success -> {
						previousState.copy(
							initial = false,
							countries = applyFilters(result.countries, previousState.filterType)
						)
					}
					is UpdateCountryListResult.Failure -> {
						previousState.copy(
							initial = false,
							error = result.error)
					}
				}
		}
	}

	override val statesLiveData: LiveData<CountryListViewState> =
		LiveDataReactiveStreams.fromPublisher(
			intentsSubject
				.doOnNext { intent ->
					logD("intent: $intent")
				}
				.map(this::actionFromIntent)
				//observe app state - database
				.mergeWith(countryListInteractor.countryRepository.getCountries().map { UpdateCountryListAction(it) })
				.doOnNext { action ->
					logD("action: $action")
				}
				.compose(countryListInteractor.actionProcessor)
				// Cache each state and pass it to the reducer to create a new state from
				// the previous cached one and the latest Result emitted from the action processor.
				// The Scan operator is used here for the caching.
				.scan(CountryListViewState.idle(), reducer)
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

	override fun processIntents(intents: Observable<CountryListIntent>) =
		intents
			.doOnNext { intent ->
				logD("intent: $intent")
			}
			.subscribe(intentsSubject)

	override fun actionFromIntent(intent: CountryListIntent): CountryListAction =
		when (intent) {
			is InitialIntent -> LoadCountriesAction(false)
			is SwipeToRefresh -> LoadCountriesAction(true)
			is ChangeFilterIntent -> LoadCountriesAction(filterType = intent.filterType)
			is AddToFavoriteIntent -> AddToFavoriteAction(intent.countryName)
			is RemoveFromFavoriteIntent -> RemoveFromFavoriteAction(intent.countryName)
		}

	private fun applyFilters(countries: List<Country>, filterType: FilterType): List<Country> =
		if (filterType == FilterType.Favorite) countries.filter { it.isFavorite } else countries
}