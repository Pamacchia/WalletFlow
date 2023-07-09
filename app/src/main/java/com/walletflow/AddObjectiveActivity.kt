package com.walletflow

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import androidx.core.view.get
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class AddObjectiveActivity : BaseActivity() {

    private lateinit var btnSumbitObjective : Button
    private lateinit var etName : EditText
    private lateinit var etAmount : EditText
    private lateinit var etAlreadySaved : EditText
    private lateinit var dpDate : DatePicker
    private lateinit var cbGroupObjective : CheckBox
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_objective)

        btnSumbitObjective = findViewById(R.id.btnSubmitNewObjective)
        etAmount = findViewById(R.id.etObjectiveAmount)
        etAlreadySaved = findViewById(R.id.etAlreadySaved)
        etName = findViewById(R.id.etObjectiveName)
        dpDate = findViewById(R.id.dpObjectiveExpiringDate)
        cbGroupObjective = findViewById(R.id.cbGroupObjective)

        btnSumbitObjective.setOnClickListener{

            val db = FirebaseFirestore.getInstance()
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userID = sharedPreferences.getString("userID", "")

            val amount = etAmount.text.toString().toFloat()
            val name = etName.text.toString()
            val alreadySaved = etAlreadySaved.text.toString().toFloat()
            val calendar = Calendar.getInstance()
            calendar.set(dpDate.year, dpDate.month, dpDate.dayOfMonth)
            val date = calendar.time
            val group = cbGroupObjective.isChecked

            saveObjective(userID, name, amount, alreadySaved, group, date, db)

            val intent = Intent(this, ObjectivesActivity::class.java)
            startActivity(intent)

        }
    }

    private fun saveObjective(
        userID: String?,
        name: String,
        amount: Float,
        alreadySaved: Float,
        group: Boolean,
        date: Date,
        db: FirebaseFirestore
    ) {
        val transaction: MutableMap<String, Any?> = HashMap()
        transaction["user"] = userID
        transaction["name"] = name
        transaction["amount"] = amount
        transaction["alreadySaved"] = alreadySaved
        transaction["group"] = group
        transaction["date"] = SimpleDateFormat("yyyy-MM-dd").format(date)

        db.collection("objectives")
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
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_add_objective
    }
}