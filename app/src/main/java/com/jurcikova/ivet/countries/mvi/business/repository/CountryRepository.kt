package com.jurcikova.ivet.countries.mvi.business.repository

import com.jurcikova.ivet.countries.mvi.business.api.CountryApi
import com.jurcikova.ivet.countries.mvi.business.db.dao.CountryDao
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.strv.ktools.logD
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable

interface CountryRepository {

    fun getCountry(countryName: String): Flowable<Country>

    fun getAllCountries(): Flowable<List<Country>>

    fun getCountriesByName(searchQuery: String): Flowable<List<Country>>

    fun addToFavorite(countryName: String)

    fun removeFromFavorite(countryName: String)
}

class CountryRepositoryImpl(private val countryService: CountryApi, private val countryDao: CountryDao) : CountryRepository {

    override fun addToFavorite(countryName: String) {
        countryDao.updateIsFavorite(countryName, true)
    }

    override fun removeFromFavorite(countryName: String) {
        countryDao.updateIsFavorite(countryName, false)
    }

    override fun getCountry(countryName: String): Flowable<Country> =
            getCountryFromDb(countryName)

    override fun getCountriesByName(searchQuery: String): Flowable<List<Country>> =
            getCountriesByNameFromDb(searchQuery)

    override fun getAllCountries(): Flowable<List<Country>>  {
        return Observable.concatArrayEager(
                getCountriesFromDb(),
                getCountriesFromApi())
                .toFlowable(BackpressureStrategy.BUFFER)
    }

    private fun getCountriesFromDb() =
            countryDao.getAll()
                    .toObservable()
                    .doOnNext { logD("Dispatching ${it.size} from DB...") }

    private fun getCountryFromDb(name: String) =
            countryDao.getCountry(name)
                    .doOnNext { logD("Dispatching ${it.name} from DB...") }

    private fun getCountriesByNameFromDb(name: String) =
            countryDao.getCountriesByName("%$name%")
                    .doOnNext { logD("Dispatching ${it.size} from DB...") }

    private fun getCountriesFromApi() =
            countryService.getAllCountries()
                    .toObservable()
                    .doOnNext { countries ->
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

