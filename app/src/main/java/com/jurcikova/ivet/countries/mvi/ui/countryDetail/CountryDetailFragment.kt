package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import android.os.Bundle
import com.jurcikova.ivet.countries.mvi.common.setOnClick
import com.jurcikova.ivet.countries.mvi.ui.BaseFragment
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.AddToFavoriteIntent
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.InitialIntent
import com.jurcikova.ivet.countries.mvi.ui.countryDetail.CountryDetailIntent.RemoveFromFavoriteIntent
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryDetailBinding
import com.strv.ktools.logD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountryDetailFragment : BaseFragment<FragmentCountryDetailBinding, CountryDetailIntent, CountryDetailViewState>(R.layout.fragment_country_detail) {

	companion object {
		const val countryName = "countryName"
	}

	private val viewModel: CountryDetailViewModel by viewModel()

	@ExperimentalCoroutinesApi
	@ObsoleteCoroutinesApi
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		launch {
			viewModel.state.consumeEach { state ->
					logD("state: $state")
					render(state)
			}
		}

		setupIntents()
	}

	override fun startStream() =
		launch { viewModel.run { processIntents(intents) } }

	@ExperimentalCoroutinesApi
	override fun setupIntents() {
		binding.fabAdd.setOnClick(this) {
			viewModel.state.value.let {
				if (it.isFavorite) intents.send(RemoveFromFavoriteIntent(it.country!!.name))
				else intents.send(AddToFavoriteIntent(it.country!!.name))
			}
		}
	}

	override fun initViews() {
	}

	override fun render(state: CountryDetailViewState) {
		binding.countryDetailViewState = state

		if (state.initial) {
			launch {
				intents.send(InitialIntent(arguments?.getString(countryName)))
			}
		}

		if (state.showMessage) {
			showMessage(state.isFavorite)
		}

		state.error?.let {
			showErrorMessage(it)
		}
	}

	private fun showMessage(isFavorite: Boolean) =
		toast("Country was marked as ${if (isFavorite) "favorite" else "not favorite"}")

	private fun showErrorMessage(exception: Throwable) =
		toast("Error during fetching from api ${exception.localizedMessage}")
}