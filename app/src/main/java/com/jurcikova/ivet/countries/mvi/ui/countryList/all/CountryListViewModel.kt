package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.business.interactor.CountryListInteractor
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.*
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.*
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.channels.*

class CountryListViewModel(val countryListInteractor: CountryListInteractor) : BaseViewModel<CountryListIntent, CountryListAction, CountryListResult, CountryListViewState>() {

    override val state = ConflatedBroadcastChannel(CountryListViewState.idle())

    override suspend fun processIntents(): SendChannel<CountryListIntent> = actor<CountryListIntent> {
        map { intent ->
            logD("intent: $intent")
            actionFromIntent(intent = intent)
        }.flatMap { action ->
            logD("action: $action")
            countryListInteractor.run {
                processAction(action)
            }
        }.consumeEach { result ->
            logD("result: $result")
            state.send(reducer(state.value, result))
        }
    }

    override fun reducer(previousState: CountryListViewState, result: CountryListResult) =
            when (result) {
                is LoadCountriesResult -> when (result) {
                    is LoadCountriesResult.Success -> {
                        val filterType = result.filterType ?: previousState.filterType
                        previousState.copy(
                                isLoading = false,
                                isRefreshing = false,
                                filterType = filterType,
                                countries = applyFilters(result.countries, filterType),
                                error = null,
                                initial = false
                        )
                    }
                    is LoadCountriesResult.Failure -> previousState.copy(isLoading = false, error = result.error, initial = false)
                    is LoadCountriesResult.InProgress -> {
                        if (result.isRefreshing) {
                            previousState.copy(isLoading = false, isRefreshing = true)
                        } else previousState.copy(isLoading = true, isRefreshing = false)
                    }
                }
                is AddToFavoriteResult -> when (result) {
                    is AddToFavoriteResult.Success -> previousState.copy(
                            isLoading = false, error = null, message = MessageType.AddToFavorite)
                    is AddToFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is AddToFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                    is AddToFavoriteResult.Reset -> previousState.copy(message = null)
                }
                is RemoveFromFavoriteResult -> when (result) {
                    is RemoveFromFavoriteResult.Success -> previousState.copy(isLoading = false, error = null, message = MessageType.RemoveFromFavorite)
                    is RemoveFromFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                    is RemoveFromFavoriteResult.Reset -> previousState.copy(message = null)
                }
            }

    override fun actionFromIntent(intent: CountryListIntent): CountryListAction {
        return when (intent) {
            is InitialIntent -> LoadCountriesAction(false)
            is SwipeToRefresh -> LoadCountriesAction(true)
            is ChangeFilterIntent -> LoadCountriesAction(filterType = intent.filterType)
            is AddToFavoriteIntent -> CountryListAction.AddToFavoriteAction(intent.countryName)
            is RemoveFromFavoriteIntent -> CountryListAction.RemoveFromFavoriteAction(intent.countryName)
        }
    }

    private fun applyFilters(countries: List<Country>, filterType: FilterType): List<Country> =
            if (filterType == FilterType.Favorite) countries.filter { it.isFavorite } else countries
}