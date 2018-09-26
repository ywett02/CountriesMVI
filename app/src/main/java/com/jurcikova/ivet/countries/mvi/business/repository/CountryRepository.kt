package com.jurcikova.ivet.countries.mvi.business.repository

import com.jurcikova.ivet.countries.mvi.business.api.CountryApi
import com.jurcikova.ivet.countries.mvi.business.db.dao.CountryDao
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.FilterType
import com.strv.ktools.logD
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

interface CountryRepository {

    fun getCountry(countryName: String): Observable<Country>

    fun getCountries(filterType: FilterType): Observable<List<Country>>

    fun getFavoriteCountries(): Single<List<Country>>

    fun getCountriesByName(searchQuery: String): Observable<List<Country>>

    fun addToFavorite(countryName: String)

    fun removeFromFavorite(countryName: String)
}

class CountryRepositoryImpl(val countryService: CountryApi, val countryDao: CountryDao) : CountryRepository {
    override fun getFavoriteCountries(): Single<List<Country>> =
            countryDao.getFavorite()


    override fun addToFavorite(countryName: String) {
        countryDao.updateIsFavorite(countryName, true)
    }

    override fun removeFromFavorite(countryName: String) {
        countryDao.updateIsFavorite(countryName, false)
    }


    override fun getCountry(countryName: String): Observable<Country> =
            Observable.concatArrayEager(
                    getCountryFromDb(countryName),
                    getCountryFromApi(countryName)
            )
                    .debounce(400, TimeUnit.MILLISECONDS)

    override fun getCountries(filterType: FilterType): Observable<List<Country>> =
            when (filterType) {
                is FilterType.All -> getAllCountries()
                is FilterType.Favorite -> getFavoriteCountries().toObservable()
            }

    override fun getCountriesByName(searchQuery: String): Observable<List<Country>> =
            Observable.concatArrayEager(
                    getCountriesFromDb(searchQuery),
                    getCountriesFromApi(searchQuery)
            )
                    .debounce(400, TimeUnit.MILLISECONDS)

    private fun getAllCountries(): Observable<List<Country>> =
            Observable.concatArrayEager(
                    getCountriesFromDb(),
                    getCountriesFromApi()
            )
                    .debounce(400, TimeUnit.MILLISECONDS)


    private fun getCountryFromDb(name: String) =
            countryDao.getCountry(name)
                    .toObservable()
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
                    .toObservable()
                    .doOnNext { logD("Dispatching ${it.size} from DB...") }

    private fun getCountriesFromApi() =
            countryService.getAllCountries()
                    .toObservable()
                    .doOnNext {
                        logD("Dispatching ${it.size} countries from API...")
                        storeCountriesInDb(it)
                    }

    private fun getCountriesFromDb(name: String) =
            countryDao.getCountries("%$name%")
                    .toObservable()
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

    private fun updateCountry(country: Country) {
        countryDao.updateCountry(country)
    }
}

