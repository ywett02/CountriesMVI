package com.jurcikova.ivet.triptodomvi.ui.myCountries

import com.jurcikova.ivet.triptodomvi.business.entity.MyCountry
import com.jurcikova.ivet.triptodomvi.mvibase.MviAction

sealed class MyCountryListAction: MviAction {
    data class LoadMyCountriesAction(val isRefreshing: Boolean): MyCountryListAction()
    data class ChangeStateOfCountryAction(val myCountry: MyCountry): MyCountryListAction()
}