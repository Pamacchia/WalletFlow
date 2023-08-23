package com.walletflow.objectives

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.data.Objective
import com.walletflow.data.Participant
import com.walletflow.transactions.ChooseCategoryActivity
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddObjectiveActivity : BaseActivity() {

    private val chooseCategoryRequestCode = 1
    private lateinit var btnSubmitObjective: Button
    private lateinit var etName: EditText
    private lateinit var etAmount: EditText
    private lateinit var etSelectDate: EditText
    private lateinit var selectedDate: Date
    private lateinit var friendQuotesLayout: LinearLayout

    private val textWatcher2 = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            btnSubmitObjective.isEnabled = etName.text.isNotEmpty()
                    && etAmount.text.isNotEmpty()
                    && etSelectDate.text.isNotEmpty() && addObjective()
        }

        override fun afterTextChanged(p0: Editable?) {
        }

    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (intent.getStringArrayListExtra("group") != null) {
                updateQuoteValues(intent.getStringArrayListExtra("group")!!.size)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
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

        btnSubmitObjective.isEnabled = false
        etName.addTextChangedListener(textWatcher2)
        etAmount.addTextChangedListener(textWatcher)
        etSelectDate.addTextChangedListener(textWatcher2)

        val group = intent.getStringArrayListExtra("group")

        etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && group != null) {
                updateQuoteValues(group.size)
            }
        }

        friendQuotesLayout = findViewById(R.id.friendQuotesLayout)

        if (group != null) {
            for (friend in group) {
                val friendQuoteView =
                    layoutInflater.inflate(R.layout.friend_quote_layout, null) as LinearLayout
                val friendUsernameTextView =
                    friendQuoteView.findViewById<TextView>(R.id.friendUsernameTextView)
                val friendQuoteEditText =
                    friendQuoteView.findViewById<EditText>(R.id.friendQuoteEditText)
                val factor: Float = this.resources.displayMetrics.density
                friendUsernameTextView.text = friend
                friendQuoteEditText.setText(0.0.toString())
                friendQuoteEditText.addTextChangedListener(textWatcher2)
                friendQuotesLayout.addView(friendQuoteView)
                (friendQuoteView.layoutParams as LinearLayout.LayoutParams).setMargins(
                    0,
                    (factor * 15).toInt(),
                    0,
                    0
                )

            }
        }

        etSelectDate.setOnClickListener {
            showDatePicker()
        }

        btnSubmitObjective.setOnClickListener {
            val intent = Intent(this, ChooseCategoryActivity::class.java)
            intent.putExtra("typeName", "expense")
            startActivityForResult(intent, chooseCategoryRequestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == chooseCategoryRequestCode && resultCode == Activity.RESULT_OK) {
            intentData?.let { data ->
                val obj = Objective(
                    etName.text.toString(),
                    etAmount.text.toString().toDouble(),
                    SimpleDateFormat("yyyy-MM-dd").format(selectedDate),
                    userID,
                    false,
                    data.getStringExtra("category")
                )
                saveObjective(obj, db)
                finish()
            }
        }
    }

    private fun addObjective(): Boolean {
        val group = intent.getStringArrayListExtra("group")
        val friendQuotesLayout = findViewById<LinearLayout>(R.id.friendQuotesLayout)
        var sumOfQuotes = 0.0
        if (group != null) {
            for (i in 0 until friendQuotesLayout.childCount) {
                val friendQuoteView = friendQuotesLayout.getChildAt(i) as LinearLayout
                val friendQuoteEditText =
                    friendQuoteView.findViewById<EditText>(R.id.friendQuoteEditText)
                val quoteValue = friendQuoteEditText.text.toString().toDoubleOrNull() ?: 0.0
                sumOfQuotes += quoteValue
            }
        }

        return sumOfQuotes == etAmount.text.toString().toDouble() || group == null

    }

    private fun showDatePicker() {

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
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

    private fun updateQuoteValues(numParticipants: Int) {
        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0


        val friendQuotesLayout = findViewById<LinearLayout>(R.id.friendQuotesLayout)
        var sum = 0.0.toBigDecimal()

        for (i in 1 until friendQuotesLayout.childCount) {
            val friendQuoteView = friendQuotesLayout.getChildAt(i) as LinearLayout
            val friendQuoteEditText =
                friendQuoteView.findViewById<EditText>(R.id.friendQuoteEditText)
            val newQuote =
                (amount / numParticipants).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
            sum += newQuote
            friendQuoteEditText.setText(newQuote.toString())
        }

        val friendQuoteView = friendQuotesLayout.getChildAt(0) as LinearLayout
        val friendQuoteEditText = friendQuoteView.findViewById<EditText>(R.id.friendQuoteEditText)
        val adminQuote = (amount.toBigDecimal() - sum)
        friendQuoteEditText.setText(adminQuote.toString())
    }

    private fun saveObjective(
        obj: Objective,
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

        if (friendQuotesLayout.childCount != 0) {
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