package com.jurcikova.ivet.triptodomvi.business.interactor

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.jurcikova.ivet.triptodomvi.business.repository.CountryRepository
import com.jurcikova.ivet.triptodomvi.mvibase.MviInteractor
import com.jurcikova.ivet.triptodomvi.ui.countryList.search.CountrySearchAction
import com.jurcikova.ivet.triptodomvi.ui.countryList.search.CountrySearchResult
import com.strv.ktools.inject
import com.strv.ktools.logD
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CountrySearchInteractor : MviInteractor<CountrySearchAction, CountrySearchResult> {

    private val countryRepository by inject<CountryRepository>()

    /**
     * Splits the [Observable] to match each type of [MviAction] to
     * its corresponding business logic. Each takes a defined [MviAction],
     * returns a defined [MviResult]
     * The global actionProcessor then merges all [Observable] back to
     * one unique [Observable].
     *
     *
     * The splitting is done using [Observable.publish] which allows almost anything
     * on the passed [Observable] as long as one and only one [Observable] is returned.
     *
     *
     * An security layer is also added for unhandled [MviAction] to allow early crash
     * at runtime to easy the maintenance.
     */
    override val actionProcessor =
            ObservableTransformer<CountrySearchAction, CountrySearchResult> { actions ->
                actions.publish { selector ->
                    selector.ofType(CountrySearchAction.LoadCountriesByNameAction::class.java)
                            .compose(loadTasksByName)
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

    private val loadTasksByName =
            ObservableTransformer<CountrySearchAction.LoadCountriesByNameAction, CountrySearchResult> { actions ->
                actions.flatMap { action ->
                    if (action.searchQuery.isBlank()) {
                        Observable.just(CountrySearchResult.LoadCountriesByNameResult.NotStarted)
                    } else {
                        countryRepository.getCountriesByName(action.searchQuery)
                                .toObservable()
                                .map { countries -> CountrySearchResult.LoadCountriesByNameResult.Success(countries) }
                                .cast(CountrySearchResult.LoadCountriesByNameResult::class.java)
                                .onErrorReturn { error ->
                                    //because in case of empty result api returns 404 :(
                                    if (error is HttpException && error.code() == 404) {
                                        CountrySearchResult.LoadCountriesByNameResult.EmptyResult
                                    } else CountrySearchResult.LoadCountriesByNameResult.Failure(error)
                                }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .startWith(CountrySearchResult.LoadCountriesByNameResult.InProgress(action.searchQuery))
                    }
                }
            }
}