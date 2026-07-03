package com.anri.weathercalendarapp.common.view.helper

import kotlin.math.roundToInt

fun formatDouble(value: Double): String = "${value.roundToInt()}"

/** 降水確率を10%区切りに四捨五入（0.0〜1.0 → "0%"〜"100%"） */
fun formatPop(pop: Double): String {
    val percent = (pop * 10).roundToInt() * 10
    return "${percent}%"
}