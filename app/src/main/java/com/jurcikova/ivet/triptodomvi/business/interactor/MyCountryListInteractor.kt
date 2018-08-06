package com.jurcikova.ivet.triptodomvi.business.interactor

import com.jurcikova.ivet.triptodomvi.business.repository.CountryRepository
import com.jurcikova.ivet.triptodomvi.common.pairWithDelay
import com.jurcikova.ivet.triptodomvi.mvibase.MviInteractor
import com.jurcikova.ivet.triptodomvi.ui.myCountries.MyCountryListAction
import com.jurcikova.ivet.triptodomvi.ui.myCountries.MyCountryListAction.ChangeStateOfCountryAction
import com.jurcikova.ivet.triptodomvi.ui.myCountries.MyCountryListResult
import com.jurcikova.ivet.triptodomvi.ui.myCountries.MyCountryListResult.LoadCountriesResult
import com.jurcikova.ivet.triptodomvi.ui.myCountries.MyCountryListResult.UpdateCountryResult
import com.strv.ktools.inject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MyCountryListInteractor : MviInteractor<MyCountryListAction, MyCountryListResult> {

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
    override var actionProcessor =
            ObservableTransformer<MyCountryListAction, MyCountryListResult> { actions ->
                actions.publish { selected ->
                    Observable.merge<MyCountryListResult>(
                            selected.ofType(MyCountryListAction.LoadMyCountriesAction::class.java)
                                    .compose(loadTasks),
                            selected.ofType(ChangeStateOfCountryAction::class.java)
                                    .compose(updateTask))
                            .mergeWith(
                                    // Error for not implemented actions
                                    selected.filter { v ->
                                        v !is MyCountryListAction.LoadMyCountriesAction &&
                                                v !is ChangeStateOfCountryAction
                                    }
                                            .flatMap { w ->
                                                Observable.error<MyCountryListResult>(
                                                        IllegalArgumentException("Unknown Action type: $w"))
                                            })
                }
            }


    //todo check types
    private val loadTasks =
            ObservableTransformer<MyCountryListAction.LoadMyCountriesAction, MyCountryListResult> { actions ->
                actions.flatMap { action ->
                    countryRepository.getMyCountries()
                            // Transform the Flowable to an Observable to allow emission of multiple
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

    private val updateTask =
            ObservableTransformer<ChangeStateOfCountryAction, MyCountryListResult.UpdateCountryResult> { actions ->
                actions.flatMap { action ->
                    countryRepository.updateCountry(action.myCountry)
                            .andThen(countryRepository.getCountry(action.myCountry.name))
                            .toObservable()
                            .flatMap { country ->
                                // Emit two events to allow the UI notification to be hidden after
                                // some delay
                                pairWithDelay(
                                        UpdateCountryResult.Success(country.visited),
                                        UpdateCountryResult.ResetState)

                            }
                            .cast(UpdateCountryResult::class.java)
                            .onErrorReturn {
                                UpdateCountryResult.Failure(it)
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                }
            }
}