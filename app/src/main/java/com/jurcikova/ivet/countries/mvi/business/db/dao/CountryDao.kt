package com.jurcikova.ivet.countries.mvi.business.db.dao

import androidx.room.*
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import io.reactivex.Flowable

@Dao
interface CountryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCountry(country: Country)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCountries(countries: List<Country>)

    @Update
    fun updateCountry(country: Country)

    @Query("SELECT * FROM country WHERE name = :name")
    fun getCountry(name: String?): Flowable<Country>

    @Query("SELECT * FROM country WHERE name = :name")
    fun getCountrySync(name: String): Country?

    @Query("SELECT * FROM country order by name")
    fun getAll(): Flowable<List<Country>>

    @Query("SELECT * FROM country order by name")
    fun getAllSync(): List<Country>

    @Query("SELECT * FROM country WHERE name like :name order by name")
    fun getCountriesByName(name: String): Flowable<List<Country>>

    @Transaction
    fun updateIsFavorite(countryName: String, isFavorite: Boolean) {
        val country = getCountrySync(countryName)
        country?.let {
            it.isFavorite = isFavorite
            updateCountry(country)
        }
    }

    @Transaction
    fun insertFetchedCountries(countries: List<Country>) {
        countries.forEach {
            val storedCountry = getCountrySync(it.name)
            it.isFavorite = storedCountry?.isFavorite ?: false
        }
        insertCountries(countries)
    }
}