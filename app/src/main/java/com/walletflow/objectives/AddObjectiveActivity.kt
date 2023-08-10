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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.data.Objective
import com.walletflow.data.Participant
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddObjectiveActivity : BaseActivity() {

    private lateinit var btnSubmitObjective : Button
    private lateinit var etName : EditText
    private lateinit var etAmount : EditText
    private lateinit var etSelectDate: EditText
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

        val group = intent.getStringArrayListExtra("group")

        etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && group!=null) {
                updateQuoteValues(group!!.size)
            }
        }

        val friendQuotesLayout = findViewById<LinearLayout>(R.id.friendQuotesLayout)

        if(group!=null) {
            for (friend in group!!) {
                val friendQuoteView =
                    layoutInflater.inflate(R.layout.friend_quote_layout, null) as LinearLayout
                val friendUsernameTextView =
                    friendQuoteView.findViewById<TextView>(R.id.friendUsernameTextView)
                friendUsernameTextView.text = friend
                friendQuotesLayout.addView(friendQuoteView)
            }
        }

        etSelectDate.setOnClickListener{
            showDatePicker()
        }

        btnSubmitObjective.setOnClickListener{

            val friendQuotesLayout = findViewById<LinearLayout>(R.id.friendQuotesLayout)
            var sumOfQuotes = 0.0
            if(group!=null) {
                for (i in 0 until friendQuotesLayout.childCount) {
                    val friendQuoteView = friendQuotesLayout.getChildAt(i) as LinearLayout
                    val friendQuoteEditText =
                        friendQuoteView.findViewById<EditText>(R.id.friendQuoteEditText)
                    val quoteValue = friendQuoteEditText.text.toString().toDoubleOrNull() ?: 0.0
                    sumOfQuotes += quoteValue
                }
            }

            // Compare the sum of quote values with the original amount
            if (sumOfQuotes == etAmount.text.toString().toDouble() || group==null) {

                val obj = Objective(
                    etName.text.toString(),
                    etAmount.text.toString().toDouble(),
                    SimpleDateFormat("yyyy-MM-dd").format(selectedDate),
                    userID,
                    false
                )

                saveObjective(obj, db)

                val intent = Intent(this, ObjectivesActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Quote values do not match the amount.", Toast.LENGTH_SHORT).show()
            }
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

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDate = Date(selection)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = format.format(selectedDate)
            etSelectDate.setText(formattedDate)
        }

        datePicker.show(supportFragmentManager, datePicker.toString())
    }

    private fun updateQuoteValues(numParticipants : Int) {
        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0


        val friendQuotesLayout = findViewById<LinearLayout>(R.id.friendQuotesLayout)

        for (i in 0 until friendQuotesLayout.childCount) {
            val friendQuoteView = friendQuotesLayout.getChildAt(i) as LinearLayout
            val friendQuoteEditText = friendQuoteView.findViewById<EditText>(R.id.friendQuoteEditText)
            val newQuote = (amount / numParticipants).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
            friendQuoteEditText.setText(newQuote.toString())
        }
    }

    private fun saveObjective(
        obj : Objective,
        db: FirebaseFirestore
    ) {
        db.collection("objectives")
            .add(obj)
            .addOnSuccessListener { documentReference ->
                saveParticipants(db, documentReference)
            }
    }

    private fun saveParticipants(
        db: FirebaseFirestore,
        documentReference: DocumentReference
    ) {
        val friendQuotesLayout = findViewById<LinearLayout>(R.id.friendQuotesLayout)

        if(friendQuotesLayout.childCount != 0) {
            for (i in 0 until friendQuotesLayout.childCount) {
                val friendQuoteView = friendQuotesLayout.getChildAt(i) as LinearLayout
                db.collection("participants").add(
                    Participant(
                        documentReference.id,
                        friendQuoteView.findViewById<TextView>(R.id.friendUsernameTextView).text.toString(),
                        friendQuoteView.findViewById<EditText>(R.id.friendQuoteEditText).text.toString()
                            .toDouble(),
                        0.0
                    ).toMap()
                )
            }
        } else {
            db.collection("participants").add(
                Participant(
                    documentReference.id,
                    userID,
                    etAmount.text.toString().toDouble(),
                    0.0
                ).toMap()
            )
        }

    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_objective_add
    }
}