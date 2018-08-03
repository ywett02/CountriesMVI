package com.jurcikova.ivet.triptodomvi.ui.countryList.all

import com.jurcikova.ivet.triptodomvi.mvibase.MviAction

sealed class CountryListAction : MviAction {
    data class LoadCountriesAction(val isRefreshing: Boolean) : CountryListAction()
}