package com.walletflow.transactions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.utils.TransactionManager
import java.text.SimpleDateFormat
import java.util.Calendar

class AddTransactionActivity : BaseActivity() {

    private val chooseCategoryRequestCode = 1
    private lateinit var introDateTextView : TextView
    lateinit var amountEditText: EditText
    private lateinit var noteEditText: EditText
    private lateinit var frequentCheck: CheckBox
    lateinit var chooseCategoryBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        amountEditText = findViewById(R.id.etAmount)
        noteEditText = findViewById(R.id.etNote)
        frequentCheck = findViewById(R.id.cbFrequent)
        chooseCategoryBtn = findViewById(R.id.btnChooseCategory)
        introDateTextView = findViewById(R.id.tvIntroDate)

        amountEditText.addTextChangedListener(textWatcher)
        chooseCategoryBtn.isEnabled = amountEditText.text.isNotEmpty()

        introDateTextView.append(SimpleDateFormat("EEEE, d MMMM").format(Calendar.getInstance().time))

        chooseCategoryBtn.setOnClickListener {

            val amount = amountEditText.text.toString()
            val note = noteEditText.text.toString()
            val frequent = frequentCheck.isChecked

            if (amount.isEmpty()) {
                Toast.makeText(this, "Please specify the amount", Toast.LENGTH_LONG).show()
            } else if(note.isEmpty() && frequent) {
                Toast.makeText(this, "Please add a note for frequent transactions", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, ChooseCategoryActivity::class.java)
                intent.putExtra("typeName", this.intent.getStringExtra("type_name"))
                startActivityForResult(intent, chooseCategoryRequestCode)
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        val amount = amountEditText.text.toString()
        val note = noteEditText.text.toString()
        val frequent = frequentCheck.isChecked
        val type = intent.getIntExtra("type", 0)
        val typeName = intent.getStringExtra("type_name")

        if (requestCode == chooseCategoryRequestCode && resultCode == Activity.RESULT_OK) {
            intentData?.let { data ->

                addTransaction(
                    db, amount.toFloat() * type,
                    note, frequent, userID, typeName, data.getStringExtra("category")
                )

                TransactionManager.updateBalance(
                    db,
                    amount.toFloat() * type,
                    userID
                )
                finish()
            }
        }
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

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_transaction_add
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            chooseCategoryBtn.isEnabled = amountEditText.text.isNotEmpty()
        }
    }

}