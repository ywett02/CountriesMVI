package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult
import com.strv.ktools.inject
import com.strv.ktools.logMe
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay

class CountryDetailInteractor : MviInteractor<CountryDetailAction, CountryDetailResult> {

    private val countryRepository by inject<CountryRepository>()

    override fun processAction(action: CountryDetailAction): ReceiveChannel<CountryDetailResult> {
        action.logMe()

        return when (action) {
            is CountryDetailAction.LoadCountryDetailAction -> produceLoadCountryDetailAction(action.countryName)
            is CountryDetailAction.AddToFavoriteAction -> produceAddToFavoriteAction()
            is CountryDetailAction.RemoveFromFavoriteAction -> produceRemoveFromFavoriteAction()
        }
    }

    private fun produceLoadCountryDetailAction(name: String) = produce<CountryDetailResult> {
        send(CountryDetailResult.LoadCountryDetailResult.InProgress)
        send(
                try {
                    CountryDetailResult.LoadCountryDetailResult.Success(countryRepository.getCountry(name))
                } catch (exception: Exception) {
                    CountryDetailResult.LoadCountryDetailResult.Failure(exception)
                }
        )
    }

    private fun produceAddToFavoriteAction() = produce<CountryDetailResult> {
        send(CountryDetailResult.AddToFavoriteResult.Success)
        delay(2000)
        send(CountryDetailResult.AddToFavoriteResult.Reset)
    }

    private fun produceRemoveFromFavoriteAction() = produce<CountryDetailResult> {
        send(CountryDetailResult.RemoveFromFavoriteResult.Success)
        delay(2000)
        send(CountryDetailResult.RemoveFromFavoriteResult.Reset)
    }
}