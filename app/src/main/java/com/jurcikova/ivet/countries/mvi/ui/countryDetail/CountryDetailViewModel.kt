package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import com.jurcikova.ivet.countries.mvi.business.interactor.CountryDetailInteractor
import com.jurcikova.ivet.countries.mvi.common.notOfType
import com.jurcikova.ivet.countries.mvi.ui.BaseViewModel
import com.strv.ktools.inject
import com.strv.ktools.logD
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction

class CountryDetailViewModel : BaseViewModel<CountryDetailIntent, CountryDetailAction, CountryDetailResult, CountryDetailViewState>() {

    private val countryDetailInteractor by inject<CountryDetailInteractor>()

    private val intentFilter: ObservableTransformer<CountryDetailIntent, CountryDetailIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { selected ->
                Observable.merge(
                        selected.ofType(CountryDetailIntent.InitialIntent::class.java).take(1),
                        selected.notOfType(CountryDetailIntent.InitialIntent::class.java)
                )
            }
        }

    override val reducer = BiFunction { previousState: CountryDetailViewState, result: CountryDetailResult ->
        when (result) {
            is CountryDetailResult.LoadCountryDetailResult -> when (result) {
                is CountryDetailResult.LoadCountryDetailResult.Success -> previousState.copy(isLoading = false, country = result.country)
                is CountryDetailResult.LoadCountryDetailResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                is CountryDetailResult.LoadCountryDetailResult.InProgress -> previousState.copy(isLoading = true)
            }
            is CountryDetailResult.AddToFavoriteResult -> when (result) {
                is CountryDetailResult.AddToFavoriteResult.Success -> previousState.copy(isLoading = false, isFavorite = true, showMessage = true)
                is CountryDetailResult.AddToFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                is CountryDetailResult.AddToFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                is CountryDetailResult.AddToFavoriteResult.Reset -> previousState.copy(showMessage = false)
            }
            is CountryDetailResult.RemoveFromFavoriteResult -> when (result) {
                is CountryDetailResult.RemoveFromFavoriteResult.Success -> previousState.copy(isLoading = false, isFavorite = false, showMessage = true)
                is CountryDetailResult.RemoveFromFavoriteResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                is CountryDetailResult.RemoveFromFavoriteResult.InProgress -> previousState.copy(isLoading = true)
                is CountryDetailResult.RemoveFromFavoriteResult.Reset -> previousState.copy(showMessage = false)
            }
        }
    }

    override val statesObservable: Observable<CountryDetailViewState> = intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .doOnNext { action ->
                logD("action: $action")
            }
            .compose(countryDetailInteractor.actionProcessor)
            // Cache each state and pass it to the reducer to create a new state from
            // the previous cached one and the latest Result emitted from the action processor.
            // The Scan operator is used here for the caching.
            .scan(CountryDetailViewState.idle(), reducer)
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

    override fun processIntents(intents: Observable<CountryDetailIntent>) {
        intents
                .doOnNext { intent ->
                    logD("intent: $intent")
                }
                .subscribe(intentsSubject)
    }

    override fun states(): LiveData<CountryDetailViewState> =
            LiveDataReactiveStreams.fromPublisher(statesObservable.toFlowable(BackpressureStrategy.BUFFER))

    override fun actionFromIntent(intent: CountryDetailIntent): CountryDetailAction {
        return when (intent) {
            is CountryDetailIntent.InitialIntent -> CountryDetailAction.LoadCountryDetailAction(intent.countryName)
            is CountryDetailIntent.AddToFavoriteIntent -> CountryDetailAction.AddToFavoriteAction(intent.countryName)
            is CountryDetailIntent.RemoveFavoriteIntent -> CountryDetailAction.RemoveFromFavoriteAction(intent.countryName)
        }
    }
}