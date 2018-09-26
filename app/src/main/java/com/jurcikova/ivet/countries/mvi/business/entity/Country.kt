package com.jurcikova.ivet.countries.mvi.business.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryProperty
import com.jurcikova.ivet.mvi.R
import com.squareup.moshi.Json

@Entity
data class Country(
        @PrimaryKey
        val name: String,
        val capital: String,
        val region: String,
        val subregion: String,
        val population: String,
        val area: String?,
        val currencies: List<Currency>,
        val languages: List<Languages>,
        val flag: String,
        var isFavorite: Boolean = false
) {

    @Ignore
    val currrencyDescription = currencies.map {
        it.currency
    }.joinToString(", ")

    @Ignore
    val languageDescription = languages.map {
        it.name
    }.joinToString(", ")

    @Ignore
    val countryProperties = listOf(
                CountryProperty(R.drawable.ic_capital, capital),
                CountryProperty(R.drawable.ic_location, region, subregion),
                CountryProperty(R.drawable.ic_people, population),
                CountryProperty(R.drawable.ic_landscape, area),
                CountryProperty(R.drawable.ic_currency, currrencyDescription),
                CountryProperty(R.drawable.ic_language, languageDescription)
        )
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