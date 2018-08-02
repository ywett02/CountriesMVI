package com.jurcikova.ivet.triptodomvi.ui.countryList

import com.jurcikova.ivet.triptodomvi.mvibase.MviAction

sealed class CountryListAction: MviAction {
    object LoadCountriesAction: CountryListAction()
    data class LoadCountriesByNameAction(val searchQuery: String): CountryListAction()
}