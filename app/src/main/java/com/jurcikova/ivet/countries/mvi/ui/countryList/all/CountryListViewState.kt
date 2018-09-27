package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import com.jurcikova.ivet.countries.mvi.mvibase.MviViewState
import com.jurcikova.ivet.countries.mvi.business.entity.Country

data class CountryListViewState(val isLoading: Boolean,
                                val countries: List<Country>,
                                val filterType: FilterType,
                                val error: Throwable?,
                                val message: MessageType?) : MviViewState {
    companion object {
        fun idle(): CountryListViewState {
            return CountryListViewState(
                    isLoading = false,
                    countries = emptyList(),
                    filterType = FilterType.All,
                    error = null,
                    message = null
            )
        }
    }

    sealed class MessageType() {
        object AddToFavorite: MessageType()
        object RemoveFromFavorite: MessageType()
    }
}