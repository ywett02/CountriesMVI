package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import com.jurcikova.ivet.countries.mvi.business.interactor.CountryListInteractor
import com.jurcikova.ivet.countries.mvi.common.notOfType
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.InitialIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.*
import com.strv.ktools.logD
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction

class CountryListViewModel(countryListInteractor: CountryListInteractor) : BaseViewModel<CountryListIntent, CountryListAction, CountryListResult, CountryListViewState>() {

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

    override val reducer = BiFunction { previousState: CountryListViewState, result: CountryListResult ->
        when (result) {
            is LoadCountriesResult -> when (result) {
                is LoadCountriesResult.Success -> {
                    previousState.copy(
                            isLoading = false,
                            countries = result.countries,
                            error = null
                    )
                }
                is LoadCountriesResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                is LoadCountriesResult.InProgress -> {
                    previousState.copy(isLoading = true)
                }
            }
            is AddToFavoriteResult -> when (result) {
                is AddToFavoriteResult.Success -> previousState.copy(
                        countries = previousState.countries.map {
                            if (it.name == result.countryName) {
                                it.isFavorite = true
                            }
                            it
                        },
                        isLoading = false, error = null, message = CountryListViewState.MessageType.AddToFavorite)
                is AddToFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                is AddToFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                is AddToFavoriteResult.Reset -> previousState.copy(message = null)
            }
            is RemoveFromFavoriteResult -> when (result) {
                is RemoveFromFavoriteResult.Success -> previousState.copy(
                        countries = previousState.countries.map {
                            if (it.name == result.countryName) {
                                it.isFavorite = false
                            }
                            it
                        }, isLoading = false, error = null, message = CountryListViewState.MessageType.RemoveFromFavorite)
                is RemoveFromFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                is RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                is RemoveFromFavoriteResult.Reset -> previousState.copy(message = null)
            }
        }
    }

    override val statesObservable: Observable<CountryListViewState> = intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .doOnNext { action ->
                logD("action: $action")
            }
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

    override fun states(): LiveData<CountryListViewState> =
            LiveDataReactiveStreams.fromPublisher(statesObservable.toFlowable(BackpressureStrategy.BUFFER))

    override fun processIntents(intents: Observable<CountryListIntent>) {
        intents
                .doOnNext { intent ->
                    logD("intent: $intent")
                }
                .subscribe(intentsSubject)
    }

    override fun actionFromIntent(intent: CountryListIntent): CountryListAction {
        return when (intent) {
            is InitialIntent -> LoadCountriesAction()
            is CountryListIntent.ChangeFilterIntent -> LoadCountriesAction(intent.filterType)
            is CountryListIntent.AddToFavoriteIntent -> CountryListAction.AddToFavoriteAction(intent.countryName)
            is CountryListIntent.RemoveFromFavoriteIntent -> CountryListAction.RemoveFromFavoriteAction(intent.countryName)
        }
    }
}