package com.walletflow.objectives

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
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

    private lateinit var btnSubmitObjective : Button
    private lateinit var etName : EditText
    private lateinit var etAmount : EditText
    private lateinit var etSelectDate: EditText
    private lateinit var cbGroupObjective : CheckBox

    private lateinit var selectedDate : Date

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            TODO("Not yet implemented")
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            TODO("Not yet implemented")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){
            btnSubmitObjective.isEnabled = etName.text.isNotEmpty()
                    && etAmount.text.isNotEmpty()
                    && etSelectDate.text.isNotEmpty()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnSubmitObjective = findViewById(R.id.btnSubmitNewObjective)
        etAmount = findViewById(R.id.etObjectiveAmount)
        etName = findViewById(R.id.etObjectiveName)
        etSelectDate = findViewById(R.id.etSelectDate)

        etAmount.setText(0.0.toString())

        val group = intent.getStringArrayExtra("group")

        etAmount.setOnFocusChangeListener()

        btnSubmitObjective.setOnClickListener{

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