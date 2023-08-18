package com.walletflow.objectives

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.data.Objective
import com.walletflow.data.Participant
import com.walletflow.data.Transaction
import com.walletflow.utils.TransactionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.roundToInt

class ObjectiveDetailActivity : BaseActivity() {

    lateinit var titleTv: TextView
    lateinit var completedBtn: Button
    lateinit var addSavingsBtn: Button
    lateinit var deleteObjBtn: Button
    lateinit var addSavingsEt: EditText
    lateinit var objectiveBudgetTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val objective = intent.getSerializableExtra("objective") as Objective
        val friends = intent.getSerializableExtra("participants") as ArrayList<Participant>?
        val currentUser = intent.getSerializableExtra("currentUser") as Participant?

        titleTv = findViewById(R.id.tvObjectiveTitle)
        completedBtn = findViewById(R.id.btnCompletedObjective)
        deleteObjBtn = findViewById(R.id.btnDeleteObjective)
        addSavingsBtn = findViewById(R.id.btnAddSavings)
        addSavingsEt = findViewById(R.id.etAddSavings)
        objectiveBudgetTv = findViewById(R.id.tvObjectiveBudget)

        titleTv.text = "${objective.name}"
        var totalSaved = totalRecapInit(currentUser, friends, objective)
        loadParticipantInformation(currentUser, friends, totalSaved, objective)

        addSavingsBtn.setOnClickListener {
            var amount = addSavingsEt.text.toString().toDouble()
            if (amount <= (currentUser!!.quote - currentUser.saved) && amount > 0) {
                amount = ((amount * 100).roundToInt() / 100.0)
                currentUser.saved = currentUser.saved?.plus(amount)!!

                db.collection("participants")
                    .whereEqualTo("objectiveId", currentUser.objectiveId)
                    .whereEqualTo("participant", currentUser.participant)
                    .get()
                    .addOnSuccessListener { task ->
                        task.documents.first().reference.update("saved", currentUser.saved)
                        TransactionManager.updateBalance(db, -amount.toFloat(), userID)

                        totalSaved = totalRecapInit(currentUser, friends, objective)
                        loadParticipantInformation(currentUser, friends, totalSaved, objective)

                        if (totalSaved == objective.amount) {
                            completedBtn.isEnabled = true
                        }
                    }
            } else {
                Toast.makeText(this, "Invalid amount.", Toast.LENGTH_LONG).show()
            }
        }

        completedBtn.setOnClickListener {
            db.collection("participants")
                .whereEqualTo("objectiveId",currentUser!!.objectiveId)
                .get()
                .addOnSuccessListener { task ->
                    val objectiveRef = db.collection("objectives")
                        .document(currentUser!!.objectiveId)

                    objectiveRef.get().addOnSuccessListener { obj->
                        obj.toObject(Objective::class.java)
                        for (document in task.documents){
                            val participant = document.toObject(Participant::class.java)
                            val userTransaction = Transaction(
                                -participant!!.quote,
                                objective!!.category,
                                objective!!.name,
                                "expense",
                                participant!!.participant,
                                SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().time))

                            db.collection("transactions")
                                .add(userTransaction)
                                .addOnSuccessListener { documentReference ->
                                    Log.d(
                                        this.localClassName,
                                        "DocumentSnapshot added with ID: " + documentReference.id
                                    )
                                    document.reference.delete().addOnSuccessListener {
                                        Log.d(
                                            this.localClassName,
                                            "Deleted participant"
                                        )
                                    }. addOnFailureListener{
                                        Log.w(
                                            this.localClassName,
                                            "Error deleting participant"
                                        )
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        this.localClassName,
                                        "Error adding document",
                                        e
                                    )
                                }
                        }
                        objectiveRef.delete().addOnSuccessListener {
                            finish()
                        }
                    }

                }
        }

        deleteObjBtn.setOnClickListener {

            db.collection("objectives")
                .document(currentUser!!.objectiveId)
                .delete()
                .addOnSuccessListener {
                    Log.d(this.localClassName, "DocumentSnapshot successfully deleted!")

                    // Delete participants with the same objectiveId
                    db.collection("participants")
                        .whereEqualTo("objectiveId", currentUser!!.objectiveId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val batch = db.batch()
                            for (document in querySnapshot) {
                                Log.w(
                                    this.localClassName,
                                    document.getString("participants").toString()
                                )
                                TransactionManager.updateBalance(
                                    db,
                                    document.getDouble("saved")!!.toFloat(),
                                    document.getString("participant")
                                )
                                val participantRef =
                                    db.collection("participants").document(document.id)
                                batch.delete(participantRef)
                            }

                            batch.commit()
                                .addOnSuccessListener {
                                    Log.d(this.localClassName, "Participants deleted successfully!")
                                    Thread.sleep(150L)
                                    finish()
                                    overridePendingTransition(0, 0)
                                }
                                .addOnFailureListener { e ->
                                    Log.w(this.localClassName, "Error deleting participants", e)
                                }

                        }
                        .addOnFailureListener { e ->
                            Log.w(this.localClassName, "Error querying participants", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w(this.localClassName, "Error deleting document", e)
                }
        }

    }

    private fun loadParticipantInformation(
        currentUser: Participant?,
        friends: ArrayList<Participant>?,
        totalSaved: Double?,
        objective: Objective
    ) {
        val rootView = findViewById<LinearLayout>(R.id.participantsListLayout)
        rootView.removeAllViews()

        for (participant in friends!!) {
            val cardView = LayoutInflater.from(this)
                .inflate(R.layout.participant_detail_item, rootView, false) as CardView
            val usernameTv = cardView.findViewById<TextView>(R.id.tvUsername)
            val savingsTv = cardView.findViewById<TextView>(R.id.tvSavings)

            if (participant.participant == currentUser!!.participant) {
                usernameTv.text = currentUser.participant
                savingsTv.text = "Has saved ${currentUser.saved}$ over ${currentUser.quote}$"

            } else {
                usernameTv.text = participant.participant
                savingsTv.text = "Has saved ${participant.saved}$ over ${participant.quote}$"
            }

            rootView.addView(cardView)
        }

        if (totalSaved == objective.amount) {
            completedBtn.isEnabled = true
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_objective_detail
    }

    private fun totalRecapInit(
        currentUser: Participant?,
        friends: ArrayList<Participant>?,
        objective: Objective
    ): Double? {

        var totalSaved = 0.0

        friends!!.forEach { friend ->
            if (friend.participant == currentUser!!.participant) {
                totalSaved = currentUser!!.saved?.let { totalSaved?.plus(it) }!!
            } else {
                totalSaved = friend!!.saved?.let { totalSaved?.plus(it) }!!
            }
        }

        objectiveBudgetTv.text = " ${totalSaved}$/${objective.amount}$"
        val objectiveProgressBar = findViewById<FrameLayout>(R.id.objectiveProgressBar)
        val difference = kotlin.math.abs(objective.amount!! - totalSaved!!)
        val invRelativeDifference = difference / objective.amount
        val desiredWidthInDp = 325
        val minProgressBarWidthInPx = 1
        val relativeDifference = 1 - invRelativeDifference
        val newWidthInPx =
            (minProgressBarWidthInPx + (relativeDifference * (desiredWidthInDp - minProgressBarWidthInPx)) * resources.displayMetrics.density).toInt()
        val layoutParams = objectiveProgressBar.layoutParams
        layoutParams.width = newWidthInPx
        objectiveProgressBar.layoutParams = layoutParams
        return totalSaved
    }
}