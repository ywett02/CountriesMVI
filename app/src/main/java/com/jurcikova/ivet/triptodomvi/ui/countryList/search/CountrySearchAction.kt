package com.jurcikova.ivet.triptodomvi.ui.countryList.search

import com.jurcikova.ivet.triptodomvi.mvibase.MviAction

sealed class CountrySearchAction: MviAction {
    data class LoadCountriesByNameAction(val searchQuery: String): CountrySearchAction()
}