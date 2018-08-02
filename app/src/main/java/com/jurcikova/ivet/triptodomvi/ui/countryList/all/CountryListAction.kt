package com.jurcikova.ivet.triptodomvi.ui.countryList.all

import com.jurcikova.ivet.triptodomvi.mvibase.MviAction

sealed class CountryListAction: MviAction {
    object LoadCountriesAction: CountryListAction()
}