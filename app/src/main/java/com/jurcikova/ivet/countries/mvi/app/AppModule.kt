package com.jurcikova.ivet.countries.mvi.app

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.jurcikova.ivet.countries.mvi.business.api.CountryApi
import com.jurcikova.ivet.countries.mvi.business.interactor.CountryDetailInteractor
import com.jurcikova.ivet.countries.mvi.business.interactor.CountryListInteractor
import com.jurcikova.ivet.countries.mvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepositoryImpl
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.CountryAdapter
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val appModule = module {
    single<CountryApi> {
        retrofit.create(CountryApi::class.java)
    }
    single<CountryRepository> {
        CountryRepositoryImpl(get())
    }
    single {
        CountryListInteractor(get())
    }
    single {
        CountrySearchInteractor(get())
    }
    single {
        CountryDetailInteractor(get())
    }
    viewModel {
        CountrySearchViewModel(get())
    }
    viewModel {
        CountryListViewModel(get())
    }
    viewModel {
        CountryDetailViewModel(get())
    }
    factory {
        CountryAdapter()
    }
}

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
