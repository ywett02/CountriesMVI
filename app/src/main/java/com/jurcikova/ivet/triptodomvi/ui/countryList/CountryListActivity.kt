package com.jurcikova.ivet.triptodomvi.ui.countryList

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jurcikova.ivet.triptodomvi.R
import com.jurcikova.ivet.triptodomvi.common.BindActivity
import com.jurcikova.ivet.triptodomvi.databinding.ActivityCountryListBinding
import com.jurcikova.ivet.triptodomvi.mvibase.MviIntent
import com.jurcikova.ivet.triptodomvi.mvibase.MviView
import com.strv.ktools.inject
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class CountryListActivity : AppCompatActivity(), MviView<CountryListIntent, CountryListViewState> {

    private val viewModel: CountryListViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(CountryListViewModel::class.java)
    }

    //delegate the binding initialization to BindFragment delegate
    private val binding: ActivityCountryListBinding by BindActivity(R.layout.activity_country_list)

    private val adapter by inject<CountryAdapter>()

    private val initialIntent by lazy {
        Observable.just(CountryListIntent.InitialIntent)
    }

    private val searchIntent by lazy {
        RxTextView.textChanges(binding.edSearch)
                //because after orientation change the passed value would be emitted
                .skip(2)
                .filter {
                    it.length > 2 || it.isEmpty()
                }
                .debounce(500, TimeUnit.MILLISECONDS)
                .map {
                    CountryListIntent.SearchIntent(it.toString())
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country_list)

        viewModel.states().observe(this, Observer {
            render(it!!)
        })

        setupListView()
        startStream()
    }

    override fun render(state: CountryListViewState) {
        binding.model = state

        if (state.error != null) {
            showErrorState(state.error)
        }
    }

    override fun intents() =
            Observable.merge(
                    initialIntent,
                    searchIntent
            )

    /**
     *  Start the stream by passing [MviIntent] to [MviViewModel]
     */
    private fun startStream() {
        // Pass the UI's intents to the ViewModel
        viewModel.processIntents(intents())
    }

    private fun setupListView() {
        binding.rvCountries.layoutManager = LinearLayoutManager(this)
        binding.rvCountries.adapter = adapter
    }

    private fun showErrorState(exception: Throwable) {
        Toast.makeText(this, "Error during fetching from api ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
