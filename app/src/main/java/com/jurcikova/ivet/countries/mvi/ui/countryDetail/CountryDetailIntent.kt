package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import com.jurcikova.ivet.countriesMVI.mvibase.MviIntent

sealed class CountryDetailIntent: MviIntent {
    data class InitialIntent(val countryName: String): CountryDetailIntent()
    data class AddToFavoriteIntent(val countryName: String) : CountryDetailIntent()
    data class RemoveFavoriteIntent(val countryName: String) : CountryDetailIntent()
}