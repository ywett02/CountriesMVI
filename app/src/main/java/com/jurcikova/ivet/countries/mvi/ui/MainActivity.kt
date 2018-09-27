package com.jurcikova.ivet.countries.mvi.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jurcikova.ivet.countries.mvi.common.findNavController

import com.jurcikova.ivet.mvi.R
import com.jurcikova.ivet.mvi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    override fun onSupportNavigateUp()
            = findNavController(R.id.my_nav_host_fragment).navigateUp()
}
