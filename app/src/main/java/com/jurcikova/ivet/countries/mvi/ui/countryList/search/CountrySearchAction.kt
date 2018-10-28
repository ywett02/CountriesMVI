package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import com.jurcikova.ivet.countries.mvi.mvibase.MviAction

sealed class CountrySearchAction : MviAction {
	data class LoadCountriesByNameAction(val searchQuery: String) : CountrySearchAction()
}