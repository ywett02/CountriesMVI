package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ProducerScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay

class CountryDetailInteractor(val countryRepository: CountryRepository) : MviInteractor<CountryDetailAction, CountryDetailResult> {

	override fun CoroutineScope.processAction(action: CountryDetailAction): ReceiveChannel<CountryDetailResult> = produce {
		when (action) {
			is CountryDetailAction.LoadCountryDetailAction -> produceLoadCountryDetailAction(action.countryName)
			is CountryDetailAction.AddToFavoriteAction -> produceAddToFavoriteAction()
			is CountryDetailAction.RemoveFromFavoriteAction -> produceRemoveFromFavoriteAction()
		}
	}

	private suspend fun ProducerScope<CountryDetailResult>.produceLoadCountryDetailAction(name: String?) {
		send(CountryDetailResult.LoadCountryDetailResult.InProgress)
		send(
			try {
				CountryDetailResult.LoadCountryDetailResult.Success(countryRepository.getCountry(name))
			} catch (exception: Exception) {
				CountryDetailResult.LoadCountryDetailResult.Failure(exception)
			}
		)
	}

	private suspend fun ProducerScope<CountryDetailResult>.produceAddToFavoriteAction() {
		send(CountryDetailResult.AddToFavoriteResult.Success)
		delay(2000)
		send(CountryDetailResult.AddToFavoriteResult.Reset)
	}

	private suspend fun ProducerScope<CountryDetailResult>.produceRemoveFromFavoriteAction() {
		send(CountryDetailResult.RemoveFromFavoriteResult.Success)
		delay(2000)
		send(CountryDetailResult.RemoveFromFavoriteResult.Reset)
	}
}