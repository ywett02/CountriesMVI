package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

class CountryListInteractor(
	private val countryRepository: CountryRepository
) : MviInteractor<CountryListAction, CountryListResult> {

	@ExperimentalCoroutinesApi
	override fun CoroutineScope.processAction(action: CountryListAction): ReceiveChannel<CountryListResult> =
		produce {
			when (action) {
				is LoadCountriesAction -> {
					produceLoadCountriesResult(action.isRefreshing)
				}
			}
		}

	@ExperimentalCoroutinesApi
	private suspend fun ProducerScope<CountryListResult>.produceLoadCountriesResult(isRefreshing: Boolean) {
		send(CountryListResult.LoadCountriesResult.InProgress(isRefreshing))
		send(
			try {
				CountryListResult.LoadCountriesResult.Success(countryRepository.getAllCountries())
			} catch (exception: Exception) {
				CountryListResult.LoadCountriesResult.Failure(exception)
			}
		)
	}
}
