package com.jurcikova.ivet.triptodomvi.ui.myCountries

import android.support.annotation.StringRes
import com.example.android.architecture.blueprints.todoapp.mvibase.MviViewState
import com.jurcikova.ivet.triptodomvi.R
import com.jurcikova.ivet.triptodomvi.business.entity.MyCountry

data class MyCountryListViewState(
        val isLoading: Boolean,
        val isRefreshing: Boolean,
        val countries: List<MyCountry>,
        val error: Throwable?,
        val countryStateChange: CountryState?
) : MviViewState {

    sealed class CountryState(@StringRes val stringRes: Int) {
        object Visited : CountryState(R.string.country_state_visited)
        object UnVisited : CountryState(R.string.country_state_unvisited)
    }

    companion object {
        fun idle(): MyCountryListViewState {
            return MyCountryListViewState(
                    isLoading = false,
                    isRefreshing = false,
                    countries = emptyList(),
                    error = null,
                    countryStateChange = null
            )
        }
    }
}