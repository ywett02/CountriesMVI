package com.jurcikova.ivet.countries.mvi.business.interactor

import com.jurcikova.ivet.countries.mvi.business.repository.CountryRepository
import com.jurcikova.ivet.countries.mvi.common.pairWithDelay
import com.jurcikova.ivet.countries.mvi.mvibase.MviInteractor
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.AddToFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.LoadCountriesAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.RemoveFromFavoriteAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListAction.UpdateCountryListAction
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.AddToFavoriteResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.LoadCountriesResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.RemoveFromFavoriteResult
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListResult.UpdateCountryListResult
import com.strv.ktools.logD
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CountryListInteractor(val countryRepository: CountryRepository) : MviInteractor<CountryListAction, CountryListResult> {

	override val actionProcessor =
		ObservableTransformer<CountryListAction, CountryListResult> { actions ->
			actions.publish { selector ->
				Observable.merge(
					selector.ofType(LoadCountriesAction::class.java).compose(loadCountries)
						.doOnNext { result ->
							logD("result: $result")
						},
					selector.ofType(UpdateCountryListAction::class.java).compose(updateCountries)
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

	private val loadCountries =
		ObservableTransformer<LoadCountriesAction, CountryListResult> { actions ->
			actions.flatMap { action ->
				countryRepository.loadCountries()
					.andThen(
						Observable.just(LoadCountriesResult.Success(action.filterType))
					)
					.cast(LoadCountriesResult::class.java)
					.onErrorReturn { LoadCountriesResult.Failure(it) }
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.startWith(LoadCountriesResult.InProgress(action.isRefreshing))
			}
		}

	private val addToFavorite =
		ObservableTransformer<AddToFavoriteAction, CountryListResult> { actions ->
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
					.cast(CountryListResult::class.java)
					.onErrorReturn { AddToFavoriteResult.Failure(it) }
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.startWith(AddToFavoriteResult.InProgress)
			}
		}

	private val removeFromFavorite =
		ObservableTransformer<RemoveFromFavoriteAction, CountryListResult> { actions ->
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
					.cast(CountryListResult::class.java)
					.onErrorReturn { RemoveFromFavoriteResult.Failure(it) }
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.startWith(RemoveFromFavoriteResult.InProgress)
			}
		}

	private val updateCountries =
		ObservableTransformer<UpdateCountryListAction, CountryListResult> { actions ->
			actions.flatMap { action ->
				Observable.just(UpdateCountryListResult.Success(action.countries))
					.cast(UpdateCountryListResult::class.java)
					.onErrorReturn { UpdateCountryListResult.Failure(it) }
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
			}
		}
}
