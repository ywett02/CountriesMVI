package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import android.os.Bundle
import android.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.common.BindFragment
import com.jurcikova.ivet.countries.mvi.common.OnCountryClickListener
import com.jurcikova.ivet.countries.mvi.common.bundleOf
import com.jurcikova.ivet.countries.mvi.common.navigate
import com.jurcikova.ivet.countries.mvi.ui.BaseFragment
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailFragment.Companion.countryName
import com.jurcikova.ivet.countries.mvi.ui.countryList.CountryAdapter
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryListBinding
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountryListFragment : BaseFragment<FragmentCountryListBinding, CountryListIntent, CountryListViewState>() {

    private val viewModel: CountryListViewModel by viewModel()

    private val adapter = CountryAdapter(object : OnCountryClickListener {
        override fun onFavoriteClick(country: Country) {
            if (country.isFavorite) {
                launch {
                    viewModel.run {
                        processIntents().send(CountryListIntent.RemoveFromFavoriteIntent(country.name))
                    }
                }
            } else {
                launch {
                    viewModel.run {
                        processIntents().send(CountryListIntent.AddToFavoriteIntent(country.name))
                    }
                }
            }
        }

        override fun onCountryClick(country: Country) {
            showCountryDetail(country.name)
        }
    })

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

        launch() {
            viewModel.state.consume {
                for (state in this) {
                    logD("state: $state")
                    render(state)
                }
            }
        }
    }

    override fun setupIntents() {
        binding.swiperefresh.setOnRefreshListener {
            launch {
                viewModel.run {
                    processIntents().send(CountryListIntent.SwipeToRefresh)
                }
            }
        }
    }

    override fun initViews() {
        setupListView()
    }

    override fun render(state: CountryListViewState) {
        binding.model = state

        if (state.initial && !state.isLoading) {
            launch {
                viewModel.run {
                    processIntents().send(CountryListIntent.InitialIntent)
                }
            }
        }

        if (state.message != null) {
            showMessage(state.message)
        }

        if (state.error != null) {
            showErrorMessage(state.error)
        }
    }

    private fun setupListView() {
        binding.rvCountries.layoutManager = LinearLayoutManager(activity)
        (binding.rvCountries.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.rvCountries.adapter = adapter
    }

    private fun showFilteringPopUpMenu(): Boolean =
            PopupMenu(activity, activity?.findViewById(R.id.menu_filter)).let { menu ->
                menu.menuInflater.inflate(R.menu.menu_filter, menu.menu)
                menu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.showAll -> launch {
                            viewModel.run {
                                processIntents().send(CountryListIntent.ChangeFilterIntent(FilterType.All))
                            }
                        }
                        R.id.showFavorite -> launch {
                            viewModel.run {
                                processIntents().send(CountryListIntent.ChangeFilterIntent(FilterType.Favorite))
                            }
                        }
                        else -> launch {
                            viewModel.run {
                                processIntents().send(CountryListIntent.ChangeFilterIntent(FilterType.All))
                            }
                        }
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

    private fun showCountryDetail(name: String) {
        navigate(R.id.action_countryListFragment_to_countryDetailFragment, bundleOf(countryName to name))
    }
}
