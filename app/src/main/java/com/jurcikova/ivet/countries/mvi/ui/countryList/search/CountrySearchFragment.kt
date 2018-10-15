package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
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
import com.jurcikova.ivet.mvi.databinding.FragmentCountrySearchBinding
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountrySearchFragment : BaseFragment<FragmentCountrySearchBinding, CountrySearchIntent, CountrySearchViewState>() {

    private val viewModel: CountrySearchViewModel by viewModel()

    private val adapter = CountryAdapter(object : OnCountryClickListener {
        override fun onFavoriteClick(country: Country) {
            if (country.isFavorite) {
                launch {
                    viewModel.run {
                        processIntents().send(CountrySearchIntent.RemoveFromFavoriteIntent(country.name))
                    }
                }
            } else {
                launch {
                    viewModel.run {
                        processIntents().send(CountrySearchIntent.AddToFavoriteIntent(country.name))
                    }
                }
            }
        }

        override fun onCountryClick(country: Country) {
            showCountryDetail(country.name)
        }
    })

    override val binding: FragmentCountrySearchBinding by BindFragment(R.layout.fragment_country_search)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch {
            viewModel.state.consume {
                for (state in this) {
                    logD("state: $state")
                    render(state)
                }
            }
        }

        setupIntents()
    }

    override fun setupIntents() {
        var searchJob = Job()
        var skippedFirst = false

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String): Boolean = true
            override fun onQueryTextChange(p0: String): Boolean {
                if ((p0.length > 2 || p0.isBlank()) && skippedFirst) {
                    searchJob.cancel()

                    searchJob = launch {
                        delay(500)
                        viewModel.run {
                            processIntents().send(CountrySearchIntent.SearchIntent(p0))
                        }
                    }
                }
                skippedFirst = true
                return true
            }
        })
    }

    override fun initViews() {
        setupListView()
    }

    override fun render(state: CountrySearchViewState) {
        binding.model = state

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

    private fun showMessage(messageType: MessageType) {
        showMessage(getString(R.string.toast_favorite_message,
                if (messageType is MessageType.AddToFavorite) {
                    getString(R.string.toast_favorite_message_marked)
                } else {
                    getString(R.string.toast_favorite_message_unmarked)
                }))
    }

    private fun showErrorMessage(exception: Throwable) {
        activity?.let {
            Toast.makeText(it, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCountryDetail(name: String) {
        navigate(R.id.action_countrySearchFragment_to_countryDetailFragment, bundleOf(countryName to name))
    }
}