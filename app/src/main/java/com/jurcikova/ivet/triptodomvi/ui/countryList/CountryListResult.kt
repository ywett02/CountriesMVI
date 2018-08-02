package com.jurcikova.ivet.triptodomvi.ui.countryList

import com.jurcikova.ivet.triptodomvi.business.entity.Country
import com.jurcikova.ivet.triptodomvi.mvibase.MviResult

sealed class CountryListResult : MviResult {
    sealed class LoadCountriesResult : CountryListResult() {
        data class Success(val countries: List<Country>) : LoadCountriesResult()
        data class Failure(val error: Throwable) : LoadCountriesResult()
        object InProgress : LoadCountriesResult()
    }
    sealed class LoadCountriesByNameResult : CountryListResult() {
        data class Success(val countries: List<Country>) : LoadCountriesByNameResult()
        data class Failure(val error: Throwable) : LoadCountriesByNameResult()
        data class InProgress(val searchQuery: String) : LoadCountriesByNameResult()
    }
}