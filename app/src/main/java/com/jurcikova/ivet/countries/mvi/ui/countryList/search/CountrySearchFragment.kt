package com.jurcikova.ivet.countries.mvi.ui.countryList.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.widget.RxSearchView
import com.jurcikova.ivet.countries.mvi.common.BindFragment
import com.jurcikova.ivet.countries.mvi.ui.BaseFragment
import com.jurcikova.ivet.countries.mvi.ui.countryList.CountryAdapter
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountrySearchBinding
import com.strv.ktools.inject
import com.strv.ktools.logD
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class CountrySearchFragment : BaseFragment<FragmentCountrySearchBinding, CountrySearchIntent, CountrySearchViewState>() {

    private val adapter by inject<CountryAdapter>()

    private val viewModel: CountrySearchViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(CountrySearchViewModel::class.java)
    }

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

    override val binding: FragmentCountrySearchBinding by BindFragment(R.layout.fragment_country_search)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.states().observe(this, Observer { state ->
            logD("state: Search $state")
            render(state!!)
        })
    }

    override fun initViews() {
        setupListView()
    }

    override fun intents() = searchIntent as Observable<CountrySearchIntent>

    override fun render(state: CountrySearchViewState) {
        binding.model = state

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

        adapter.countryClickObservable.observe(this, Observer { country ->
            val action = CountrySearchFragmentDirections.actionCountrySearchFragmentToCountryDetailFragment(country!!.name)
            findNavController().navigate(action)
        })
    }

    private fun showErrorMessage(exception: Throwable) {
        activity?.let {
            Toast.makeText(it, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}