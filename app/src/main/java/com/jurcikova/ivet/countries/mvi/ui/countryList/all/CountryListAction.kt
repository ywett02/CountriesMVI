package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countries.mvi.mvibase.MviAction

sealed class CountryListAction : MviAction {
    data class LoadCountriesAction(val isRefreshing: Boolean) : CountryListAction()
}