package com.jurcikova.ivet.triptodomvi.business.api

import com.jurcikova.ivet.triptodomvi.business.entity.Country
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface CountryApi {

    @GET("all")
    fun getAllCountries(): Single<List<Country>>

    @GET("name/{name}")
    fun getCountriesByName(@Path("name") name: String): Single<List<Country>>
}