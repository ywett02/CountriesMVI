package com.jurcikova.ivet.triptodomvi.ui.countryList.search

import com.example.android.architecture.blueprints.todoapp.mvibase.MviViewState
import com.jurcikova.ivet.triptodomvi.business.entity.Country

data class CountrySearchViewState(
        val isLoading: Boolean,
        val searchQuery: String,
        val searchNotStartedYet: Boolean,
        val countries: List<Country>,
        val error: Throwable?) : MviViewState {
    companion object {
        fun idle(): CountrySearchViewState {
            return CountrySearchViewState(
                    isLoading = false,
                    searchQuery = "",
                    searchNotStartedYet = true,
                    countries = emptyList(),
                    error = null
            )
        }
    }
}