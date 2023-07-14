package com.walletflow.transactions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.HomeActivity
import com.walletflow.R
import com.walletflow.data.Icon
import java.text.SimpleDateFormat
import java.util.Calendar

private const val CHOOSE_CATEGORY_TYPE = 1

class ChooseCategoryActivity : CategoryActivity() {

    lateinit var submitBtn : Button
//    lateinit var addCategoryBtn : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_category_choose)

        val iconList: MutableList<Icon> = getIconList(CHOOSE_CATEGORY_TYPE)

        loadIcons(iconList)

        submitBtn = findViewById(R.id.btnSubmitCategory)
//        addCategoryBtn = findViewById(R.id.btnAddCategory)

        submitBtn.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            addTransaction(db, intent.getFloatExtra("amount", 0F),
                intent.getStringExtra("note"), intent.getStringExtra("recurrency"),
                intent.getStringExtra("userID"), intent.getStringExtra("typeName"), selected)
        }

//        addCategoryBtn.setOnClickListener {
//            val intent = Intent(this, AddCategoryActivity::class.java)
//            startActivity(intent)
//        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_transaction_category_choose
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