package com.jurcikova.ivet.triptodomvi.business.interactor

import com.jurcikova.ivet.triptodomvi.business.repository.CountryRepository
import com.jurcikova.ivet.triptodomvi.mvibase.MviInteractor
import com.jurcikova.ivet.triptodomvi.ui.countryList.search.CountrySearchAction
import com.jurcikova.ivet.triptodomvi.ui.countryList.search.CountrySearchResult
import com.strv.ktools.inject
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
                    // Match LoadCountriesByNameAction to loadTasksByName interactor method
                    selector.ofType(CountrySearchAction.LoadCountriesByNameAction::class.java)
                            .compose(loadTasksByName)
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
                                // Transform the Single to an Observable to allow emission of multiple
                                // events down the stream (e.g. the InFlight event)
                                .toObservable()
                                // Wrap returned data into an immutable object
                                .map { countries -> CountrySearchResult.LoadCountriesByNameResult.Success(countries) }
                                .cast(CountrySearchResult.LoadCountriesByNameResult::class.java)
                                // Wrap any error into an immutable object and pass it down the stream
                                // without crashing.
                                // Because errors are data and hence, should just be part of the stream.
                                .onErrorReturn { CountrySearchResult.LoadCountriesByNameResult.Failure(it) }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                // Emit an InProgress event to notify the subscribers (e.g. the UI) we are
                                // doing work and waiting on a response.
                                // We emit it after observing on the UI thread to allow the event to be emitted
                                // on the current frame and avoid jank.
                                .startWith(CountrySearchResult.LoadCountriesByNameResult.InProgress(action.searchQuery))
                    }
                }
            }
}