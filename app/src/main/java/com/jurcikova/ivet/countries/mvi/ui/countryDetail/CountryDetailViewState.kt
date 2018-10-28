package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.mvibase.MviViewState

data class CountryDetailViewState(
	val isLoading: Boolean,
	val country: Country?,
	val error: Throwable?,
	val message: MessageType?,
	val initial: Boolean
) : MviViewState {
	companion object {
		fun idle(): CountryDetailViewState {
			return CountryDetailViewState(
				isLoading = false,
				country = null,
				error = null,
				message = null,
				initial = true
			)
		}
	}
}