package com.walletflow

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException


class ChooseCategoryActivity : AppCompatActivity() {

    //lateinit var submitBtn : Button
    lateinit var selected : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_category)

        //submitBtn = findViewById(R.id.btnSubmitCategory)

        val assetManager = resources.assets
        val fileList: Array<String>? = assetManager.list("icons")

        loadIcons(fileList)

        /*submitBtn.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            addTransaction(db, intent.getFloatExtra("amount", 0F),
                intent.getStringExtra("note"), intent.getStringExtra("recurrency"),
                intent.getStringExtra("userID"), intent.getStringExtra("typeName"), selected)
        }*/
    }

    //TODO: da finire non va
    private fun loadIcons(fileList : Array<String>?){

        val rootView = findViewById<ViewGroup>(android.R.id.content)

        for (fileName in fileList!!) {
            val imageView = ImageView(this)

            try {
                // Open the input stream for the image file in assets
                val inputStream = assets.open("icons/my_image.png")

                // Create a Drawable from the input stream
                val drawable = Drawable.createFromStream(inputStream, null)

                // Set the drawable as the image source for the ImageView
                imageView.setImageDrawable(drawable)

                // Close the input stream
                inputStream.close()
                rootView.addView(imageView)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            imageView.layoutParams = layoutParams
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