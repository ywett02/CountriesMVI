package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countriesMVI.mvibase.MviIntent

sealed class CountryListIntent: MviIntent {
    object InitialIntent : CountryListIntent()
    data class ChangeFilterIntent(val filterType: FilterType): CountryListIntent()
    data class AddToFavoriteIntent(val countryName: String): CountryListIntent()
    data class RemoveFromFavoriteIntent(val countryName: String): CountryListIntent()
}