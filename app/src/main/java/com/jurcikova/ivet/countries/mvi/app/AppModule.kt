package com.jurcikova.ivet.countries.mvi.app

import android.app.Application
import androidx.room.Room
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.jurcikova.ivet.countries.mvi.business.api.CountryApi
import com.jurcikova.ivet.countries.mvi.business.db.CountryDatabase
import com.jurcikova.ivet.countries.mvi.business.interactor.CountryDetailInteractor
import com.jurcikova.ivet.countries.mvi.business.interactor.CountryListInteractor
import com.jurcikova.ivet.countries.mvi.business.interactor.CountrySearchInteractor
import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepositoryImpl
import com.jurcikova.ivet.countries.mvi.common.Config
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryPropertyAdapter
import com.jurcikova.ivet.countries.mvi.ui.countryList.CountryAdapter
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val databaseModule = module {
    single {
        createDb(androidApplication())
    }

    single {
        getCountryDao(get())
    }
}

val apiModule = module {
    single {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    single {
        OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
    }
    single {
        createRetrofit(get(), get())
    }

    single<CountryApi> {
        createCountryService(get())
    }
    single<CountryRepository> {
        CountryRepositoryImpl(get(), get())
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
}

val viewModelModule = module {
    viewModel {
        CountrySearchViewModel(get())
    }
    viewModel {
        CountryListViewModel(get())
    }
    viewModel {
        CountryDetailViewModel(get())
    }
}

val appModule = module {
    factory {
        CountryAdapter()
    }
    factory {
        CountryPropertyAdapter()
    }
}

private fun createDb(context: Application) =
        Room.databaseBuilder(context, CountryDatabase::class.java, Config.databaseName).build()

private fun getCountryDao(database: CountryDatabase) =
        database.countryDao()

private fun createRetrofit(moshi: Moshi, okHttpClient: OkHttpClient) =
        Retrofit.Builder()
                .baseUrl(Config.baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

private fun createCountryService(retrofit: Retrofit) =
        retrofit.create(CountryApi::class.java)



