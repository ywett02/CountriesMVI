package com.jurcikova.ivet.triptodomvi.business.repository

import com.jurcikova.ivet.triptodomvi.business.api.CountryApi
import com.jurcikova.ivet.triptodomvi.business.db.dao.CountryDao
import com.jurcikova.ivet.triptodomvi.business.entity.Country
import com.jurcikova.ivet.triptodomvi.business.entity.MyCountry
import com.strv.ktools.inject
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface CountryRepository {

    fun updateCountry(myCountry: MyCountry): Completable

    fun getCountry(countryName: String): Single<MyCountry>

    fun getAllCountries(): Single<List<Country>>

    fun getCountriesByName(searchQuery: String): Single<List<Country>>

    fun getMyCountries(): Flowable<List<MyCountry>>
}

class CountryRepositoryImpl() : CountryRepository {

    private val countryService by inject<CountryApi>()
    private val countryDao by inject<CountryDao>()

    override fun updateCountry(myCountry: MyCountry): Completable = Completable.fromAction {
        countryDao.updateCountry(myCountry)
    }

    override fun getCountry(countryName: String): Single<MyCountry> = countryDao.getCountry(countryName)

    override fun getAllCountries() = countryService.getAllCountries()

    override fun getCountriesByName(searchQuery: String): Single<List<Country>> = countryService.getCountriesByName(searchQuery)

    override fun getMyCountries(): Flowable<List<MyCountry>> = countryDao.findAllCountries()
}