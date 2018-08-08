package com.jurcikova.ivet.triptodomvi.ui.countryDetail

import com.jurcikova.ivet.triptodomvi.mvibase.MviAction

sealed class CountryDetailAction: MviAction {
    data class LoadCountryDetailAction(val countryName: String): CountryDetailAction()
    data class AddToFavoriteAction(val countryName: String): CountryDetailAction()
    data class RemoveFromFavoriteAction(val countryName: String): CountryDetailAction()
}