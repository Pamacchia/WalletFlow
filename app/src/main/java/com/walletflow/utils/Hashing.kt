package com.walletflow.utils

import java.security.MessageDigest

object Hashing {

    fun hashPassword(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashBytes = messageDigest.digest(password.toByteArray())
        return bytesToHex(hashBytes)
    }

    private fun bytesToHex(hashBytes: ByteArray): String {
        val hexChars = "0123456789ABCDEF"
        val hexBuilder = StringBuilder(hashBytes.size * 2)

        for (byte in hashBytes) {
            val highNibble = (byte.toInt() and 0xF0) ushr 4
            val lowNibble = byte.toInt() and 0x0F
            hexBuilder.append(hexChars[highNibble])
            hexBuilder.append(hexChars[lowNibble])
        }

        return hexBuilder.toString()
    }
}