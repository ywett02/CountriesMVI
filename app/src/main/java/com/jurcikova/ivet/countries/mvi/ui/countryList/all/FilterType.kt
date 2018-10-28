package com.jurcikova.ivet.countries.mvi.ui.countryList.all

sealed class FilterType {
	object All : FilterType()
	object Favorite : FilterType()
}