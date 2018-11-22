package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import retrofit2.HttpException

class CountrySearchInteractor(val countryRepository: CountryRepository) : MviInteractor<CountrySearchAction, CountrySearchResult> {

	@ExperimentalCoroutinesApi
	override fun CoroutineScope.processAction(action: CountrySearchAction): ReceiveChannel<CountrySearchResult> =
		produce {
			when (action) {
				is CountrySearchAction.LoadCountriesByNameAction -> {
					produceSearchCountriesResult(action.searchQuery)
				}
			}
		}

	@ExperimentalCoroutinesApi
	private suspend fun ProducerScope<CountrySearchResult>.produceSearchCountriesResult(query: String) {
		if (query.isBlank()) {
			send(CountrySearchResult.LoadCountriesByNameResult.NotStarted)
		} else {
			send(CountrySearchResult.LoadCountriesByNameResult.InProgress(query))
			send(
				try {
					CountrySearchResult.LoadCountriesByNameResult.Success(countryRepository.getCountriesByName(query))
				} catch (httpException: HttpException) {
					CountrySearchResult.LoadCountriesByNameResult.Success(emptyList())
				} catch (exception: Exception) {
					CountrySearchResult.LoadCountriesByNameResult.Failure(exception)
				}
			)
		}
	}
}