package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import com.jurcikova.ivet.countries.mvi.mvibase.MviAction

sealed class CountrySearchAction : MviAction {
	data class LoadCountriesByNameAction(val searchQuery: String) : CountrySearchAction()
	data class AddToFavoriteAction(val countryName: String) : CountrySearchAction()
	data class RemoveFromFavoriteAction(val countryName: String) : CountrySearchAction()
}