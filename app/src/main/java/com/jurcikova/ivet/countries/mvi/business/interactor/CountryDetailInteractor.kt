package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay

class CountryDetailInteractor(val countryRepository: CountryRepository) : MviInteractor<CountryDetailAction, CountryDetailResult> {

	@ExperimentalCoroutinesApi
	override fun CoroutineScope.processAction(action: CountryDetailAction): ReceiveChannel<CountryDetailResult> =
		produce {
			when (action) {
				is CountryDetailAction.LoadCountryDetailAction -> produceLoadCountryDetailAction(action.countryName)
				is CountryDetailAction.AddToFavoriteAction -> produceAddToFavoriteAction()
				is CountryDetailAction.RemoveFromFavoriteAction -> produceRemoveFromFavoriteAction()
			}
		}

	@ExperimentalCoroutinesApi
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

	@ExperimentalCoroutinesApi
	private suspend fun ProducerScope<CountryDetailResult>.produceAddToFavoriteAction() {
		send(CountryDetailResult.AddToFavoriteResult.Success)
		delay(2000)
		send(CountryDetailResult.AddToFavoriteResult.Reset)
	}

	@ExperimentalCoroutinesApi
	private suspend fun ProducerScope<CountryDetailResult>.produceRemoveFromFavoriteAction() {
		send(CountryDetailResult.RemoveFromFavoriteResult.Success)
		delay(2000)
		send(CountryDetailResult.RemoveFromFavoriteResult.Reset)
	}
}