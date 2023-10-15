package com.cocoslime.concurrencyinkotlin2023

import android.util.Log
import java.lang.Exception

fun String.logDebug(tag: String) {
    Log.d(tag, this)
}

fun Throwable.logError(tag: String) {
    Log.e(tag, this.message ?: "Unknown error")
}