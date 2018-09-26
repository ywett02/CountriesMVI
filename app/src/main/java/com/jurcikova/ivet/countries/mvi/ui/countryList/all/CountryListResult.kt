package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.mvibase.MviResult

sealed class CountryListResult : MviResult {
    sealed class LoadCountriesResult : CountryListResult() {
        data class Success(val countries: List<Country>) : LoadCountriesResult()
        data class Failure(val error: Throwable) : LoadCountriesResult()
        object InProgress : LoadCountriesResult()
    }
    sealed class AddToFavoriteResult: CountryListResult() {
        data class Success(val countryName: String) : AddToFavoriteResult()
        data class Failure(val error: Throwable) : AddToFavoriteResult()
        object InProgress : AddToFavoriteResult()
        object Reset: AddToFavoriteResult()
    }
    sealed class RemoveFromFavoriteResult: CountryListResult() {
        data class Success(val countryName: String) : RemoveFromFavoriteResult()
        data class Failure(val error: Throwable) : RemoveFromFavoriteResult()
        object InProgress : RemoveFromFavoriteResult()
        object Reset: RemoveFromFavoriteResult()
    }
}