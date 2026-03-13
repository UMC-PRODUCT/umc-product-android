package com.umc.presentation.util

import android.util.Log

object ULog {
    fun d(msg: String) {
        Log.d("UMC LOG", msg)
    }

    fun d(
        log: String,
        msg: String,
    ) {
        Log.d(log, msg)
    }
}
