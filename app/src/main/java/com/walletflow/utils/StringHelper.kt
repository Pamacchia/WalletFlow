package com.walletflow.utils

object StringHelper {

    fun getShrunkForm(number: Double): String {
        val suffixes = arrayOf("", "k", "M", "B", "T", "P", "E", "Z", "Y")

        var num = number
        var index = 0

        while (num >= 10000 && index < suffixes.size - 1) {
            num /= 1000
            index++
        }

        val formattedNum = String.format("%.2f", num)

        return "$formattedNum${suffixes[index]}"
    }
}