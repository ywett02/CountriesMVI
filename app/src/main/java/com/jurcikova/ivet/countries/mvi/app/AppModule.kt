package com.jurcikova.ivet.countries.mvi.app

import android.app.Application
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.jurcikova.ivet.countries.mvi.business.api.CountryApi
import com.jurcikova.ivet.countries.mvi.business.interactor.CountryDetailInteractor
import com.jurcikova.ivet.countries.mvi.business.interactor.CountryListInteractor
import com.jurcikova.ivet.countries.mvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepositoryImpl
import com.jurcikova.ivet.countries.mvi.ui.countryList.CountryAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.strv.ktools.DIModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class AppModule(private val application: Application) : DIModule() {

    private val moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
                .baseUrl("https://restcountries.eu/rest/v2/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    override fun onProvide() {
        provideSingleton { application }

        onProvideApi()
        onProvideRepo()
        onProvideAdapters()
        onProvideInteractors()
    }

    private fun onProvideInteractors() {
        provideSingleton { CountryListInteractor() }
        provideSingleton { CountrySearchInteractor() }
        provideSingleton { CountryDetailInteractor() }
    }

    private fun onProvideAdapters() {
        provide {
            CountryAdapter()
        }
    }

    private fun onProvideRepo() {
        provideSingleton<CountryRepository> { CountryRepositoryImpl() }
    }

    private fun onProvideApi() {
        provideSingleton<CountryApi> { retrofit.create(CountryApi::class.java) }
    }
}




