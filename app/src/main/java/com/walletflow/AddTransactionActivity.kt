package com.walletflow

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class AddTransactionActivity : AppCompatActivity() {

    lateinit var titleTv : TextView
    lateinit var subtitleTv : TextView
    lateinit var amountTitleTv : TextView
    lateinit var amountEditText : EditText
    lateinit var noteEditText : EditText
    lateinit var recurrentCheck : CheckBox
    lateinit var chooseCategoryBtn : Button
    lateinit var selectorRecurrent : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val type = intent.getIntExtra("type", 0)
        val typeName = intent.getStringExtra("type_name")
        val typeVerb = intent.getStringExtra("type_verb")
        titleTv = findViewById(R.id.tvTitle)
        subtitleTv = findViewById(R.id.tvSubTitle)
        amountTitleTv = findViewById(R.id.tvAmountTitle)
        amountEditText = findViewById(R.id.etAmount)
        noteEditText = findViewById(R.id.etNote)
        recurrentCheck = findViewById(R.id.cbRecurrent)
        chooseCategoryBtn = findViewById(R.id.btnChooseCategory)
        selectorRecurrent = findViewById(R.id.spRecurrent)

        titleTv.text = typeName?.uppercase()
        subtitleTv.append(typeName)
        amountTitleTv.append(typeVerb + "?")

        recurrentCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectorRecurrent.visibility = View.VISIBLE
            } else {
                selectorRecurrent.visibility = View.GONE
            }
        }

        chooseCategoryBtn.setOnClickListener {

            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userID = sharedPreferences.getString("userID", "")
            val amount = amountEditText.text.toString()
            val note = noteEditText.text.toString()
            val recurrent = recurrentCheck.isChecked
            var recurrency = "None"

            val intent = Intent(this, ChooseCategoryActivity::class.java)
            startActivity(intent)

            /*if(recurrent){
                recurrency = selectorRecurrent.selectedItem.toString()
            }

            if(amount.isEmpty()){
                Toast.makeText(this, "Please specify the amount", Toast.LENGTH_LONG).show()
            }
            else {
                val db = FirebaseFirestore.getInstance()
                addTransaction(db, amount.toFloat()*type, note, recurrency, userID, typeName)
            }*/

        }

    }

    private fun addTransaction(db : FirebaseFirestore, amount : Float, note : String, recurrency : String, userID : String?, type_name : String?){
        val transaction: MutableMap<String, Any?> = HashMap()
        transaction["user"] = userID
        transaction["type"] = type_name
        transaction["amount"] = amount
        transaction["recurrency"] = recurrency
        transaction["note"] = note

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