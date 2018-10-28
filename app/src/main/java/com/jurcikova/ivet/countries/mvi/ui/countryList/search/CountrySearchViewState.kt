package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.mvibase.MviViewState

data class CountrySearchViewState(
	val isLoading: Boolean,
	val searchQuery: String,
	val searchNotStartedYet: Boolean,
	val countries: List<Country>,
	val error: Throwable?,
	val message: MessageType?
) : MviViewState {
	companion object {
		fun idle(): CountrySearchViewState {
			return CountrySearchViewState(
				isLoading = false,
				searchQuery = "",
				searchNotStartedYet = true,
				countries = emptyList(),
				error = null,
				message = null
			)
		}
	}
}