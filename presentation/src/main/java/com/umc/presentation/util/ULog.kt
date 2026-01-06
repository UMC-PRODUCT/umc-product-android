package com.umc.presentation.util

import android.util.Log

object ULog {

    fun D(msg : String) {
        Log.d("UMC", msg)
    }

    fun D(log : String, msg : String) {
        Log.d(log, msg)
    }
}