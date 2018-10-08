package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.common.BindFragment
import com.jurcikova.ivet.countries.mvi.common.OnItemClickListener
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

    private val adapter = CountryAdapter(object : OnItemClickListener<Country> {
        override fun onItemClick(item: Country) {
            showCountryDetail(item.name)
        }
    })

    override val binding: FragmentCountryListBinding by BindFragment(R.layout.fragment_country_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        launch {
            intents.send(CountryListIntent.InitialIntent)
        }

        binding.swiperefresh.setOnRefreshListener {
            launch {
                intents.send(CountryListIntent.SwipeToRefresh)
            }
        }
    }

    override fun startStream() {
        launch { viewModel.processIntents(intents) }
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
        navigate(R.id.action_countryListFragment_to_countryDetailFragment, bundleOf(countryName to name))
    }
}
