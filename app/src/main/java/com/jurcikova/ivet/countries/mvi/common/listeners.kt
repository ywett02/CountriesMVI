package com.jurcikova.ivet.countries.mvi.common

import com.jurcikova.ivet.countries.mvi.business.entity.Country

interface OnCountryClickListener {
    fun onCountryClick(country: Country)
    fun onFavoriteClick(country: Country)
}