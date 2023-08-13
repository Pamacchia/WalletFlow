package com.walletflow.transactions

import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.R
import com.walletflow.data.Icon
import com.walletflow.utils.TransactionManager
import java.text.SimpleDateFormat
import java.util.Calendar

private const val CHOOSE_CATEGORY_TYPE = 1

class ChooseCategoryActivity : CategoryActivity() {

    lateinit var submitBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val iconList: MutableList<Icon> = getIconList(CHOOSE_CATEGORY_TYPE)

        loadIcons(iconList)

        submitBtn = findViewById(R.id.btnSubmitCategory)

        submitBtn.setOnClickListener {
            val db = FirebaseFirestore.getInstance()

            addTransaction(
                db, intent.getFloatExtra("amount", 0F),
                intent.getStringExtra("note"), intent.getBooleanExtra("frequent", false),
                intent.getStringExtra("userID"), intent.getStringExtra("typeName"), selected
            )

            TransactionManager.updateBalance(
                db,
                intent.getFloatExtra("amount", 0F),
                intent.getStringExtra("userID")
            )
            Thread.sleep(150L)
            finish()
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_transaction_category_choose
    }

    override fun onRestart() {
        super.onRestart()
        val iconList: MutableList<Icon> = getIconList(CHOOSE_CATEGORY_TYPE)

        loadIcons(iconList)
    }

    private fun addTransaction(
        db: FirebaseFirestore,
        amount: Float,
        note: String?,
        frequent: Boolean,
        userID: String?,
        type_name: String?,
        category: String?
    ) {
        val transaction: MutableMap<String, Any?> = HashMap()
        transaction["user"] = userID
        transaction["type"] = type_name
        transaction["amount"] = amount
        transaction["note"] = note
        transaction["category"] = category
        transaction["date"] =
            SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().time)

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


        if (frequent) {

            val frequentTransaction: MutableMap<String, Any?> = HashMap()
            frequentTransaction["user"] = userID
            frequentTransaction["type"] = type_name
            frequentTransaction["amount"] = amount
            frequentTransaction["note"] = note
            frequentTransaction["category"] = category

            db.collection("frequentTransactions")
                .add(frequentTransaction)
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
        }
    }
}