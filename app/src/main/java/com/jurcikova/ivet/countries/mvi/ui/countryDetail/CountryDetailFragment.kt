package com.jurcikova.ivet.countries.mvi.ui.countryDetail

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.jurcikova.ivet.countries.mvi.business.entity.enums.MessageType
import com.jurcikova.ivet.countries.mvi.common.BindFragment
import com.jurcikova.ivet.countries.mvi.ui.base.BaseFragment
import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.FragmentCountryDetailBinding
import com.strv.ktools.logD
import io.reactivex.Observable
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CountryDetailFragment : BaseFragment<FragmentCountryDetailBinding, CountryDetailIntent, CountryDetailViewState>() {

    private val countryDetailViewModel: CountryDetailViewModel by viewModel()

    private val adapter by inject<CountryPropertyAdapter>()

    private val initialIntent by lazy {
        Observable.just(CountryDetailIntent.InitialIntent(CountryDetailFragmentArgs.fromBundle(arguments).argCountryName) as CountryDetailIntent)
    }

    private val favoriteButtonClickedIntent by lazy {
        RxView.clicks(binding.fabAdd).flatMap {
            countryDetailViewModel.statesStream().map { state ->
                if (state.country?.isFavorite != true) {
                    CountryDetailIntent.AddToFavoriteIntent(state.country!!.name)
                } else {
                    CountryDetailIntent.RemoveFavoriteIntent(state.country.name)
                }
            }.take(1)
        }
    }

    override val binding: FragmentCountryDetailBinding by BindFragment(R.layout.fragment_country_detail)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        countryDetailViewModel.states().observe(this, Observer { state ->
            logD("state: $state")

            render(state!!)
        })
    }

    override fun initViews() {
        setupListView()
    }

    override fun startStream() {
        countryDetailViewModel.processIntents(intents())
    }

    override fun intents(): Observable<CountryDetailIntent> = Observable.merge(
            initialIntent,
            favoriteButtonClickedIntent
    )

    override fun render(state: CountryDetailViewState) {
        binding.countryDetailViewState = state

        if (state.message != null) {
            showMessage(state.message)
        }

        state.error?.let {
            showErrorMessage(it)
        }
    }

    private fun setupListView() {
        binding.rvProperties.layoutManager = LinearLayoutManager(activity)
        binding.rvProperties.adapter = adapter
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
        showMessage(exception.localizedMessage)
    }
}