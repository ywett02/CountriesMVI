package com.jurcikova.ivet.triptodomvi.ui.myCountries

import com.jurcikova.ivet.triptodomvi.business.entity.MyCountry
import com.jurcikova.ivet.triptodomvi.mvibase.MviResult

sealed class MyCountryListResult : MviResult{
    sealed class LoadCountriesResult : MyCountryListResult() {
        data class Success(val myCountries: List<MyCountry>) :LoadCountriesResult()
        data class Failure(val error: Throwable) : LoadCountriesResult()
        data class InProgress(val isRefreshing: Boolean) : LoadCountriesResult()
    }

    sealed class UpdateCountryResult: MyCountryListResult() {
        data class Success(val visited: Boolean) : UpdateCountryResult()
        data class Failure(val error: Throwable) : UpdateCountryResult()
        object ResetState : UpdateCountryResult()
    }


}