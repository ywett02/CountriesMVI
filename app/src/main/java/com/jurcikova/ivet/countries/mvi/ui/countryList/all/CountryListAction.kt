package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countries.mvi.mvibase.MviAction

sealed class CountryListAction : MviAction {
    data class LoadCountriesAction(val isRefreshing: Boolean = false, val filterType: FilterType? = null) : CountryListAction()
    data class AddToFavoriteAction(val countryName: String) : CountryListAction()
    data class RemoveFromFavoriteAction(val countryName: String) : CountryListAction()
}