package com.jurcikova.ivet.triptodomvi.business.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class MyCountry(
        @PrimaryKey val name: String,
        val flag: String,
        var visited: Boolean
)

data class Country(
        @PrimaryKey val name: String,
        val capital: String,
        val region: String,
        val flag: String
)

data class Currency(
        val code: String?,
        val name: String?,
        val symbol: String?
)