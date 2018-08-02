package com.jurcikova.ivet.triptodomvi.ui.countryList.search

import com.jurcikova.ivet.triptodomvi.mvibase.MviIntent

sealed class CountrySearchIntent: MviIntent {
    data class SearchIntent(val searchQuery: String) : CountrySearchIntent()
}