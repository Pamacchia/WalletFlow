package com.walletflow

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class ChooseCategoryActivity : AppCompatActivity() {

    lateinit var submitBtn : Button
    lateinit var addCategoryBtn : Button
    lateinit var selected : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_category)

        val assetManager = resources.assets
        val fileList: Array<String>? = assetManager.list("icons/test")
        loadIcons(fileList)

        submitBtn = findViewById(R.id.btnSubmitCategory)
        addCategoryBtn = findViewById(R.id.btnAddCategory)

        submitBtn.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            addTransaction(db, intent.getFloatExtra("amount", 0F),
                intent.getStringExtra("note"), intent.getStringExtra("recurrency"),
                intent.getStringExtra("userID"), intent.getStringExtra("typeName"), selected)
        }

        addCategoryBtn.setOnClickListener {
        }
    }

    private fun loadIcons(fileList : Array<String>?){

        val rootView = findViewById<LinearLayout>(R.id.iconsLinearLayout)

        var count = 0
        lateinit var linearLayout : LinearLayout
        for (fileName in fileList!!) {
            if(count%3==0){
                linearLayout = LinearLayout(this)
                linearLayout.id=View.generateViewId()
                rootView.addView(linearLayout)
                linearLayout.orientation = LinearLayout.HORIZONTAL
                linearLayout.layoutParams.height = LayoutParams.WRAP_CONTENT
            }

            val imageView = ImageView(this)

            try {
                // Open the input stream for the image file in assets
                val inputStream = assets.open("icons/test/$fileName")

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

    private fun addTransaction(db : FirebaseFirestore, amount : Float, note : String?, recurrency : String?, userID : String?, type_name : String?, category : String?){
        val transaction: MutableMap<String, Any?> = HashMap()
        transaction["user"] = userID
        transaction["type"] = type_name
        transaction["amount"] = amount
        transaction["recurrency"] = recurrency
        transaction["note"] = note
        transaction["category"] = category
        transaction["date"] = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().time)

        db.collection("transactions")
            .add(transaction)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    this.localClassName,
                    "DocumentSnapshot added with ID: " + documentReference.id
                )
            }
            .addOnFailureListener { e ->
                Log.w(
                    this.localClassName,
                    "Error adding document",
                    e
                )
            }

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
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