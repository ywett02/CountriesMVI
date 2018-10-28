package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.jakewharton.rxbinding2.widget.RxSearchView
import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.common.bundleOf
import com.jurcikova.ivet.countries.mvi.common.hideKeyboard
import com.jurcikova.ivet.countries.mvi.common.navigate
import com.jurcikova.ivet.countries.mvi.ui.base.BaseFragment
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailFragment.Companion.countryName
import com.jurcikova.ivet.countries.mvi.ui.countryList.CountryAdapter
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchIntent.AddToFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.search.CountrySearchIntent.RemoveFromFavoriteIntent
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountrySearchBinding
import com.strv.ktools.logD
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject.create
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class CountrySearchFragment
	: BaseFragment<FragmentCountrySearchBinding, CountrySearchIntent, CountrySearchViewState>(R.layout.fragment_country_search) {

	private val adapter by inject<CountryAdapter>()

	private val countrySearchViewModel: CountrySearchViewModel by viewModel()

	private val addToFavoritePublisher = create<AddToFavoriteIntent>()
	private val removeFromFavoritePublisher = create<RemoveFromFavoriteIntent>()

	private val searchIntent by lazy {
		RxSearchView.queryTextChanges(binding.searchView)
			//because after orientation change the passed value would be emitted
			.skip(2)
			.filter {
				it.length > 2 || it.isEmpty()
			}
			.debounce(500, TimeUnit.MILLISECONDS)
			.map {
				CountrySearchIntent.SearchIntent(it.toString())
			}.cast(CountrySearchIntent::class.java)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		countrySearchViewModel.statesLiveData.observe(this, Observer { state ->
			logD("state: Search $state")
			render(state!!)
		})
	}

	override fun initViews() {
		setupListView()
	}

	override fun intents() =
		Observable.merge(
			searchIntent,
			addToFavoriteIntent(),
			removeFromFavoriteIntent()
		)

	override fun render(state: CountrySearchViewState) {
		binding.model = state

		if (state.message != null) {
			showFavoriteStateChangeMessage(state.message)
		}

		if (state.error != null) {
			showErrorMessage(state.error)
		}
	}

	override fun startStream() =
	// Pass the UI's intents to the ViewModel
		countrySearchViewModel.processIntents(intents())

	private fun setupListView() {
		binding.rvCountries.layoutManager = LinearLayoutManager(activity)
		(binding.rvCountries.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
		binding.rvCountries.adapter = adapter

		adapter.countryClickObservable.observe(this, Observer { country ->
			hideKeyboard()
			navigate(R.id.action_countrySearchFragment_to_countryDetailFragment, bundleOf(countryName to country!!.name))
		})

		adapter.favoriteButtonClickObservable.observe(this, Observer {
			if (it.isFavorite) {
				removeFromFavoritePublisher.onNext(RemoveFromFavoriteIntent(it.name))
			} else {
				addToFavoritePublisher.onNext(AddToFavoriteIntent(it.name))
			}
		})
	}

	private fun addToFavoriteIntent(): Observable<AddToFavoriteIntent> =
		addToFavoritePublisher

	private fun removeFromFavoriteIntent(): Observable<RemoveFromFavoriteIntent> =
		removeFromFavoritePublisher

	private fun showFavoriteStateChangeMessage(messageType: MessageType) =
		toast(getString(R.string.toast_favorite_message,
			if (messageType is MessageType.AddToFavorite) {
				getString(R.string.toast_favorite_message_marked)
			} else {
				getString(R.string.toast_favorite_message_unmarked)
			}))

	private fun showErrorMessage(exception: Throwable) =
		toast(exception.localizedMessage)
}