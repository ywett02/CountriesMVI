package com.jurcikova.ivet.triptodomvi.ui.myCountries

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import com.jurcikova.ivet.triptodomvi.business.interactor.MyCountryListInteractor
import com.jurcikova.ivet.triptodomvi.common.notOfType
import com.jurcikova.ivet.triptodomvi.mvibase.MviViewModel
import com.strv.ktools.inject
import com.strv.ktools.logD
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class MyCountryListViewModel : ViewModel(), MviViewModel<MyCountryListIntent, MyCountryListViewState> {

    private val myCountryListInteractor by inject<MyCountryListInteractor>()
    /**
     * Proxy subject used to keep the stream alive even after the UI gets recycled.
     * This is basically used to keep ongoing events and the last cached State alive
     * while the UI disconnects and reconnects on config changes.
     */
    private val intentsSubject: PublishSubject<MyCountryListIntent> = PublishSubject.create()

    /**
     * take only the first ever InitialIntent and all intents of other types
     * to avoid reloading data on config changes
     */
    private val intentFilter: ObservableTransformer<MyCountryListIntent, MyCountryListIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { selected ->
                Observable.merge(
                        selected.ofType(MyCountryListIntent.InitialIntent::class.java).take(1),
                        selected.notOfType(MyCountryListIntent.InitialIntent::class.java)
                )
            }
        }

    /**
     * The Reducer is where [MviViewState], that the [MviView] will use to
     * render itself, are created.
     * It takes the last cached [MviViewState], the latest [MviResult] and
     * creates a new [MviViewState] by only updating the related fields.
     * This is basically like a big switch statement of all possible types for the [MviResult]
     */
    private val reducer = BiFunction { previousState: MyCountryListViewState, result: MyCountryListResult ->
        when (result) {
            is MyCountryListResult.LoadCountriesResult -> {
                when (result) {
                    is MyCountryListResult.LoadCountriesResult.Success -> {
                        previousState.copy(
                                isLoading = false,
                                isRefreshing = false,
                                countries = result.myCountries

                        )
                    }
                    is MyCountryListResult.LoadCountriesResult.Failure -> previousState.copy(isLoading = false, isRefreshing = false, error = result.error)
                    is MyCountryListResult.LoadCountriesResult.InProgress -> {
                        if (result.isRefreshing) {
                            previousState.copy(isLoading = false, isRefreshing = true)
                        } else previousState.copy(isLoading = true, isRefreshing = false)
                    }
                }
            }

            is MyCountryListResult.UpdateCountryResult -> when (result) {
                is MyCountryListResult.UpdateCountryResult.Success -> previousState.copy(
                        countryStateChange = if (result.visited) MyCountryListViewState.CountryState.Visited
                        else MyCountryListViewState.CountryState.UnVisited)
                is MyCountryListResult.UpdateCountryResult.Failure -> previousState.copy(error = result.error)
                is MyCountryListResult.UpdateCountryResult.ResetState -> previousState.copy(countryStateChange = null)
            }
        }
    }

    /**
     * Compose all components to create the stream logic
     */
    private val statesObservable: Observable<MyCountryListViewState> = intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .doOnNext { action ->
                logD("action: $action")
            }
            .compose(myCountryListInteractor.actionProcessor)
            // Cache each state and pass it to the reducer to create a new state from
            // the previous cached one and the latest Result emitted from the action processor.
            // The Scan operator is used here for the caching.
            .scan(MyCountryListViewState.idle(), reducer)
            // When a reducer just emits previousState, there's no reason to call render. In fact,
            // redrawing the UI in cases like this can cause jank (e.g. messing up snackbar animations
            // by showing the same snackbar twice in rapid succession).
            .distinctUntilChanged()
            // Emit the last one event of the stream on subscription
            // Useful when a View rebinds to the ViewModel after rotation.
            .replay(1)
            // Create the stream on creation without waiting for anyone to subscribe
            // This allows the stream to stay alive even when the UI disconnects and
            // match the stream's lifecycle to the ViewModel's one.
            .autoConnect(0)

    override fun processIntents(intents: Observable<MyCountryListIntent>) {
        intents
                .doOnNext { intent ->
                    logD("intent: $intent")
                }
                .subscribe(intentsSubject)
    }

    override fun states(): LiveData<MyCountryListViewState> =
            LiveDataReactiveStreams.fromPublisher(statesObservable.toFlowable(BackpressureStrategy.BUFFER))

    /**
     * Translate an [MviIntent] to an [MviAction].
     * Used to decouple the UI and the business logic to allow easy testings and reusability.
     */
    private fun actionFromIntent(intent: MyCountryListIntent): MyCountryListAction {
        return when (intent) {
            MyCountryListIntent.InitialIntent -> MyCountryListAction.LoadMyCountriesAction(false)
            MyCountryListIntent.SwipeToRefresh -> MyCountryListAction.LoadMyCountriesAction(true)
            is MyCountryListIntent.ChangeCountryStateIntent -> MyCountryListAction.ChangeStateOfCountryAction(intent.myCountry)
        }
    }
}