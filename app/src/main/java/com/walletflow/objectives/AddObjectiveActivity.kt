package com.walletflow.objectives

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.models.Objective
import com.walletflow.models.Participant
import com.walletflow.models.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddObjectiveActivity : BaseActivity() {

    private lateinit var btnSumbitObjective : Button
    private lateinit var etName : EditText
    private lateinit var etAmount : EditText
    private lateinit var etSelectDate: EditText
    private lateinit var etMyQuote : EditText
    private lateinit var tvMyUsername : TextView
    private lateinit var rvParticipants : RecyclerView
    private lateinit var selectedDate : Date
    private lateinit var participantsAdapter : ParticipantsAdapter
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Implementation for afterTextChanged
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Implementation for beforeTextChanged
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            btnSumbitObjective.isEnabled = etName.text.isNotEmpty() &&
                    etAmount.text.isNotEmpty() &&
                    etSelectDate.text.isNotEmpty()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnSumbitObjective = findViewById(R.id.btnSubmitNewObjective)
        btnSumbitObjective.isEnabled = false
        etAmount = findViewById(R.id.etObjectiveAmount)
        etAmount.addTextChangedListener(textWatcher)
        etName = findViewById(R.id.etObjectiveName)
        etName.addTextChangedListener(textWatcher)
        etSelectDate = findViewById(R.id.btnSelectDate)
        etSelectDate.addTextChangedListener(textWatcher)
        etMyQuote = findViewById(R.id.myQuote)
        tvMyUsername = findViewById(R.id.myUsername)
        rvParticipants = findViewById(R.id.friendRecycleView)

        val db = FirebaseFirestore.getInstance()
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")

        etAmount.setText(0.0.toString())

        val group = intent.getParcelableExtra("group", ArrayList::class.java)
        participantsAdapter = ParticipantsAdapter()
        rvParticipants.adapter = participantsAdapter

        if (group!=null){
            tvMyUsername.visibility = View.VISIBLE
            tvMyUsername.text = "Me"
            etMyQuote.visibility = View.VISIBLE
            etMyQuote.setText(0.0.toString())
            participantsAdapter.setParticipantsListFromUsers(group as ArrayList<User>)
        }

        etAmount.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val amount = etAmount.text.toString().toDouble()
                participantsAdapter.setQuotes(amount)
                etMyQuote.setText((amount/(participantsAdapter.itemCount+1)).toString())
            }
        }

        etSelectDate.setOnClickListener {
            showDatePicker()
        }

        btnSumbitObjective.setOnClickListener{
            etAmount.clearFocus()
            etMyQuote.clearFocus()

            btnSumbitObjective.requestFocus()
            if ((rvParticipants.adapter as ParticipantsAdapter).getTotalOfTheQuotes() +
                etMyQuote.text.toString().toDouble()
                == etAmount.text.toString().toDouble()
            ){
                val obj = Objective(etName.text.toString(),
                    etAmount.text.toString().toDouble(),
                    SimpleDateFormat("yyyy-MM-dd").format(selectedDate),
                    userID)
                saveObjective(obj, db)
                val intent = Intent(this, ObjectivesActivity::class.java)
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "The total of the quotes is not equal to the amount. This is wrong!",Toast.LENGTH_LONG).show()
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

    private fun saveObjective(
        obj : Objective,
        db: FirebaseFirestore
    ) {
        db.collection("objectives")
            .add(obj)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    this.localClassName,
                    "DocumentSnapshot added with ID: " + documentReference.id
                )
                saveParticipants(db, obj, documentReference)
            }
            .addOnFailureListener { e ->
                Log.w(
                    this.localClassName,
                    "Error adding document",
                    e
                )
            }
    }

    private fun saveParticipants(
        db: FirebaseFirestore,
        obj: Objective,
        documentReference: DocumentReference
    ) {
        val saveParticipantQueries = arrayListOf<Task<DocumentReference>>(
            db.collection("participants").add(
                Participant(
                    obj.admin,
                    etMyQuote.text.toString().toDouble(),
                    objectiveId = documentReference.id
                )
            )
        )
        (rvParticipants.adapter as ParticipantsAdapter).getParticipantsList().forEach { participant ->
            participant.objectiveId = documentReference.id
            saveParticipantQueries.add(db.collection("participants").add(participant))
        }

        Tasks.whenAllSuccess<QuerySnapshot>(saveParticipantQueries)
            .addOnSuccessListener {
                Log.d(this.localClassName, "All participants saved correctly")
            }
            .addOnFailureListener {
                Log.w(this.localClassName, "Problems with participants saving")
            }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_objective_add
    }
}