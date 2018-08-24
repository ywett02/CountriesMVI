package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.common.BindFragment
import com.jurcikova.ivet.countries.mvi.common.OnItemClickListener
import com.jurcikova.ivet.countries.mvi.ui.BaseFragment
import com.jurcikova.ivet.countries.mvi.ui.countryList.CountryAdapter
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryListBinding
import com.strv.ktools.logMe
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.launch

class CountryListFragment : BaseFragment<FragmentCountryListBinding, CountryListIntent, CountryListViewState>() {

    private val adapter = CountryAdapter(object : OnItemClickListener<Country> {
        override fun onItemClick(item: Country) {
            showCountryDetail(item.name)
        }
    })

    private val viewModel: CountryListViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(CountryListViewModel::class.java)
    }

    override val intents = actor<CountryListIntent> {
        for (intent in channel) {
            intent.logMe()

            viewModel.intentProcessor.send(intent)
        }
    }

    override val binding: FragmentCountryListBinding by BindFragment(R.layout.fragment_country_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch(UI, parent = job) {
            viewModel.state.consume {
                for (state in this) {
                    state.logMe()
                    render(state)
                }
            }
        }

        setupIntents()
    }

    override fun setupIntents() {
        launch(UI, parent = job) {
            intents.offer(CountryListIntent.InitialIntent)
        }

        binding.swiperefresh.setOnRefreshListener {
            launch(UI, parent = job) {
                intents.send(CountryListIntent.SwipeToRefresh)
            }
        }
    }

    override fun initViews() {
        setupListView()
    }

    override fun render(state: CountryListViewState) {
        binding.model = state

        if (state.error != null) {
            showErrorMessage(state.error)
        }
    }

    private fun setupListView() {
        binding.rvCountries.layoutManager = LinearLayoutManager(activity)
        binding.rvCountries.adapter = adapter
    }

    private fun showErrorMessage(exception: Throwable) {
        activity?.let {
            Toast.makeText(it, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCountryDetail(name: String) {
        val action = CountryListFragmentDirections.actionCountryListFragmentToCountryDetailFragment(name)
        findNavController().navigate(action)
    }
}
