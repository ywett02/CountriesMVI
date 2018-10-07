package com.jurcikova.ivet.countries.mvi.business.db.converter

import androidx.room.TypeConverter
import com.jurcikova.ivet.countries.mvi.business.entity.Currency
import com.jurcikova.ivet.countries.mvi.business.entity.Languages
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class CountryConverters : KoinComponent {

    val moshi by inject<Moshi>()

    @TypeConverter
    fun fromCurrencyJsonToList(value: String?): List<Currency> =
            value?.let {
                val type = Types.newParameterizedType(List::class.java, Currency::class.java)
                val adapter = moshi.adapter<List<Currency>>(type)
                adapter.fromJson(value)
            } ?: emptyList()


    @TypeConverter
    fun fromCurrencyListToJson(list: List<Currency>?): String =
            moshi.adapter(List::class.java).run {
                toJson(list)
            }

    @TypeConverter
    fun fromLanguageJsonToList(value: String?): List<Languages> =
            value?.let {
                val type = Types.newParameterizedType(List::class.java, Languages::class.java)
                val adapter = moshi.adapter<List<Languages>>(type)
                adapter.fromJson(value)
            } ?: emptyList()


    @TypeConverter
    fun fromLanguageListToJson(list: List<Languages>?): String =
            moshi.adapter(List::class.java).run {
                toJson(list)
            }
}

