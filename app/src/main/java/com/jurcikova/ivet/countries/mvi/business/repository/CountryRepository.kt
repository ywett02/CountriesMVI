package com.jurcikova.ivet.countries.mvi.business.repository

import com.jurcikova.ivet.countries.mvi.business.api.CountryApi
import com.jurcikova.ivet.countries.mvi.business.db.dao.CountryDao
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.strv.ktools.logD
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface CountryRepository {

	fun loadCountries(): Completable

	fun getCountries(): Observable<List<Country>>

	fun addToFavorite(countryName: String)

	fun removeFromFavorite(countryName: String)

	fun getCountry(countryName: String?): Single<Country>

	fun getCountriesByName(searchQuery: String): Single<List<Country>>
}

class CountryRepositoryImpl(private val countryService: CountryApi, private val countryDao: CountryDao) : CountryRepository {

	override fun loadCountries(): Completable =
		countryService.getAllCountries()
			.doOnSuccess { countries ->
				logD("Dispatching ${countries.size} countries from API...")
				storeCountriesInDb(countries)
			}.ignoreElement()

	override fun getCountries(): Observable<List<Country>> =
		countryDao.getAll()
			.doOnNext { logD("Dispatching ${it.size} from DB...") }
			.toObservable()

	override fun addToFavorite(countryName: String) {
		countryDao.updateIsFavorite(countryName, true)
	}

	override fun removeFromFavorite(countryName: String) {
		countryDao.updateIsFavorite(countryName, false)
	}

	override fun getCountry(countryName: String?): Single<Country> =
		countryDao.getCountry(countryName)
			.doOnSuccess { logD("Dispatching ${it.name} from DB...") }

	override fun getCountriesByName(searchQuery: String): Single<List<Country>> =
		countryDao.getCountriesByName("%$searchQuery%")
			.doOnSuccess { logD("Dispatching ${it.size} from DB...") }

	private fun storeCountriesInDb(countries: List<Country>) {
		logD("Saving ${countries.size} countries from API to DB...")
		countryDao.insertFetchedCountries(countries = countries)
	}
}

