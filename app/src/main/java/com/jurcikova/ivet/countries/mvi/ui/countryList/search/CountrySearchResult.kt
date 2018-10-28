package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.mvibase.MviResult

sealed class CountrySearchResult : MviResult {
	sealed class LoadCountriesByNameResult : CountrySearchResult() {
		object NotStarted : LoadCountriesByNameResult()
		data class Success(val countries: List<Country>) : LoadCountriesByNameResult()
		data class Failure(val error: Throwable) : LoadCountriesByNameResult()
		data class InProgress(val searchQuery: String) : LoadCountriesByNameResult()
	}

	sealed class AddToFavoriteResult : CountrySearchResult() {
		object Success : AddToFavoriteResult()
		data class Failure(val error: Throwable) : AddToFavoriteResult()
		object InProgress : AddToFavoriteResult()
		object Reset : AddToFavoriteResult()
	}

	sealed class RemoveFromFavoriteResult : CountrySearchResult() {
		object Success : RemoveFromFavoriteResult()
		data class Failure(val error: Throwable) : RemoveFromFavoriteResult()
		object InProgress : RemoveFromFavoriteResult()
		object Reset : RemoveFromFavoriteResult()
	}
}