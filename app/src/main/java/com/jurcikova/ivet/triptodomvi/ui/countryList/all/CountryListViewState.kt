package com.jurcikova.ivet.triptodomvi.ui.countryList.all

import com.example.android.architecture.blueprints.todoapp.mvibase.MviViewState
import com.jurcikova.ivet.triptodomvi.business.entity.Country

data class CountryListViewState(val isLoading: Boolean,
                                val isRefreshing: Boolean,
                                val countries: List<Country>,
                                val error: Throwable?) : MviViewState {
    companion object {
        fun idle(): CountryListViewState {
            return CountryListViewState(
                    isLoading = false,
                    isRefreshing = false,
                    countries = emptyList(),
                    error = null
            )
        }
    }
}