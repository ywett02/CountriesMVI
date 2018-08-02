package com.jurcikova.ivet.triptodomvi.business.entity

data class Country(
        val name: String,
        val capital: String,
        val region: String,
        val flag: String,
        val currencies: List<Currency>
)

data class Currency(
        val code: String?,
        val name: String?,
        val symbol: String?
)