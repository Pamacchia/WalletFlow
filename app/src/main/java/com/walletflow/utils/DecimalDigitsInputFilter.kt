package com.walletflow.utils

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Matcher
import java.util.regex.Pattern


internal class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) :
    InputFilter {
    private val mPattern: Pattern

    init {
        mPattern =
            Pattern.compile("-?[1-9]([0-9]{0," + (digitsBeforeZero - 1) + "}?)((\\.[0-9]{0," + (digitsAfterZero) + "})?)" +
                    "|(0\\.[0-9]{0," + (digitsAfterZero) + "})")
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): String? {
        val matcher: Matcher = mPattern.matcher(dest)
        return if (!matcher.matches()) "" else null
    }
}