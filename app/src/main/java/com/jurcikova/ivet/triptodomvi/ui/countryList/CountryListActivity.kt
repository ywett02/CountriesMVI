package com.jurcikova.ivet.triptodomvi.ui.countryList

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.example.android.architecture.blueprints.todoapp.mvibase.MviViewState
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jurcikova.ivet.triptodomvi.R
import com.jurcikova.ivet.triptodomvi.common.BindActivity
import com.jurcikova.ivet.triptodomvi.databinding.ActivityCountryListBinding
import com.jurcikova.ivet.triptodomvi.mvibase.MviView
import com.strv.ktools.inject
import com.strv.ktools.logMe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class CountryListActivity : AppCompatActivity(), MviView<CountryListIntent, CountryListViewState> {


    private val viewModel: CountryListViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(CountryListViewModel::class.java)
    }

    //delegate the binding initialization to BindFragment delegate
    private val binding: ActivityCountryListBinding by BindActivity(R.layout.activity_country_list)

    // Used to manage the data flow lifecycle and avoid memory leak.
    private val disposables = CompositeDisposable()

    private val adapter by inject<CountryAdapter>()

    private val initialIntent by lazy {
        Observable.just(CountryListIntent.InitialIntent)
    }

    private val searchIntent by lazy {
        RxTextView.textChanges(binding.edSearch)
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

        bind()
    }

    override fun render(state: CountryListViewState) {
        state.logMe()
        binding.model = state

        if (state.error != null) {
            showErrorState(state.error)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        disposables.dispose()
    }

    override fun intents() =
            Observable.merge(
                    initialIntent,
                    searchIntent
            )

    /**
     * Connect the [MviView] with the [MviViewModel]
     * We subscribe to the [MviViewModel] before passing it the [MviView]'s [MviIntent]s.
     * If we were to pass [MviIntent]s to the [MviViewModel] before listening to it,
     * emitted [MviViewState]s could be lost
     */
    @SuppressLint("RxSubscribeOnError")
    private fun bind() {
        setupListView()
        // Subscribe to the ViewModel and call render for every emitted state
        disposables.add(viewModel.states().subscribe(this::render))
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
