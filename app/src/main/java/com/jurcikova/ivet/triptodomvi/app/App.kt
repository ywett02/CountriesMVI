package com.jurcikova.ivet.triptodomvi.app

import android.app.Application
import com.strv.ktools.setLogTag
import com.strv.ktools.setupModule

class App : Application() {
    val module by lazy { AppModule(this) }

    override fun onCreate() {
        super.onCreate()

        setLogTag("Uncommon")
        setupModule(module)
    }
}