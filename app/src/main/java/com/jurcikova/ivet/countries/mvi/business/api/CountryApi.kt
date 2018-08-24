package com.jurcikova.ivet.countries.mvi.business.api

import com.jurcikova.ivet.countries.mvi.business.entity.Country
import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET
import retrofit2.http.Path

interface CountryApi {

    @GET("all")
    fun getAllCountries(): Deferred<List<Country>>

    @GET("name/{name}")
    fun getCountriesByName(@Path("name") name: String): Deferred<List<Country>>
}