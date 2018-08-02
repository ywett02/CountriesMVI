package com.jurcikova.ivet.triptodomvi.ui.countryList.all

import com.jurcikova.ivet.triptodomvi.mvibase.MviIntent

sealed class CountryListIntent: MviIntent {
    object InitialIntent : CountryListIntent()
}