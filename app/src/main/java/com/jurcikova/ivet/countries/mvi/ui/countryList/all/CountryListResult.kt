package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.mvibase.MviResult

sealed class CountryListResult : MviResult {
	sealed class LoadCountriesResult : CountryListResult() {
		data class Success(val filterType: FilterType) : LoadCountriesResult()
		data class Failure(val error: Throwable) : LoadCountriesResult()
		data class InProgress(val isRefreshing: Boolean) : LoadCountriesResult()
	}

	sealed class UpdateCountryListResult : CountryListResult() {
		data class Success(val countries: List<Country>) : UpdateCountryListResult()
		data class Failure(val error: Throwable) : UpdateCountryListResult()
	}

	sealed class AddToFavoriteResult : CountryListResult() {
		object Success : AddToFavoriteResult()
		data class Failure(val error: Throwable) : AddToFavoriteResult()
		object InProgress : AddToFavoriteResult()
		object Reset : AddToFavoriteResult()
	}

	sealed class RemoveFromFavoriteResult : CountryListResult() {
		object Success : RemoveFromFavoriteResult()
		data class Failure(val error: Throwable) : RemoveFromFavoriteResult()
		object InProgress : RemoveFromFavoriteResult()
		object Reset : RemoveFromFavoriteResult()
	}
}