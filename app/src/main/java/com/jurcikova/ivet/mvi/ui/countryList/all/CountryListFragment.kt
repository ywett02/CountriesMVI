package com.jurcikova.ivet.countriesMVI.ui.countryList.all

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import com.jurcikova.ivet.countriesMVI.R
import com.jurcikova.ivet.countriesMVI.common.BindFragment
import com.jurcikova.ivet.countriesMVI.databinding.FragmentCountryListBinding
import com.jurcikova.ivet.countriesMVI.mvibase.MviIntent
import com.jurcikova.ivet.countriesMVI.mvibase.MviView
import com.jurcikova.ivet.countriesMVI.ui.countryList.CountryAdapter
import com.strv.ktools.inject
import com.strv.ktools.logD
import io.reactivex.Observable

class CountryListFragment : Fragment(), MviView<CountryListIntent, CountryListViewState> {

    private val viewModel: CountryListViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(CountryListViewModel::class.java)
    }

    //delegate the binding initialization to BindFragment delegate
    private val binding: FragmentCountryListBinding by BindFragment(R.layout.fragment_country_list)

    private val adapter by inject<CountryAdapter>()

    private val initialIntent by lazy {
        Observable.just(CountryListIntent.InitialIntent)
    }

    private val swipeToRefreshIntent by lazy {
        binding.swiperefresh.refreshes()
                .map {
                    CountryListIntent.SwipeToRefresh
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.states().observe(this, Observer { state ->
            logD("state: $state")

            render(state!!)
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

    override fun render(state: CountryListViewState) {
        binding.model = state

        if (state.error != null) {
            showErrorState(state.error)
        }
    }

    override fun intents() = Observable.merge(
            initialIntent,
            swipeToRefreshIntent
    )

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

        adapter.countryClickObservable.observe(this, Observer { country ->
            val action = CountryListFragmentDirections.actionCountryListFragmentToCountryDetailFragment(country!!.name)
            findNavController().navigate(action)
        })
    }

    private fun showErrorState(exception: Throwable) {
        activity?.let {
            Toast.makeText(it, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
