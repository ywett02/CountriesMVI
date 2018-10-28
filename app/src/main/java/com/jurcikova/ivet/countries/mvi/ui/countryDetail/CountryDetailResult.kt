package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.mvibase.MviResult

sealed class CountryDetailResult : MviResult {

	sealed class LoadCountryDetailResult : CountryDetailResult() {
		data class Success(val country: Country) : LoadCountryDetailResult()
		data class Failure(val error: Throwable) : LoadCountryDetailResult()
		object InProgress : LoadCountryDetailResult()
	}

	sealed class AddToFavoriteResult : CountryDetailResult() {
		object Success : AddToFavoriteResult()
		data class Failure(val error: Throwable) : AddToFavoriteResult()
		object InProgress : AddToFavoriteResult()
		object Reset : AddToFavoriteResult()
	}

	sealed class RemoveFromFavoriteResult : CountryDetailResult() {
		object Success : RemoveFromFavoriteResult()
		data class Failure(val error: Throwable) : RemoveFromFavoriteResult()
		object InProgress : RemoveFromFavoriteResult()
		object Reset : RemoveFromFavoriteResult()
	}
}