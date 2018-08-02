package com.jurcikova.ivet.triptodomvi.ui.countryList.search

import com.jurcikova.ivet.triptodomvi.business.entity.Country
import com.jurcikova.ivet.triptodomvi.mvibase.MviResult

sealed class CountrySearchResult : MviResult {
    sealed class LoadCountriesByNameResult : CountrySearchResult() {
        object NotStarted : LoadCountriesByNameResult()
        data class Success(val countries: List<Country>) : LoadCountriesByNameResult()
        data class Failure(val error: Throwable) : LoadCountriesByNameResult()
        data class InProgress(val searchQuery: String) : LoadCountriesByNameResult()
    }
}