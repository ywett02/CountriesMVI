package com.jurcikova.ivet.triptodomvi.business.repository

import com.jurcikova.ivet.triptodomvi.business.api.CountryApi
import com.jurcikova.ivet.triptodomvi.business.entity.Country
import com.strv.ktools.inject
import io.reactivex.Single

interface CountryRepository {

    fun getAllCountries(): Single<List<Country>>

    fun getCountriesByName(searchQuery: String): Single<List<Country>>
}

class CountryRepositoryImpl() : CountryRepository {

    private val countryService by inject<CountryApi>()

    override fun getAllCountries() = countryService.getAllCountries()

    override fun getCountriesByName(searchQuery: String): Single<List<Country>> = countryService.getCountriesByName(searchQuery)
}