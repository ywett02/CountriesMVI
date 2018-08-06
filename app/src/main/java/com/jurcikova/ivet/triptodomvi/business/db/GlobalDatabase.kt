package com.jurcikova.ivet.triptodomvi.business.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.jurcikova.ivet.triptodomvi.business.db.dao.CountryDao
import com.jurcikova.ivet.triptodomvi.business.entity.MyCountry

@Database(entities = [(MyCountry::class)], version = 1)
abstract class GlobalDatabase: RoomDatabase() {
    abstract fun countryDao(): CountryDao
}