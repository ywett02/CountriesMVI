package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult
import com.strv.ktools.inject
import com.strv.ktools.logMe
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce

class CountryListInteractor : MviInteractor<CountryListAction, CountryListResult> {

    private val countryRepository by inject<CountryRepository>()

    override fun processAction(action: CountryListAction): ReceiveChannel<CountryListResult> {
        action.logMe()

        when (action) {
            is LoadCountriesAction -> {
                return produceLoadCountriesResult(action.isRefreshing)
            }
        }
    }

    private fun produceLoadCountriesResult(isRefreshing: Boolean) = produce<CountryListResult> {
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