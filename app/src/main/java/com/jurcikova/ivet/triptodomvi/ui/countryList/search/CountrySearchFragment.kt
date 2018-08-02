package com.jurcikova.ivet.triptodomvi.ui.countryList.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jakewharton.rxbinding2.widget.RxSearchView
import com.jurcikova.ivet.triptodomvi.R
import com.jurcikova.ivet.triptodomvi.common.BindFragment
import com.jurcikova.ivet.triptodomvi.databinding.FragmentCountrySearchBinding
import com.jurcikova.ivet.triptodomvi.mvibase.MviIntent
import com.jurcikova.ivet.triptodomvi.mvibase.MviView
import com.jurcikova.ivet.triptodomvi.ui.countryList.CountryAdapter
import com.strv.ktools.inject
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class CountrySearchFragment : Fragment(), MviView<CountrySearchIntent, CountrySearchViewState> {

    private val viewModel: CountrySearchViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(CountrySearchViewModel::class.java)
    }

    //delegate the binding initialization to BindFragment delegate
    private val binding: FragmentCountrySearchBinding by BindFragment(R.layout.fragment_country_search)

    private val adapter by inject<CountryAdapter>()

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
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.states().observe(this, Observer {
            render(it!!)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = binding.root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupListView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startStream()
    }

    override fun render(state: CountrySearchViewState) {
        binding.model = state

        if (state.error != null) {
            showErrorState(state.error)
        }
    }

    override fun intents() = searchIntent as Observable<CountrySearchIntent>

    /**
     *  Start the stream by passing [MviIntent] to [MviViewModel]
     */
    private fun startStream() {
        // Pass the UI's intents to the ViewModel
        viewModel.processIntents(intents())
    }

    private fun setupListView() {
        binding.rvCountries.layoutManager = LinearLayoutManager(activity)
        binding.rvCountries.adapter = adapter
    }

    private fun showErrorState(exception: Throwable) {
        activity?.let {
            Toast.makeText(it, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}