package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import com.jurcikova.ivet.countries.mvi.mvibase.MviIntent

sealed class CountryDetailIntent: MviIntent {
    data class InitialIntent(val countryName: String?): CountryDetailIntent()
    data class AddToFavoriteIntent(val countryName: String) : CountryDetailIntent()
    data class RemoveFromFavoriteIntent(val countryName: String) : CountryDetailIntent()
}