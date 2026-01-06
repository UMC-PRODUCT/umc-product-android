package com.umc.umc.extension

import kotlin.text.endsWith
import kotlin.text.startsWith

fun String?.isJsonObject(): Boolean = this?.startsWith("{") == true && this.endsWith("}")

fun String?.isJsonArray(): Boolean = this?.startsWith("[") == true && this.endsWith("]")
