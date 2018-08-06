package com.jurcikova.ivet.triptodomvi.business.db.dao

import android.arch.persistence.room.*
import com.jurcikova.ivet.triptodomvi.business.entity.MyCountry
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface CountryDao  {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCountry(myCountry: MyCountry)

    @Update
    fun updateCountry(myCountry: MyCountry)

    @Delete
    fun deleteCountry(myCountry: MyCountry)

    @Query("SELECT * FROM mycountry where name = :name")
    fun getCountry(name: String): Single<MyCountry>

    @Query("SELECT * FROM mycountry")
    fun findAllCountries(): Flowable<List<MyCountry>>
}