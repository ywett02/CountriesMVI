package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce

class CountryListInteractor(val countryRepository: CountryRepository) : MviInteractor<CountryListAction, CountryListResult> {

    override fun CoroutineScope.processAction(action: CountryListAction): ReceiveChannel<CountryListResult> =
            when (action) {
                is LoadCountriesAction -> {
                    produceLoadCountriesResult(action.isRefreshing)
                }
            }

    private fun CoroutineScope.produceLoadCountriesResult(isRefreshing: Boolean) = produce<CountryListResult> {
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