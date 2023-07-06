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
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.data.Icon
import com.walletflow.utils.SQLiteDBHelper
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar

private const val CHOOSE_CATEGORY_TYPE = 1

class ChooseCategoryActivity : CategoryActivity() {

    lateinit var submitBtn : Button
    lateinit var addCategoryBtn : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_category)

        val iconList: MutableList<Icon> = getIconList(CHOOSE_CATEGORY_TYPE)

        loadIcons(iconList)

        submitBtn = findViewById(R.id.btnSubmitCategory)
        addCategoryBtn = findViewById(R.id.btnAddCategory)

        submitBtn.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            addTransaction(db, intent.getFloatExtra("amount", 0F),
                intent.getStringExtra("note"), intent.getStringExtra("recurrency"),
                intent.getStringExtra("userID"), intent.getStringExtra("typeName"), selected)
        }

        addCategoryBtn.setOnClickListener {
            val intent = Intent(this, AddCategoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        val iconList: MutableList<Icon> = getIconList(CHOOSE_CATEGORY_TYPE)

        loadIcons(iconList)
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
}