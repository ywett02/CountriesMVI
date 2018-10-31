package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.mvibase.MviAction

sealed class CountryDetailAction : MviAction {
	data class LoadCountryDetailAction(val countryName: String?) : CountryDetailAction()
	data class UpdateCountryDetailAction(val country: Country) : CountryDetailAction()
	data class AddToFavoriteAction(val countryName: String) : CountryDetailAction()
	data class RemoveFromFavoriteAction(val countryName: String) : CountryDetailAction()
}