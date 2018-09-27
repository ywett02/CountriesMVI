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
import com.jurcikova.ivet.countries.mvi.common.BindFragment
import com.jurcikova.ivet.countries.mvi.common.navigate
import com.jurcikova.ivet.countries.mvi.ui.base.BaseFragment
import com.jurcikova.ivet.countries.mvi.ui.countryList.CountryAdapter
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryListBinding
import com.strv.ktools.logD
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountryListFragment : BaseFragment<FragmentCountryListBinding, CountryListIntent, CountryListViewState>() {

    private val initialIntent by lazy {
        Observable.just(CountryListIntent.InitialIntent)
    }

    private val swipeToRefreshIntent by lazy {
        binding.swiperefresh.refreshes()
                .map {
                    CountryListIntent.SwipeToRefresh
                }
    }

    private val changeFilterPublisher = PublishSubject.create<CountryListIntent.ChangeFilterIntent>()
    private val addToFavoritePublisher = PublishSubject.create<CountryListIntent.AddToFavoriteIntent>()
    private val removeFromFavoritePublisher = PublishSubject.create<CountryListIntent.RemoveFromFavoriteIntent>()

    private val adapter by inject<CountryAdapter>()

    private val viewModel: CountryListViewModel by viewModel()

    override val binding: FragmentCountryListBinding by BindFragment(R.layout.fragment_country_list)

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

        viewModel.states().observe(this, Observer { state ->
            logD("state: $state")

            render(state!!)
        })
    }

    override fun initViews() {
        setupListView()
    }

    override fun intents() = Observable.merge(
            initialIntent,
            swipeToRefreshIntent,
            changeFilterIntent(),
            addToFavoriteIntent()
    ).mergeWith(removeFromFavoriteIntent())


    override fun render(state: CountryListViewState) {
        binding.model = state

        if (state.message != null) {
            showMessage(state.message)
        }

        if (state.error != null) {
            showErrorMessage(state.error)
        }
    }

    override fun startStream() {
        // Pass the UI's intents to the ViewModel
        viewModel.processIntents(intents())
    }

    private fun setupListView() {
        binding.rvCountries.layoutManager = LinearLayoutManager(activity)
        (binding.rvCountries.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.rvCountries.adapter = adapter

        adapter.favoriteButtonClickObservable.observe(this, Observer {
            if (it.isFavorite) {
                removeFromFavoritePublisher.onNext(CountryListIntent.RemoveFromFavoriteIntent(it.name))
            } else {
                addToFavoritePublisher.onNext(CountryListIntent.AddToFavoriteIntent(it.name))
            }
        })

        adapter.countryClickObservable.observe(this, Observer { country ->
            navigate(CountryListFragmentDirections.actionCountryListFragmentToCountryDetailFragment(country!!.name))
        })
    }

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
                        R.id.showAll -> changeFilterPublisher.onNext(CountryListIntent.ChangeFilterIntent(FilterType.All))
                        R.id.showFavorite -> changeFilterPublisher.onNext(CountryListIntent.ChangeFilterIntent(FilterType.Favorite))
                        else -> changeFilterPublisher.onNext(CountryListIntent.ChangeFilterIntent(FilterType.All))
                    }
                    true
                }
                menu.show()
                true
            }

    private fun showMessage(messageType: MessageType) {
        showMessage(getString(R.string.toast_favorite_message,
                if (messageType is MessageType.AddToFavorite) {
                    getString(R.string.toast_favorite_message_marked)
                } else {
                    getString(R.string.toast_favorite_message_unmarked)
                }))
    }

    private fun showErrorMessage(exception: Throwable) {
        showMessage(exception.localizedMessage)
    }
}
