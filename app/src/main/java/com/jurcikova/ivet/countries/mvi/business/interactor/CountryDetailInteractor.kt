package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.common.pairWithDelay
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction.AddToFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction.LoadCountryDetailAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailAction.RemoveFromFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult.AddToFavoriteResult
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult.LoadCountryDetailResult
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailResult.RemoveFromFavoriteResult
import com.strv.ktools.logD
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CountryDetailInteractor(val countryRepository: CountryRepository) : MviInteractor<CountryDetailAction, CountryDetailResult> {

	override val actionProcessor =
		ObservableTransformer<CountryDetailAction, CountryDetailResult> { actions ->
			actions.publish { selector ->
				Observable.merge(
					selector.ofType(LoadCountryDetailAction::class.java).compose(loadCountryDetail)
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

	private val loadCountryDetail =
		ObservableTransformer<LoadCountryDetailAction, CountryDetailResult> { actions ->
			actions.flatMap { action ->
				countryRepository.getCountry(action.countryName)
					.toObservable()
					// Wrap returned data into an immutable object
					.map { country -> LoadCountryDetailResult.Success(country) }
					.cast(LoadCountryDetailResult::class.java)
					// Wrap any error into an immutable object and pass it down the stream
					// without crashing.
					// Because errors are data and hence, should just be part of the stream.
					.onErrorReturn { LoadCountryDetailResult.Failure(it) }
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					// Emit an InProgress event to notify the subscribers (e.g. the UI) we are
					// doing work and waiting on a response.
					// We emit it after observing on the UI thread to allow the event to be emitted
					// on the current frame and avoid jank.
					.startWith(LoadCountryDetailResult.InProgress)
			}
		}

	private val addToFavorite =
		ObservableTransformer<AddToFavoriteAction, CountryDetailResult> { actions ->
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
					.cast(CountryDetailResult::class.java)
					.onErrorReturn { AddToFavoriteResult.Failure(it) }
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.startWith(AddToFavoriteResult.InProgress)
			}
		}

	private val removeFromFavorite =
		ObservableTransformer<RemoveFromFavoriteAction, CountryDetailResult> { actions ->
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
					.cast(CountryDetailResult::class.java)
					.onErrorReturn { RemoveFromFavoriteResult.Failure(it) }
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.startWith(RemoveFromFavoriteResult.InProgress)
			}
		}
}