package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.countries.mvi.ui.base.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction.AddToFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction.LoadCountriesByNameAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction.RemoveFromFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchIntent.AddToFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchIntent.RemoveFromFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchIntent.SearchIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult.AddToFavoriteResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult.LoadCountriesByNameResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult.RemoveFromFavoriteResult
import com.strv.ktools.logD
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class CountrySearchViewModel(
	countrySearchInteractor: CountrySearchInteractor
) : BaseViewModel<CountrySearchIntent, CountrySearchAction, CountrySearchResult, CountrySearchViewState>() {

	override val reducer = BiFunction { previousState: CountrySearchViewState, result: CountrySearchResult ->
		when (result) {

			is LoadCountriesByNameResult ->
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
					is LoadCountriesByNameResult.Failure -> previousState.copy(isLoading = false, searchNotStartedYet = false, error = result.error)
					is LoadCountriesByNameResult.InProgress -> previousState.copy(isLoading = true, searchNotStartedYet = false, searchQuery = result.searchQuery, error = null)
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
					is RemoveFromFavoriteResult.Success -> previousState.copy(
						isLoading = false, error = null, message = MessageType.RemoveFromFavorite)
					is RemoveFromFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
					is RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
					is RemoveFromFavoriteResult.Reset -> previousState.copy(message = null)
				}
		}
	}

	override val statesLiveData: LiveData<CountrySearchViewState> =
		LiveDataReactiveStreams.fromPublisher(
			intentsSubject
				.map(this::actionFromIntent)
				.doOnNext { action ->
					logD("action: $action")
				}
				//gate to the business logic
				.compose(countrySearchInteractor.actionProcessor)
				.scan(CountrySearchViewState.idle(), reducer)
				.distinctUntilChanged()
				.replay(1)
				.autoConnect()
				.toFlowable(BackpressureStrategy.BUFFER))

	override fun processIntents(intents: Observable<CountrySearchIntent>) =
		intents
			.doOnNext { intent ->
				logD("intent: $intent")
			}
			.subscribe(intentsSubject)

	override fun actionFromIntent(intent: CountrySearchIntent): CountrySearchAction =
		when (intent) {
			is SearchIntent -> LoadCountriesByNameAction(intent.searchQuery)
			is AddToFavoriteIntent -> AddToFavoriteAction(intent.countryName)
			is RemoveFromFavoriteIntent -> RemoveFromFavoriteAction(intent.countryName)
		}
}


