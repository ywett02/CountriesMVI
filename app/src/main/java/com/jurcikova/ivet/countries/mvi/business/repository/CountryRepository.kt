package com.jurcikova.ivet.countries.mvi.business.repository

import com.jurcikova.ivet.countries.mvi.business.api.CountryApi
import com.jurcikova.ivet.countries.mvi.business.db.dao.CountryDao
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.strv.ktools.logD
import io.reactivex.Flowable

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

    override fun getCountry(countryName: String): Flowable<Country> {
        getCountryFromApi(countryName)
        return getCountryFromDb(countryName)
    }

    override fun getCountriesByName(searchQuery: String): Flowable<List<Country>> {
        getCountriesFromApi(searchQuery)
        return getCountriesByNameFromDb(searchQuery)
    }

    override fun getAllCountries(): Flowable<List<Country>> {
        getCountriesFromApi()
        return getCountriesFromDb()
    }

    private fun getCountryFromDb(name: String) =
            countryDao.getCountry(name)
                    .doOnNext { logD("Dispatching ${it.name} from DB...") }

    private fun getCountryFromApi(name: String) =
            countryService.getCountriesByName(name)
                    .map {
                        it.first()
                    }
                    .toObservable()
                    .doOnNext {
                        logD("Dispatching ${it.name} country from API...")
                        storeCountryInDb(it)
                    }

    private fun getCountriesFromDb() =
            countryDao.getAll()
                    .doOnNext { logD("Dispatching ${it.size} from DB...") }

    private fun getCountriesFromApi() =
            countryService.getAllCountries()
                    .toObservable()
                    .doOnNext {
                        logD("Dispatching ${it.size} countries from API...")
                        storeCountriesInDb(it)
                    }

    private fun getCountriesByNameFromDb(name: String) =
            countryDao.getCountriesByName("%$name%")
                    .doOnNext { logD("Dispatching ${it.size} from DB...") }

    private fun getCountriesFromApi(name: String) =
            countryService.getCountriesByName(name)
                    .toObservable()
                    .doOnNext {
                        logD("Dispatching ${it.size} countries from API...")
                        storeCountriesInDb(it)
                    }

    private fun storeCountryInDb(country: Country) {
        storeCountriesInDb(listOf(country))
    }

    private fun storeCountriesInDb(countries: List<Country>) {
        countryDao.insertFetchedCountries(countries = countries)
    }
}

