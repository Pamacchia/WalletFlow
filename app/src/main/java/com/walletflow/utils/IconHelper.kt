package com.walletflow.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView

object IconHelper {

    fun setIconCard(context : Context, categoryName: String?, frequentTransactionIv: ImageView) {
        val localDB = SQLiteDBHelper(context, null)
        val filePath = localDB.getCategoryImage(categoryName!!)
        val inputStream = context.assets?.open("icons/${filePath}")
        val drawable = Drawable.createFromStream(inputStream, null)
        frequentTransactionIv.setImageDrawable(drawable)
        inputStream!!.close()
    }

}