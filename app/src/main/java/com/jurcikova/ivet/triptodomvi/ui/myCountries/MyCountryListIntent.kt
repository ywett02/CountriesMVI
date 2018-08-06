package com.jurcikova.ivet.triptodomvi.ui.myCountries

import com.jurcikova.ivet.triptodomvi.business.entity.MyCountry
import com.jurcikova.ivet.triptodomvi.mvibase.MviIntent

sealed class MyCountryListIntent: MviIntent {
    object InitialIntent : MyCountryListIntent()
    object SwipeToRefresh : MyCountryListIntent()
    data class ChangeCountryStateIntent(val myCountry: MyCountry) : MyCountryListIntent()

}