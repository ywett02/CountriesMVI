package com.jurcikova.ivet.triptodomvi.ui.countryList

import android.arch.lifecycle.ViewModel
import com.jurcikova.ivet.triptodomvi.business.interactor.CountryListInteractor
import com.jurcikova.ivet.triptodomvi.common.notOfType
import com.jurcikova.ivet.triptodomvi.mvibase.MviViewModel
import com.jurcikova.ivet.triptodomvi.ui.countryList.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.triptodomvi.ui.countryList.CountryListIntent.InitialIntent
import com.jurcikova.ivet.triptodomvi.ui.countryList.CountryListIntent.SearchIntent
import com.jurcikova.ivet.triptodomvi.ui.countryList.CountryListResult.LoadCountriesResult
import com.strv.ktools.inject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class CountryListViewModel : ViewModel(), MviViewModel<CountryListIntent, CountryListViewState> {

    private val countryListInteractor by inject<CountryListInteractor>()

    /**
     * The Reducer is where [MviViewState], that the [MviView] will use to
     * render itself, are created.
     * It takes the last cached [MviViewState], the latest [MviResult] and
     * creates a new [MviViewState] by only updating the related fields.
     * This is basically like a big switch statement of all possible types for the [MviResult]
     */
    private val reducer = BiFunction { previousState: CountryListViewState, result: CountryListResult ->
        when (result) {
            is LoadCountriesResult -> when (result) {
                is LoadCountriesResult.Success -> {
                    previousState.copy(
                            isLoading = false,
                            countries = result.countries

                    )
                }
                is LoadCountriesResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                is LoadCountriesResult.InProgress -> previousState.copy(isLoading = true)
            }
            is CountryListResult.LoadCountriesByNameResult.Success -> {
                previousState.copy(
                        isLoading = false,
                        error = null,
                        countries = result.countries
                )
            }
            is CountryListResult.LoadCountriesByNameResult.Failure -> previousState.copy(isLoading = false, error = result.error)
            is CountryListResult.LoadCountriesByNameResult.InProgress -> previousState.copy(isLoading = true, searchQuery = result.searchQuery)
        }
    }

    /**
     * Proxy subject used to keep the stream alive even after the UI gets recycled.
     * This is basically used to keep ongoing events and the last cached State alive
     * while the UI disconnects and reconnects on config changes.
     */
    private val intentsSubject: PublishSubject<CountryListIntent> = PublishSubject.create()
    /**
     * Compose all components to create the stream logic
     */
    private val statesObservable: Observable<CountryListViewState> = intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(countryListInteractor.actionProcessor)
            // Cache each state and pass it to the reducer to create a new state from
            // the previous cached one and the latest Result emitted from the action processor.
            // The Scan operator is used here for the caching.
            .scan(CountryListViewState.idle(), reducer)
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

    /**
     * take only the first ever InitialIntent and all intents of other types
     * to avoid reloading data on config changes
     */
    private val intentFilter: ObservableTransformer<CountryListIntent, CountryListIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { selected ->
                Observable.merge(
                        selected.ofType(InitialIntent::class.java).take(1),
                        selected.notOfType(InitialIntent::class.java)
                )
            }
        }

    override fun states(): Observable<CountryListViewState> = statesObservable

    override fun processIntents(intents: Observable<CountryListIntent>) {
        intents.subscribe(intentsSubject)
    }

    /**
     * Translate an [MviIntent] to an [MviAction].
     * Used to decouple the UI and the business logic to allow easy testings and reusability.
     */
    private fun actionFromIntent(intent: CountryListIntent): CountryListAction {
        return when (intent) {
            is InitialIntent -> LoadCountriesAction
            is SearchIntent -> {
                if (intent.searchQuery.isEmpty()) LoadCountriesAction
                else CountryListAction.LoadCountriesByNameAction(intent.searchQuery)
            }
        }
    }
}