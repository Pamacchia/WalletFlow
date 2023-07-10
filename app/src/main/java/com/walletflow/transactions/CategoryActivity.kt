package com.walletflow.transactions

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.data.Icon
import com.walletflow.utils.SQLiteDBHelper
import java.io.IOException

abstract class CategoryActivity : BaseActivity() {

    lateinit var selected : String

    fun getIconList(activityType : Int) : MutableList<Icon> {
        val db = SQLiteDBHelper(this, null)

        val cursor = db.getCategories(activityType)

        val iconList: MutableList<Icon> = mutableListOf()

        if (cursor!!.moveToFirst()) {
            val pathIndex = cursor.getColumnIndexOrThrow("file_path")
            val nameIndex = cursor.getColumnIndexOrThrow("icon_name")
            val isAddedIndex = cursor.getColumnIndexOrThrow("isAdded")

            do {
                val iconPath = cursor.getString(pathIndex)
                val iconName = cursor.getString(nameIndex)
                val isAdded = cursor.getInt(isAddedIndex)

                val icon = Icon(iconPath, iconName, isAdded == 1)

                iconList.add(icon)

            } while (cursor.moveToNext())
        }
        return iconList
    }

    fun loadIcons(iconList : MutableList<Icon>){

        val rootView = findViewById<LinearLayout>(R.id.iconsLinearLayout)
        rootView.removeAllViews()

        var count = 0
        lateinit var linearLayout : LinearLayout

        for (icon in iconList!!) {
            if(count%3==0){
                linearLayout = LinearLayout(this)
                linearLayout.id = View.generateViewId()
                rootView.addView(linearLayout)
                linearLayout.orientation = LinearLayout.HORIZONTAL
                linearLayout.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

            val imageView = ImageView(this)

            try {
                // Open the input stream for the image file in assets
                val inputStream = assets.open("icons/${icon.iconPath}")

                // Create a Drawable from the input stream
                val drawable = Drawable.createFromStream(inputStream, null)

                // Set the drawable as the image source for the ImageView
                imageView.setImageDrawable(drawable)

                // Close the input stream
                inputStream.close()

                linearLayout.addView(imageView)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val factor: Float = this.resources.displayMetrics.density

            imageView.layoutParams = LinearLayout.LayoutParams((factor*100).toInt(), (factor*100).toInt(), 1F)
            imageView.tag= icon.iconName
            imageView.setOnClickListener {
                showSelected(imageView)
            }
            count++
        }
    }

    fun showSelected(v : View){

        val images = mutableListOf<ImageView>()
        val imageViews = selectAllImageViews(window.decorView.rootView, images)

        for (imageView in imageViews) {
            imageView.alpha = 1F
        }

        (v as ImageView).alpha = 0.5F
        selected = v.tag.toString()
    }
    private fun selectAllImageViews(view: View, images : MutableList<ImageView>): MutableList<ImageView> {

        if (view is ImageView) {
            images.add(view)
        }

        if (view is ViewGroup) {
            val childCount = view.childCount
            for (i in 0 until childCount) {
                val childView = view.getChildAt(i)
                selectAllImageViews(childView, images)
            }
        }

        return images
    }

}