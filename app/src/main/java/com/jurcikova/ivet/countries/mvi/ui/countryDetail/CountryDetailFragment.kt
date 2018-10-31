package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.ui.base.BaseFragment
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.AddToFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.InitialIntent
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.RemoveFavoriteIntent
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryDetailBinding
import com.strv.ktools.logD
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject.create
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountryDetailFragment
	: BaseFragment<FragmentCountryDetailBinding, CountryDetailIntent, CountryDetailViewState>(R.layout.fragment_country_detail) {

	companion object {
		const val countryName = "countryName"
	}

	private val countryDetailViewModel: CountryDetailViewModel by viewModel()

	private val adapter by inject<CountryPropertyAdapter>()

	private val initialIntentPublisher = create<InitialIntent>()

	private val favoriteButtonClickedIntent by lazy {
		RxView.clicks(binding.fabAdd).flatMap {
			binding.countryDetailViewState?.country?.let { country ->
				Observable.just(
					if (country.isFavorite) {
						RemoveFavoriteIntent(country.name)
					} else {
						AddToFavoriteIntent(country.name)
					}
				)
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		countryDetailViewModel.statesLiveData.observe(this, Observer { state ->
			logD("state: $state")

			render(state!!)
		})
	}

	override fun initViews() {
		setupListView()
	}

	override fun startStream() =
		countryDetailViewModel.processIntents(intents())

	override fun intents(): Observable<CountryDetailIntent> = Observable.merge(
		initialIntent(),
		favoriteButtonClickedIntent
	)

	override fun render(state: CountryDetailViewState) {
		binding.countryDetailViewState = state

		if (state.initial) {
			initialIntentPublisher.onNext(InitialIntent(arguments?.getString(countryName)))
		}

		if (state.message != null) {
			showFavoriteStateChangeMessage(state.message)
		}

		state.error?.let {
			showErrorMessage(it)
		}
	}

	private fun setupListView() {
		binding.rvProperties.layoutManager = LinearLayoutManager(activity)
		binding.rvProperties.adapter = adapter
	}

	private fun showFavoriteStateChangeMessage(messageType: MessageType) =
		toast(getString(R.string.toast_favorite_message,
			if (messageType is MessageType.AddToFavorite) {
				getString(R.string.toast_favorite_message_marked)
			} else {
				getString(R.string.toast_favorite_message_unmarked)
			}))

	private fun showErrorMessage(exception: Throwable) =
		toast(exception.localizedMessage)

	private fun initialIntent(): Observable<InitialIntent> = initialIntentPublisher
}