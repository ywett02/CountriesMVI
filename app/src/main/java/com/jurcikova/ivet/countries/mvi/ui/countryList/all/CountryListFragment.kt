package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import android.os.Bundle
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.common.bundleOf
import com.jurcikova.ivet.countries.mvi.common.navigate
import com.jurcikova.ivet.countries.mvi.ui.base.BaseFragment
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailFragment.Companion.countryName
import com.jurcikova.ivet.countries.mvi.ui.countryList.CountryAdapter
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.AddToFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.ChangeFilterIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.InitialIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.RemoveFromFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.SwipeToRefresh
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryListBinding
import com.strv.ktools.logD
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject.create
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountryListFragment
	: BaseFragment<FragmentCountryListBinding, CountryListIntent, CountryListViewState>(R.layout.fragment_country_list) {

	private val initialIntentPublisher = create<InitialIntent>()

	private val swipeToRefreshIntent by lazy {
		binding.swiperefresh.refreshes()
			.map {
				SwipeToRefresh
			}
	}

	private val changeFilterPublisher = create<ChangeFilterIntent>()
	private val addToFavoritePublisher = create<AddToFavoriteIntent>()
	private val removeFromFavoritePublisher = create<RemoveFromFavoriteIntent>()

	private val adapter by inject<CountryAdapter>()

	private val viewModel: CountryListViewModel by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding.bottomBar.let {
			it.inflateMenu(R.menu.menu_main)
			it.setOnMenuItemClickListener { item ->
				when (item.itemId) {
					R.id.countrySearchFragment -> NavigationUI.onNavDestinationSelected(item, findNavController())
					R.id.menu_filter -> {
						showFilteringPopUpMenu()
					}
					else -> false
				}
			}
		}

		viewModel.statesLiveData.observe(this, Observer { state ->
			logD("state: $state")

			render(state!!)
		})
	}

	override fun initViews() {
		setupListView()
	}

	override fun intents() = Observable.merge(
		initialIntent(),
		swipeToRefreshIntent,
		changeFilterIntent(),
		addToFavoriteIntent()
	).mergeWith(removeFromFavoriteIntent())

	override fun render(state: CountryListViewState) {
		binding.model = state

		if (state.initial) {
			initialIntentPublisher.onNext(InitialIntent)
		}

		if (state.message != null) {
			showFavoriteStateChangeMessage(state.message)
		}

		if (state.error != null) {
			showErrorMessage(state.error)
		}
	}

	override fun startStream() =
	// Pass the UI's intents to the ViewModel
		viewModel.processIntents(intents())

	private fun setupListView() {
		binding.rvCountries.layoutManager = LinearLayoutManager(activity)
		(binding.rvCountries.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
		binding.rvCountries.adapter = adapter

		adapter.favoriteButtonClickObservable.observe(this, Observer {
			if (it.isFavorite) {
				removeFromFavoritePublisher.onNext(RemoveFromFavoriteIntent(it.name))
			} else {
				addToFavoritePublisher.onNext(AddToFavoriteIntent(it.name))
			}
		})

		adapter.countryClickObservable.observe(this, Observer { country ->
			navigate(R.id.action_countryListFragment_to_countryDetailFragment, bundleOf(countryName to country!!.name))
		})
	}

	private fun initialIntent(): Observable<CountryListIntent.InitialIntent> =
		initialIntentPublisher

	private fun changeFilterIntent(): Observable<CountryListIntent.ChangeFilterIntent> =
		changeFilterPublisher

	private fun addToFavoriteIntent(): Observable<CountryListIntent.AddToFavoriteIntent> =
		addToFavoritePublisher

	private fun removeFromFavoriteIntent(): Observable<CountryListIntent.RemoveFromFavoriteIntent> =
		removeFromFavoritePublisher

	private fun showFilteringPopUpMenu(): Boolean =
		PopupMenu(activity, activity?.findViewById(R.id.menu_filter)).let { menu ->
			menu.menuInflater.inflate(R.menu.menu_filter, menu.menu)
			menu.setOnMenuItemClickListener { item ->
				when (item.itemId) {
					R.id.showAll -> changeFilterPublisher.onNext(ChangeFilterIntent(FilterType.All))
					R.id.showFavorite -> changeFilterPublisher.onNext(ChangeFilterIntent(FilterType.Favorite))
					else -> changeFilterPublisher.onNext(ChangeFilterIntent(FilterType.All))
				}
				true
			}
			menu.show()
			true
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
}
