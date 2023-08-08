package com.walletflow.objectives

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.data.Objective
import com.walletflow.data.Participant
import java.lang.Math.round
import java.util.ArrayList
import kotlin.math.roundToInt

class ObjectiveDetailActivity : BaseActivity() {

    lateinit var titleTv: TextView
    lateinit var recapTv: TextView
    lateinit var etSavings: EditText
    lateinit var addSavingsBtn: Button
    lateinit var btnDelete: Button
    lateinit var btnCompleted: Button
    lateinit var tvMyRecap: TextView
    lateinit var rvFriends: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        val objective = intent.getParcelableExtra<Objective>("objective")
        val friends = intent.getParcelableArrayListExtra<Participant>("participants")
        val currentUser = intent.getParcelableExtra<Participant>("currentUser")

        titleTv = findViewById(R.id.tvObjectiveTitle)
        recapTv = findViewById(R.id.tvObjectiveRecap)
        addSavingsBtn = findViewById(R.id.btnAddSavings)
        etSavings = findViewById(R.id.etSaving)
        tvMyRecap = findViewById(R.id.tvMyRecap)
        rvFriends = findViewById(R.id.rvObjectiveDetail)
        rvFriends.adapter = ObjectiveDetailFriendsAdapter(friends!!)
        btnCompleted = findViewById(R.id.btnCompletedObjective)
        if (friends.isNotEmpty()) tvMyRecap.visibility = View.VISIBLE


        titleTv.text = "${objective!!.name} detail"
        val totalSaved = totalRecapInit(currentUser, friends, objective)
        tvMyRecap.text =
            "You have currently saved ${currentUser!!.saved} over ${currentUser!!.quote}"

        if (totalSaved == objective.amount) {
            btnCompleted.isEnabled = true
        }

        addSavingsBtn.setOnClickListener {
            var amount = etSavings.text.toString().toDouble()
            if (amount <= currentUser.quote && amount > 0) {
                amount = ((amount * 100.0).roundToInt() / 100.0)
                currentUser.saved = currentUser.saved?.plus(amount)
                tvMyRecap.text =
                    "You have currently saved ${currentUser.saved}$ over ${currentUser.quote}$"
                db.collection("participants").whereEqualTo("participant", currentUser.participant)
                    .whereEqualTo("objectiveId", currentUser.objectiveId).get()
                    .addOnSuccessListener { task ->
                        Log.d(this.localClassName, "currentUser document found successfully!")
                        task.documents.first().reference.update("saved", currentUser.saved)
                            .addOnSuccessListener {
                                Log.d(this.localClassName, "savings updated ok!")
                            }.addOnFailureListener {
                            Log.w(this.localClassName, "problems updating savings!")
                        }
                    }.addOnFailureListener {
                        Log.w(this.localClassName, "problems finding the currentUser document!")
                    }
                totalRecapInit(currentUser, friends, objective)
                if (totalSaved == objective.amount) {
                    btnCompleted.isEnabled = true
                }
            } else {
                Toast.makeText(
                    this,
                    "The saving need to be lower than the quote and the balance!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun totalRecapInit(
        currentUser: Participant?,
        friends: ArrayList<Participant>?,
        objective: Objective
    ): Double? {
        var totalSaved = currentUser!!.saved
        friends!!.forEach { friend ->
            totalSaved = friend!!.saved?.let { totalSaved?.plus(it) }
        }
        recapTv.text = "$totalSaved$ saved of ${objective.amount}$"
        return totalSaved
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_objective_detail
    }
}