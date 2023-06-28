package com.walletflow.utils

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object FileManager {

    @Throws(IOException::class)
    private fun copyFile(input : InputStream?, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int? = null
        while (input?.read(buffer).also({ read = it!! }) != -1) {
            read?.let { out.write(buffer, 0, it) }
        }
    }
}