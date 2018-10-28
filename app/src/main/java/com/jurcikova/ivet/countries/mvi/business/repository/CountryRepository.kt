package com.jurcikova.ivet.countries.mvi.business.repository

import com.jurcikova.ivet.countries.mvi.business.api.CountryApi
import com.jurcikova.ivet.countries.mvi.business.entity.Country

interface CountryRepository {

	suspend fun getCountry(countryName: String?): Country

	suspend fun getAllCountries(): List<Country>

	suspend fun getCountriesByName(searchQuery: String): List<Country>
}

class CountryRepositoryImpl(val countryService: CountryApi) : CountryRepository {

	override suspend fun getCountry(countryName: String?): Country = countryService.getCountriesByName(countryName).await().first()

	override suspend fun getAllCountries(): List<Country> =
		countryService.getAllCountries().await()

	override suspend fun getCountriesByName(searchQuery: String): List<Country> = countryService.getCountriesByName(searchQuery).await()
}