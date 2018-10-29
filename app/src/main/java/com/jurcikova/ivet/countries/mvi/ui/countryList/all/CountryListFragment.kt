package com.jurcikova.ivet.countries.mvi.ui.countryList.all

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jurcikova.ivet.countries.mvi.business.entity.Country
import com.jurcikova.ivet.countries.mvi.common.OnItemClickListener
import com.jurcikova.ivet.countries.mvi.common.bundleOf
import com.jurcikova.ivet.countries.mvi.common.navigate
import com.jurcikova.ivet.countries.mvi.ui.BaseFragment
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailFragment.Companion.countryName
import com.jurcikova.ivet.countries.mvi.ui.countryList.CountryAdapter
import com.jurcikova.ivet.countries.mvi.ui.countryList.all.CountryListIntent.*
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryListBinding
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountryListFragment : BaseFragment<FragmentCountryListBinding, CountryListIntent, CountryListViewState>(R.layout.fragment_country_list) {

	private val viewModel: CountryListViewModel by viewModel()

	private val adapter = CountryAdapter(object : OnItemClickListener<Country> {
		override fun onItemClick(item: Country) {
			showCountryDetail(item.name)
		}
	})

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
		binding.swiperefresh.setOnRefreshListener {
			launch {
				intents.send(SwipeToRefresh)
			}
		}
	}

	override fun startStream() =
		launch { viewModel.run { processIntents(intents) } }

	override fun initViews() {
		setupListView()
	}

	override fun render(state: CountryListViewState) {
		binding.model = state

		if (state.initial) {
			launch {
				intents.send(InitialIntent)
			}
		}

		if (state.error != null) {
			showErrorMessage(state.error)
		}
	}

	private fun setupListView() {
		binding.rvCountries.layoutManager = LinearLayoutManager(activity)
		binding.rvCountries.adapter = adapter
	}

	private fun showErrorMessage(exception: Throwable) =
		toast("Error during fetching from api ${exception.localizedMessage}")

	private fun showCountryDetail(name: String) {
		navigate(R.id.action_countryListFragment_to_countryDetailFragment, bundleOf(countryName to name))
	}
}
