package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay
import retrofit2.HttpException

class CountrySearchInteractor(val countryRepository: CountryRepository) : MviInteractor<CountrySearchAction, CountrySearchResult> {

    override fun CoroutineScope.processAction(action: CountrySearchAction): ReceiveChannel<CountrySearchResult> =
            when (action) {
                is CountrySearchAction.LoadCountriesByNameAction -> {
                    produceSearchCountriesResult(action.searchQuery)
                }
                is CountrySearchAction.AddToFavoriteAction -> {
                    produceAddToFavoriteResult(action.countryName)
                }
                is CountrySearchAction.RemoveFromFavoriteAction -> {
                    produceRemoveFromFavoriteResult(action.countryName)
                }
            }

    private fun CoroutineScope.produceSearchCountriesResult(query: String) = produce<CountrySearchResult> {
        if (query.isBlank()) {
            send(CountrySearchResult.LoadCountriesByNameResult.NotStarted)
        } else {
            send(CountrySearchResult.LoadCountriesByNameResult.InProgress(query))
            try {
                for (items in countryRepository.getCountriesByName(query)) {
                    send(CountrySearchResult.LoadCountriesByNameResult.Success(items))
                }
            } catch (httpException: HttpException) {
                send(CountrySearchResult.LoadCountriesByNameResult.Success(emptyList()))
            } catch (exception: Exception) {
                send(CountrySearchResult.LoadCountriesByNameResult.Failure(exception))
            }
        }
    }

    private fun CoroutineScope.produceAddToFavoriteResult(countryName: String) = produce<CountrySearchResult> {
        try {
            countryRepository.run {
                addToFavorite(countryName).await()
                send(CountrySearchResult.AddToFavoriteResult.Success)
                delay(2000)
                send(CountrySearchResult.AddToFavoriteResult.Reset)
            }
        } catch (exception: Exception) {
            send(CountrySearchResult.AddToFavoriteResult.Failure(exception))
        }
    }

    private fun CoroutineScope.produceRemoveFromFavoriteResult(countryName: String) = produce<CountrySearchResult> {
        try {
            countryRepository.run {
                removeFromFavorite(countryName).await()
                send(CountrySearchResult.RemoveFromFavoriteResult.Success)
                delay(2000)
                send(CountrySearchResult.RemoveFromFavoriteResult.Reset)
            }
        } catch (exception: Exception) {
            send(CountrySearchResult.AddToFavoriteResult.Failure(exception))
        }
    }
}