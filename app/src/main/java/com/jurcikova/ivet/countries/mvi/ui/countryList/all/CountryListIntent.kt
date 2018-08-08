package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countriesMVI.mvibase.MviIntent

sealed class CountryListIntent: MviIntent {
    object InitialIntent : CountryListIntent()
    object SwipeToRefresh : CountryListIntent()
}