package com.walletflow

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.R
import com.walletflow.utils.FileManager
import com.walletflow.utils.SQLiteDBHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class AddCategoryActivity : AppCompatActivity() {

//    TODO: Ereditarieta'

    lateinit var addCategoryBtn : Button
    lateinit var selected : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)


        val db = SQLiteDBHelper(this, null)

        val cursor = db.getToAdd()

        val fileList: MutableList<String> = mutableListOf()

        if (cursor!!.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow("file_path")

            do {
                val filePath = cursor.getString(columnIndex)
                fileList.add(filePath)
            } while (cursor.moveToNext())
        }

        loadIcons(fileList)

        addCategoryBtn = findViewById(R.id.btnAddCategory)

        addCategoryBtn.setOnClickListener {


            finish()
        }
    }

    private fun loadIcons(fileList : MutableList<String>){

        val rootView = findViewById<LinearLayout>(R.id.iconsLinearLayout)

        var count = 0
        lateinit var linearLayout : LinearLayout

        for (fileName in fileList!!) {
            if(count%3==0){
                linearLayout = LinearLayout(this)
                linearLayout.id= View.generateViewId()
                rootView.addView(linearLayout)
                linearLayout.orientation = LinearLayout.HORIZONTAL
                linearLayout.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

            val imageView = ImageView(this)

            try {
                // Open the input stream for the image file in assets
                val inputStream = assets.open("icons/$fileName")

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
            imageView.tag= fileName.split(".")[0]
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