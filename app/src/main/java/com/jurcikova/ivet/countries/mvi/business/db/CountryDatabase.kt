package com.jurcikova.ivet.countries.mvi.business.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jurcikova.ivet.countries.mvi.business.db.converter.CountryConverters
import com.jurcikova.ivet.countries.mvi.business.db.dao.CountryDao
import com.jurcikova.ivet.countries.mvi.business.entity.Country

@Database(entities = [Country::class], version = 1)
@TypeConverters(CountryConverters::class)
abstract class CountryDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
}