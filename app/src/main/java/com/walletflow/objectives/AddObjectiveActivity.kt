package com.walletflow.objectives

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.BaseActivity
import com.walletflow.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddObjectiveActivity : BaseActivity() {

    private lateinit var btnSumbitObjective : Button
    private lateinit var etName : EditText
    private lateinit var etAmount : EditText
    private lateinit var cbGroupObjective : CheckBox
    private lateinit var btnSelectDate: Button
    private lateinit var tvSelectedDate: TextView

    private lateinit var selectedDate : Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnSumbitObjective = findViewById(R.id.btnSubmitNewObjective)
        etAmount = findViewById(R.id.etObjectiveAmount)
        etName = findViewById(R.id.etObjectiveName)
        cbGroupObjective = findViewById(R.id.cbGroupObjective)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)

        btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        btnSumbitObjective.setOnClickListener{

            val db = FirebaseFirestore.getInstance()
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userID = sharedPreferences.getString("userID", "")
            val name = etName.text.toString()
            val amount = etAmount.text.toString().toFloat()
            val group = cbGroupObjective.isChecked
            val date = selectedDate

            saveObjective(userID, name, amount, group, date, db)

            val intent = Intent(this, ObjectivesActivity::class.java)
            startActivity(intent)

        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setStart(calendar.timeInMillis)
                    .build()
            )
            .build()

        datePicker.addOnPositiveButtonClickListener(
            MaterialPickerOnPositiveButtonClickListener<Long> { selection ->
                selectedDate = Date(selection)
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = format.format(selectedDate)
                tvSelectedDate.text = formattedDate
            })

        datePicker.show(supportFragmentManager, datePicker.toString())
    }

    private fun saveObjective(
        userID: String?,
        name: String,
        amount: Float,
        group: Boolean,
        date: Date,
        db: FirebaseFirestore
    ) {
        val transaction: MutableMap<String, Any?> = HashMap()
        transaction["admin"] = userID
        transaction["name"] = name
        transaction["amount"] = amount
        transaction["saved"] = 0
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
        return R.layout.activity_objective_add
    }
}