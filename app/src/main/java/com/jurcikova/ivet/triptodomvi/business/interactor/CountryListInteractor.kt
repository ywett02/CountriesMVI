package com.jurcikova.ivet.triptodomvi.business.interactor

import com.jurcikova.ivet.triptodomvi.business.repository.CountryRepository
import com.jurcikova.ivet.triptodomvi.mvibase.MviInteractor
import com.jurcikova.ivet.triptodomvi.ui.countryList.all.CountryListAction
import com.jurcikova.ivet.triptodomvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.triptodomvi.ui.countryList.all.CountryListResult
import com.jurcikova.ivet.triptodomvi.ui.countryList.all.CountryListResult.LoadCountriesResult
import com.strv.ktools.inject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CountryListInteractor() : MviInteractor<CountryListAction, CountryListResult> {

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
            ObservableTransformer<CountryListAction, CountryListResult> { actions ->
                actions.publish { selector ->
                    selector.ofType(LoadCountriesAction::class.java).compose(loadTasks)
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

    //todo blbne typovanie
    private val loadTasks =
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
                            //todo why there is a funcion? what does it do?
                            .onErrorReturn(LoadCountriesResult::Failure)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            // Emit an InProgress event to notify the subscribers (e.g. the UI) we are
                            // doing work and waiting on a response.
                            // We emit it after observing on the UI thread to allow the event to be emitted
                            // on the current frame and avoid jank.
                            .startWith(LoadCountriesResult.InProgress)
                }
            }
}