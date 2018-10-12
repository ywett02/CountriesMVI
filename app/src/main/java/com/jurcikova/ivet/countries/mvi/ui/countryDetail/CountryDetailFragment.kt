package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import android.os.Bundle
import android.widget.Toast
import com.jurcikova.ivet.countries.mvi.common.BindFragment
import com.jurcikova.ivet.countries.mvi.common.setOnClick
import com.jurcikova.ivet.countries.mvi.ui.BaseFragment
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryDetailBinding
import com.strv.ktools.logD
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountryDetailFragment : BaseFragment<FragmentCountryDetailBinding, CountryDetailIntent, CountryDetailViewState>() {

    companion object {
        const val countryName = "countryName"
    }

    private val viewModel: CountryDetailViewModel by viewModel()

    override val binding: FragmentCountryDetailBinding by BindFragment(R.layout.fragment_country_detail)

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

    override fun startStream() {
        launch { viewModel.processIntents(intents) }
    }

    override fun setupIntents() {
        binding.fabAdd.setOnClick(this) {
            viewModel.state.value.let {
                if (it.isFavorite) intents.send(CountryDetailIntent.RemoveFromFavoriteIntent(it.country!!.name))
                else intents.send(CountryDetailIntent.AddToFavoriteIntent(it.country!!.name))
            }
        }
    }

    override fun initViews() {
    }

    override fun render(state: CountryDetailViewState) {
        binding.countryDetailViewState = state

        if (state.initial) {
            launch {
                intents.send(CountryDetailIntent.InitialIntent(arguments?.getString(countryName)))
            }
        }

        if (state.showMessage) {
            showMessage(state.isFavorite)
        }

        state.error?.let {
            showErrorMessage(it)
        }
    }

    private fun showMessage(isFavorite: Boolean) {
        activity?.let {
            Toast.makeText(it, "Country was marked as ${if (isFavorite) "favorite" else "not favorite"}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorMessage(exception: Throwable) {
        activity?.let {
            Toast.makeText(it, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}