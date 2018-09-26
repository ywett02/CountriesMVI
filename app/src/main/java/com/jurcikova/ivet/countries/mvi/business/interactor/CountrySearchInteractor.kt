package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult
import com.strv.ktools.logD
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CountrySearchInteractor(val countryRepository: CountryRepository) : MviInteractor<CountrySearchAction, CountrySearchResult> {

    override val actionProcessor =
            ObservableTransformer<CountrySearchAction, CountrySearchResult> { actions ->
                actions.publish { selector ->
                    selector.ofType(CountrySearchAction.LoadCountriesByNameAction::class.java)
                            .compose(loadCountriesByName)
                            .doOnNext { result ->
                                logD("result: $result")
                            }
                            .mergeWith(
                                    // Error for not implemented actions
                                    selector.filter { v ->
                                        v !is CountrySearchAction.LoadCountriesByNameAction
                                    }.flatMap { w ->
                                        Observable.error<CountrySearchResult>(
                                                IllegalArgumentException("Unknown Action type: $w"))
                                    }
                            )
                }
            }

    private val loadCountriesByName =
            ObservableTransformer<CountrySearchAction.LoadCountriesByNameAction, CountrySearchResult> { actions ->
                actions.flatMap { action ->
                    if (action.searchQuery.isBlank()) {
                        Observable.just(CountrySearchResult.LoadCountriesByNameResult.NotStarted)
                    } else {
                        countryRepository.getCountriesByName(action.searchQuery)
                                .map { countries -> CountrySearchResult.LoadCountriesByNameResult.Success(countries) }
                                .cast(CountrySearchResult.LoadCountriesByNameResult::class.java)
                                .onErrorReturn { error ->
                                    //because in case of empty result api returns 404 :(
                                    if (error is HttpException && error.code() == 404) {
                                        CountrySearchResult.LoadCountriesByNameResult.Success(emptyList())
                                    } else CountrySearchResult.LoadCountriesByNameResult.Failure(error)
                                }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .startWith(CountrySearchResult.LoadCountriesByNameResult.InProgress(action.searchQuery))
                    }
                }
            }
}