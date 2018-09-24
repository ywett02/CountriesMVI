package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import com.jurcikova.ivet.countries.mvi.common.BindFragment
import com.jurcikova.ivet.countries.mvi.ui.BaseFragment
import com.jurcikova.ivet.countries.mvi.ui.countryList.CountryAdapter
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryListBinding
import com.strv.ktools.logD
import io.reactivex.Observable
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountryListFragment : BaseFragment<FragmentCountryListBinding, CountryListIntent, CountryListViewState>() {

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

    private val countryListViewModel: CountryListViewModel by viewModel()

    override val binding: FragmentCountryListBinding by BindFragment(R.layout.fragment_country_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        countryListViewModel.states().observe(this, Observer { state ->
            logD("state: $state")

            render(state!!)
        })
    }

    override fun initViews() {
        setupListView()
    }

    override fun intents() = Observable.merge(
            initialIntent,
            swipeToRefreshIntent
    )

    override fun render(state: CountryListViewState) {
        binding.model = state

        if (state.error != null) {
            showErrorMessage(state.error)
        }
    }

    override fun startStream() {
        // Pass the UI's intents to the ViewModel
        countryListViewModel.processIntents(intents())
    }

    private fun setupListView() {
        binding.rvCountries.layoutManager = LinearLayoutManager(activity)
        binding.rvCountries.adapter = adapter

        adapter.countryClickObservable.observe(this, Observer { country ->
            val action = CountryListFragmentDirections.actionCountryListFragmentToCountryDetailFragment(country!!.name)
            findNavController().navigate(action)
        })
    }

    private fun showErrorMessage(exception: Throwable) {
        activity?.let {
            Toast.makeText(it, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
