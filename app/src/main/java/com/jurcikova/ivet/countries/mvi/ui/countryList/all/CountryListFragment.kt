package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.jurcikova.ivet.countries.mvi.common.BindFragment
import com.jurcikova.ivet.countries.mvi.ui.BaseFragment
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

    private val changeFilterPublisher = PublishSubject.create<CountryListIntent.ChangeFilterIntent>()
    private val addToFavoriteIntentPublisher = PublishSubject.create<CountryListIntent.AddToFavoriteIntent>()
    private val removeFromFavoriteIntentPublisher = PublishSubject.create<CountryListIntent.RemoveFromFavoriteIntent>()

    private val adapter by inject<CountryAdapter>()

    private val viewModel: CountryListViewModel by viewModel()

    override val binding: FragmentCountryListBinding by BindFragment(R.layout.fragment_country_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.toolbar.let {
            it.title = activity?.title
            it.inflateMenu(R.menu.menu_main)
            it.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.countrySearchFragment -> NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(binding.root))
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
            changeFilterIntent(),
            addToFavoriteIntentPublisher,
            removeFromFavoriteIntentPublisher
    )

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
        binding.rvCountries.adapter = adapter

        adapter.favoriteButtonClickObservable.observe(this, Observer {
            if (it.isFavorite) {
                removeFromFavoriteIntentPublisher.onNext(CountryListIntent.RemoveFromFavoriteIntent(it.name))
            } else {
                addToFavoriteIntentPublisher.onNext(CountryListIntent.AddToFavoriteIntent(it.name))
            }
        })

        adapter.countryClickObservable.observe(this, Observer { country ->
            val action = CountryListFragmentDirections.actionCountryListFragmentToCountryDetailFragment(country!!.name)
            findNavController().navigate(action)
        })
    }

    private fun changeFilterIntent(): Observable<CountryListIntent.ChangeFilterIntent> =
            changeFilterPublisher

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

    private fun showMessage(messageType: CountryListViewState.MessageType) {
        activity?.let {
            Toast.makeText(it, "Country was " +
                    "${if (messageType is CountryListViewState.MessageType.AddToFavorite) "marked" else "unmarked"} as favorite", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorMessage(exception: Throwable) {
        activity?.let {
            Toast.makeText(it, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
