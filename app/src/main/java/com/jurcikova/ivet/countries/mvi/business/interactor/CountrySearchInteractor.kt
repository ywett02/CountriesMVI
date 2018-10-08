package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import retrofit2.HttpException

class CountrySearchInteractor(val countryRepository: CountryRepository) : MviInteractor<CountrySearchAction, CountrySearchResult> {

    override fun CoroutineScope.processAction(action: CountrySearchAction): ReceiveChannel<CountrySearchResult> =
            when (action) {
                is CountrySearchAction.LoadCountriesByNameAction -> {
                    produceSearchCountriesResult(action.searchQuery)
                }
            }

    private fun CoroutineScope.produceSearchCountriesResult(query: String) = produce<CountrySearchResult> {
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