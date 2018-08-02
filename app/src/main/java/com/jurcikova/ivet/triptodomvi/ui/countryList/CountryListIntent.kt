package com.jurcikova.ivet.triptodomvi.ui.countryList

import com.jurcikova.ivet.triptodomvi.mvibase.MviIntent

sealed class CountryListIntent: MviIntent {
    object InitialIntent : CountryListIntent()
    data class SearchIntent(val searchQuery: String) : CountryListIntent()
}