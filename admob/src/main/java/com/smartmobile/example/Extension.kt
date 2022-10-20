package com.smartmobile.example

import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import android.view.View


val Number.toPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

fun log(msg: String) {
//    if (BuildConfig.DEBUG)
        Log.d("===Debug", msg)
}

fun loge(msg: String) {
//    if (BuildConfig.DEBUG)
        Log.e("===Error", msg)
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}
