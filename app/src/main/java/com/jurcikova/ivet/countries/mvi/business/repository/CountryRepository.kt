package com.jurcikova.ivet.countries.mvi.business.repository

import com.jurcikova.ivet.countries.mvi.business.api.CountryApi
import com.jurcikova.ivet.countries.mvi.business.db.dao.CountryDao
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.reactive.openSubscription

interface CountryRepository {

    suspend fun getCountry(countryName: String?): ReceiveChannel<Country>

    suspend fun getAllCountries(): ReceiveChannel<List<Country>>

    suspend fun getCountriesByName(searchQuery: String): ReceiveChannel<List<Country>>

    fun CoroutineScope.addToFavorite(countryName: String): Deferred<Unit>

    fun CoroutineScope.removeFromFavorite(countryName: String): Deferred<Unit>
}

class CountryRepositoryImpl(val countryService: CountryApi, val countryDao: CountryDao) : CountryRepository {
    override suspend fun getCountry(countryName: String?): ReceiveChannel<Country> =
            countryDao.getCountry(countryName).openSubscription()

    @Suppress("DeferredResultUnused")
    override suspend fun getAllCountries(): ReceiveChannel<List<Country>> =
            coroutineScope {
                async {
                    getCountriesFromApi()
                }

                return@coroutineScope getCountriesFromDb()
            }

    override suspend fun getCountriesByName(searchQuery: String): ReceiveChannel<List<Country>> = countryDao.getCountriesByName("%$searchQuery%").openSubscription()

    override fun CoroutineScope.addToFavorite(countryName: String) = async {
        countryDao.updateIsFavorite(countryName, true)
    }

    override fun CoroutineScope.removeFromFavorite(countryName: String) = async {
        countryDao.updateIsFavorite(countryName, false)
    }

    private fun getCountriesFromDb(): ReceiveChannel<List<Country>> =
            countryDao.getAll().openSubscription()

    private suspend fun getCountriesFromApi() {
        val countries = countryService.getAllCountries().await()
        logD("Dispatching ${countries.size} countries from API...")
        val updatedItems = (countryDao.getAllSync() + countries).groupBy {
            it
        }.filter {
            it.value.size == 1
        }.keys.toList()

        if (updatedItems.isNotEmpty()) {
            logD("Saving ${updatedItems.size} countries from API to DB...")
            storeCountriesInDb(updatedItems)
        }
    }

    private fun storeCountriesInDb(countries: List<Country>) {
        countryDao.insertFetchedCountries(countries = countries)
    }
}