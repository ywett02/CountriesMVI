package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay

class CountryDetailInteractor(val countryRepository: CountryRepository) : MviInteractor<CountryDetailAction, CountryDetailResult> {

    override fun CoroutineScope.processAction(action: CountryDetailAction): ReceiveChannel<CountryDetailResult> =
            when (action) {
                is CountryDetailAction.LoadCountryDetailAction -> produceLoadCountryDetailAction(action.countryName)
                is CountryDetailAction.AddToFavoriteAction -> produceAddToFavoriteAction(action.countryName)
                is CountryDetailAction.RemoveFromFavoriteAction -> produceRemoveFromFavoriteAction(action.countryName)
            }

    private fun CoroutineScope.produceLoadCountryDetailAction(name: String?) = produce<CountryDetailResult> {
        send(CountryDetailResult.LoadCountryDetailResult.InProgress)
        try {
            for (country in countryRepository.getCountry(name)) {
                send(CountryDetailResult.LoadCountryDetailResult.Success(country))
            }
        } catch (exception: Exception) {
            send(CountryDetailResult.LoadCountryDetailResult.Failure(exception))
        }
    }

    private fun CoroutineScope.produceAddToFavoriteAction(countryName: String) = produce<CountryDetailResult> {
        try {
            countryRepository.run {
                addToFavorite(countryName).await()
                send(CountryDetailResult.AddToFavoriteResult.Success)
                delay(500)
                send(CountryDetailResult.AddToFavoriteResult.Reset)
            }
        } catch (exception: Exception) {
            CountryDetailResult.AddToFavoriteResult.Failure(exception)
        }
    }

    private fun CoroutineScope.produceRemoveFromFavoriteAction(countryName: String) = produce<CountryDetailResult> {
        try {
            countryRepository.run {
                removeFromFavorite(countryName).await()
                send(CountryDetailResult.RemoveFromFavoriteResult.Success)
                delay(500)
                send(CountryDetailResult.RemoveFromFavoriteResult.Reset)
            }
        } catch (exception: Exception) {
            CountryDetailResult.AddToFavoriteResult.Failure(exception)
        }
    }
}