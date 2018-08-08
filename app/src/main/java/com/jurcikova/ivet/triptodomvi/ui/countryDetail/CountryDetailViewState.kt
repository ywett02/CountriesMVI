package com.jurcikova.ivet.triptodomvi.ui.countryDetail

import com.example.android.architecture.blueprints.todoapp.mvibase.MviViewState
import com.jurcikova.ivet.triptodomvi.business.entity.Country

data class CountryDetailViewState(
        val isLoading: Boolean,
        val country: Country?,
        val error: Throwable?,
        val isFavorite: Boolean,
        val showMessage: Boolean
) : MviViewState {
    companion object {
        fun idle(): CountryDetailViewState {
            return CountryDetailViewState(
                    isLoading = false,
                    country = null,
                    error = null,
                    isFavorite = false,
                    showMessage = false
            )
        }
    }
}