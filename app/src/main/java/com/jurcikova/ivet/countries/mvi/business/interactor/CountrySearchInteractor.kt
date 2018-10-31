package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.common.pairWithDelay
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction.AddToFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction.LoadCountriesByNameAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchAction.RemoveFromFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult.AddToFavoriteResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult.LoadCountriesByNameResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchResult.RemoveFromFavoriteResult
import com.strv.ktools.logD
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CountrySearchInteractor(val countryRepository: CountryRepository) : MviInteractor<CountrySearchAction, CountrySearchResult> {

	override val actionProcessor =
		ObservableTransformer<CountrySearchAction, CountrySearchResult> { actions ->
			actions.publish { selector ->
				Observable.merge(
					selector.ofType(LoadCountriesByNameAction::class.java)
						.compose(loadCountriesByName)
						.doOnNext { result ->
							logD("result: $result")
						},
					selector.ofType(AddToFavoriteAction::class.java).compose(addToFavorite)
						.doOnNext { result ->
							logD("result: $result")
						},
					selector.ofType(RemoveFromFavoriteAction::class.java).compose(removeFromFavorite)
						.doOnNext { result ->
							logD("result: $result")
						}
				)
			}
		}

	private val loadCountriesByName =
		ObservableTransformer<LoadCountriesByNameAction, CountrySearchResult> { actions ->
			actions.flatMap { action ->
				if (action.searchQuery.isBlank()) {
					Observable.just(LoadCountriesByNameResult.NotStarted)
				} else {
					countryRepository.getCountriesByName(action.searchQuery)
						.toObservable()
						.map { countries -> LoadCountriesByNameResult.Success(countries) }
						.cast(LoadCountriesByNameResult::class.java)
						.onErrorReturn { error ->
							//because in case of empty result api returns 404 :(
							if (error is HttpException && error.code() == 404) {
								LoadCountriesByNameResult.Success(emptyList())
							} else LoadCountriesByNameResult.Failure(error)
						}
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.startWith(LoadCountriesByNameResult.InProgress(action.searchQuery))
				}
			}
		}

	private val addToFavorite =
		ObservableTransformer<AddToFavoriteAction, CountrySearchResult> { actions ->
			actions.flatMap { action ->
				Completable.fromAction {
					countryRepository.addToFavorite(action.countryName)
				}
					.andThen(
						// Emit two events to allow the UI notification to be hidden after
						// some delay
						pairWithDelay(
							AddToFavoriteResult.Success,
							AddToFavoriteResult.Reset)
					)
					.cast(CountrySearchResult::class.java)
					.onErrorReturn { AddToFavoriteResult.Failure(it) }
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.startWith(AddToFavoriteResult.InProgress)
			}
		}

	private val removeFromFavorite =
		ObservableTransformer<RemoveFromFavoriteAction, CountrySearchResult> { actions ->
			actions.flatMap { action ->
				Completable.fromAction {
					countryRepository.removeFromFavorite(action.countryName)
				}
					.andThen(
						// Emit two events to allow the UI notification to be hidden after
						// some delay
						pairWithDelay(
							RemoveFromFavoriteResult.Success,
							RemoveFromFavoriteResult.Reset)
					)
					.cast(CountrySearchResult::class.java)
					.onErrorReturn { RemoveFromFavoriteResult.Failure(it) }
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.startWith(RemoveFromFavoriteResult.InProgress)
			}
		}
}