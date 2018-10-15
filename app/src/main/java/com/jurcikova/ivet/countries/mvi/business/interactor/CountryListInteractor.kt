package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.FilterType
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay

class CountryListInteractor(val countryRepository: CountryRepository) : MviInteractor<CountryListAction, CountryListResult> {

    override fun CoroutineScope.processAction(action: CountryListAction): ReceiveChannel<CountryListResult> =
            when (action) {
                is LoadCountriesAction -> {
                    produceLoadCountriesResult(action.isRefreshing, action.filterType)
                }
                is CountryListAction.AddToFavoriteAction -> {
                    produceAddToFavoriteResult(action.countryName)
                }
                is CountryListAction.RemoveFromFavoriteAction -> {
                    produceRemoveFromFavoriteResult(action.countryName)
                }
            }

    private fun CoroutineScope.produceLoadCountriesResult(isRefreshing: Boolean, filterType: FilterType?) = produce<CountryListResult> {
        send(CountryListResult.LoadCountriesResult.InProgress(isRefreshing))
        try {
            for (items in countryRepository.getAllCountries()) {
                send(CountryListResult.LoadCountriesResult.Success(items, filterType))
            }
        } catch (exception: java.lang.Exception) {
            send(CountryListResult.LoadCountriesResult.Failure(exception))
        }
    }

    private fun CoroutineScope.produceAddToFavoriteResult(countryName: String) = produce<CountryListResult> {
        try {
            countryRepository.run {
                addToFavorite(countryName).await()
                send(CountryListResult.AddToFavoriteResult.Success)
                delay(2000)
                send(CountryListResult.AddToFavoriteResult.Reset)
            }
        } catch (exception: Exception) {
            send(CountryListResult.AddToFavoriteResult.Failure(exception))
        }
    }

    private fun CoroutineScope.produceRemoveFromFavoriteResult(countryName: String) = produce<CountryListResult> {
        try {
            countryRepository.run {
                removeFromFavorite(countryName).await()
                send(CountryListResult.RemoveFromFavoriteResult.Success)
                delay(2000)
                send(CountryListResult.RemoveFromFavoriteResult.Reset)
            }
        } catch (exception: Exception) {
            send(CountryListResult.AddToFavoriteResult.Failure(exception))
        }
    }
}