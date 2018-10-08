package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import com.jurcikova.ivet.countries.mvi.mvibase.MviViewState
import com.jurcikova.ivet.countries.mvi.business.entity.Country

data class CountryDetailViewState(
        val isLoading: Boolean,
        val country: Country?,
        val error: Throwable?,
        val isFavorite: Boolean,
        val showMessage: Boolean,
        val initialIntentProcessed: Boolean
) : MviViewState {
    companion object {
        fun idle(): CountryDetailViewState {
            return CountryDetailViewState(
                    isLoading = false,
                    country = null,
                    error = null,
                    isFavorite = false,
                    showMessage = false,
                    initialIntentProcessed = false
            )
        }
    }

    fun copyState(isLoading: Boolean = this.isLoading,
                  country: Country? = this.country,
                  error: Throwable? = this.error,
                  isFavorite: Boolean = this.isFavorite,
                  showMessage: Boolean = this.showMessage) =
            CountryDetailViewState(isLoading, country, error, isFavorite, showMessage, true)
}