package com.jurcikova.ivet.countries.mvi.app

import android.app.Application
import com.strv.ktools.setLogTag
import com.strv.ktools.setupModule

class App : Application() {
    val module by lazy { AppModule(this) }

    override fun onCreate() {
        super.onCreate()

        setLogTag("CountriesMVI")
        setupModule(module)
    }
}