package com.example.mymap

import java.util.*

class Contains {
    companion object {
        const val SEND_INT_KEY_CODE = "Key Int"
        const val KEY_CODE_START = 1
        const val KEY_CODE_END = 2
        const val RESULT = "result"
        fun convertTimeToString(sec:Int): String{
            val hours = (sec / 3600)
            val minutes = (sec % 3600 / 60)
            return String.format(Locale.getDefault(), " Thời gian : %02d h :%02d p", hours, minutes)
        }
        fun conVertMetToString(met:Int):String{
            val km = met / 1000
            val meter = met % 1000
            return String.format(Locale.getDefault(), "Khoảng cách %02d.%02d km", km, meter)
        }
    }


}