package com.cocoslime.concurrencyinkotlin2023

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.newSingleThreadContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}