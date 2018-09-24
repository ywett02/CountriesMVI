package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.LoadCountriesResult
import com.strv.ktools.logD
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CountryListInteractor(val countryRepository: CountryRepository) : MviInteractor<CountryListAction, CountryListResult> {

    override val actionProcessor =
            ObservableTransformer<CountryListAction, CountryListResult> { actions ->
                actions.publish { selector ->
                    selector.ofType(LoadCountriesAction::class.java).compose(loadCountries)
                            .doOnNext { result ->
                                logD("result: $result")
                            }
                            .mergeWith(
                                    // Error for not implemented actions
                                    selector.filter { v ->
                                        v !is LoadCountriesAction
                                    }.flatMap { w ->
                                        Observable.error<CountryListResult>(
                                                IllegalArgumentException("Unknown Action type: $w"))
                                    }
                            )
                }
            }

    private val loadCountries =
            ObservableTransformer<LoadCountriesAction, CountryListResult> { actions ->
                actions.flatMap { action ->
                    countryRepository.getAllCountries()
                            // Transform the Single to an Observable to allow emission of multiple
                            // events down the stream (e.g. the InFlight event)
                            .toObservable()
                            // Wrap returned data into an immutable object
                            .map { countries -> LoadCountriesResult.Success(countries) }
                            .cast(LoadCountriesResult::class.java)
                            // Wrap any error into an immutable object and pass it down the stream
                            // without crashing.
                            // Because errors are data and hence, should just be part of the stream.
                            .onErrorReturn { LoadCountriesResult.Failure(it) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            // Emit an InProgress event to notify the subscribers (e.g. the UI) we are
                            // doing work and waiting on a response.
                            // We emit it after observing on the UI thread to allow the event to be emitted
                            // on the current frame and avoid jank.
                            .startWith(LoadCountriesResult.InProgress(action.isRefreshing))
                }
            }
}