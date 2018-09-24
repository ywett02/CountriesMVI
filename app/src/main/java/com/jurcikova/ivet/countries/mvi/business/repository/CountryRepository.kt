package com.jurcikova.ivet.countries.mvi.business.repository

import com.jurcikova.ivet.countries.mvi.business.api.CountryApi
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import io.reactivex.Single

interface CountryRepository {

    fun getCountry(countryName: String): Single<Country>

    fun getAllCountries(): Single<List<Country>>

    fun getCountriesByName(searchQuery: String): Single<List<Country>>

}

class CountryRepositoryImpl(val countryService: CountryApi) : CountryRepository {

    override fun getCountry(countryName: String): Single<Country> = countryService.getCountriesByName(countryName).map {
        it.first()
    }

    override fun getAllCountries() = countryService.getAllCountries()

    override fun getCountriesByName(searchQuery: String): Single<List<Country>> = countryService.getCountriesByName(searchQuery)
}