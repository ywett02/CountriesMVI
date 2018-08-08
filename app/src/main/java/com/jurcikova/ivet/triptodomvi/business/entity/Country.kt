package com.jurcikova.ivet.triptodomvi.business.entity

import com.squareup.moshi.Json

data class Country(
        val name: String,
        val capital: String,
        val region: String,
        val subregion: String,
        val population: String,
        val area: String?,
        val currencies: List<Currency>,
        val languages: List<Languages>,
        val flag: String
) {
    val currrencyDescription = currencies.map {
        it.currency
    }.joinToString(", ")

    val languageDescription = languages.map {
        it.name
    }.joinToString(", ")
}

data class Currency(
        val code: String?,
        val name: String?,
        val symbol: String?
) {
    val currency = "$name ($code)"
}

data class Languages(
        @Json(name = "iso639_1") val iso6391: String?,
        @Json(name = "iso639_2") val iso6392: String,
        val name: String,
        val nativeName: String
)