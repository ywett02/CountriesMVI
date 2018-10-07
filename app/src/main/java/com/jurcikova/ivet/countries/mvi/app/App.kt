package com.jurcikova.ivet.countries.mvi.app

import android.app.Application
import com.strv.ktools.setLogTag
import org.koin.android.ext.android.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        setLogTag("CountriesMVI")
        startKoin(this, listOf(appModule, databaseModule, apiModule, viewModelModule))
    }
}