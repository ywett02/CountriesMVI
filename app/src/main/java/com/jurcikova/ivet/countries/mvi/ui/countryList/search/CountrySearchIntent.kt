package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import com.jurcikova.ivet.countries.mvi.mvibase.MviIntent

sealed class CountrySearchIntent : MviIntent {
	data class SearchIntent(val searchQuery: String) : CountrySearchIntent()
	data class AddToFavoriteIntent(val countryName: String) : CountrySearchIntent()
	data class RemoveFromFavoriteIntent(val countryName: String) : CountrySearchIntent()
}