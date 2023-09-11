package com.walletflow.objectives

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputFilter
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
import com.walletflow.utils.DecimalDigitsInputFilter
import com.walletflow.utils.StringHelper
import com.walletflow.utils.TransactionManager
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt


class ObjectiveDetailActivity : BaseActivity() {

    private lateinit var titleTv: TextView
    private lateinit var completedBtn: Button
    private lateinit var addSavingsBtn: Button
    private lateinit var deleteObjBtn: Button
    private lateinit var addSavingsEt: EditText
    private lateinit var objectiveBudgetTv: TextView
    private var totalSaved: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val objective = intent.getSerializableExtra("objective") as Objective
        val currentUser = intent.getSerializableExtra("currentUser") as Participant?

        titleTv = findViewById(R.id.tvObjectiveTitle)
        completedBtn = findViewById(R.id.btnCompletedObjective)
        deleteObjBtn = findViewById(R.id.btnDeleteObjective)
        addSavingsBtn = findViewById(R.id.btnAddSavings)
        addSavingsEt = findViewById(R.id.etAddSavings)
        addSavingsEt.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(2))
        objectiveBudgetTv = findViewById(R.id.tvObjectiveBudget)

        titleTv.text = objective.name
        totalRecapInit(currentUser, objective)

        addSavingsBtn.setOnClickListener {
            var amount = addSavingsEt.text.toString().toDouble()
            if (
                (amount > 0 && amount.toBigDecimal() <= (currentUser!!.quote.toBigDecimal() - currentUser.saved.toBigDecimal())) ||
                (amount < 0 && abs(amount).toBigDecimal() <= currentUser!!.saved.toBigDecimal())
            ) {
                updateUserSavings(amount.toBigDecimal(), currentUser)
            } else {
                Toast.makeText(this, "Invalid amount.", Toast.LENGTH_LONG).show()
            }
        }

        completedBtn.setOnClickListener {
            completeObjective(currentUser, objective)
        }

        deleteObjBtn.setOnClickListener {
            createAlertForObjective(currentUser, "Confirm deleting operation")
        }
    }

    private fun totalRecapInit(
        currentUser: Participant?, objective: Objective
    ) {

        db.collection("participants").whereEqualTo("objectiveId", currentUser!!.objectiveId)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                querySnapshot?.let {
                    totalSaved = 0.0
                    val friends = it.toObjects(Participant::class.java)
                    friends.forEach { friend ->
                        totalSaved = if (friend.participant == currentUser.participant) {
                            currentUser.saved.let { x -> totalSaved?.plus(x) }!!
                        } else {
                            friend!!.saved.let { x -> totalSaved?.plus(x) }!!
                        }
                    }
                    objectiveBudgetTv.text = " ${StringHelper.getShrunkForm(totalSaved!!)}€/${StringHelper.getShrunkForm(objective.amount!!)}€"
                    showProgressBar(objective, objective.amount)
                    loadParticipantInformation(currentUser, friends, totalSaved, objective)
                }
            }
    }

    private fun showProgressBar(objective: Objective, amount: Double) {
        val objectiveProgressBar = findViewById<FrameLayout>(R.id.objectiveProgressBar)
        val difference = abs(objective.amount!! - totalSaved!!)
        val invRelativeDifference = difference / amount
        val desiredWidthInDp = 325
        val minProgressBarWidthInPx = 1
        val relativeDifference = 1 - invRelativeDifference.toDouble()
        val newWidthInPx =
            (minProgressBarWidthInPx + (relativeDifference * (desiredWidthInDp - minProgressBarWidthInPx)) * resources.displayMetrics.density).toInt()
        val layoutParams = objectiveProgressBar.layoutParams
        layoutParams.width = newWidthInPx
        objectiveProgressBar.layoutParams = layoutParams
    }

    private fun createAlertForObjective(
        currentUser: Participant?,
        message: String
    ) {
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle(message)
        alert.setMessage("Are you sure?")
        alert.setPositiveButton("Yes") { _, _ ->
            deleteObjective(currentUser)
        }
        alert.setNegativeButton("No") { dialog, _ ->
            dialog.cancel()
        }
        alert.show()
    }
    private fun deleteObjective(currentUser: Participant?) {
        db.collection("objectives").document(currentUser!!.objectiveId).delete()
            .addOnSuccessListener {
                Log.d(this.localClassName, "DocumentSnapshot successfully deleted!")

                db.collection("participants")
                    .whereEqualTo("objectiveId", currentUser.objectiveId).get()
                    .addOnSuccessListener { querySnapshot ->
                        val batch = db.batch()
                        for (document in querySnapshot) {
                            TransactionManager.updateBalance(
                                db,
                                document.getDouble("saved")!!.toFloat(),
                                document.getString("participant")
                            )
                            val participantRef =
                                db.collection("participants").document(document.id)
                            batch.delete(participantRef)
                        }

                        batch.commit().addOnSuccessListener {
                            finish()
                            overridePendingTransition(0, 0)
                        }

                    }
            }
    }

    private fun completeObjective(
        currentUser: Participant?,
        objective: Objective
    ) {
        db.collection("participants").whereEqualTo("objectiveId", currentUser!!.objectiveId)
            .get().addOnSuccessListener { task ->
                val objectiveRef = db.collection("objectives").document(currentUser.objectiveId)

                objectiveRef.get().addOnSuccessListener { obj ->
                    obj.toObject(Objective::class.java)
                    for (document in task.documents) {
                        val participant = document.toObject(Participant::class.java)

                        val userTransaction = Transaction(
                            -participant!!.quote,
                            objective.category,
                            objective.name,
                            "expense",
                            participant.participant,
                            SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().time)
                        )

                        db.collection("transactions").add(userTransaction)
                            .addOnSuccessListener {
                                document.reference.delete()
                            }

                    }
                    objectiveRef.delete().addOnSuccessListener {
                        finish()
                    }
                }

            }
    }

    private fun updateUserSavings(userAmount: BigDecimal, currentUser: Participant) {
        var amount = userAmount
        currentUser.saved = (currentUser.saved.toBigDecimal() + amount).toDouble()

        db.collection("participants").whereEqualTo("objectiveId", currentUser.objectiveId)
            .whereEqualTo("participant", currentUser.participant).get()
            .addOnSuccessListener { task ->
                task.documents.first().reference.update("saved", currentUser.saved)
                TransactionManager.updateBalance(db, -amount.toFloat(), userID)
            }
    }

    private fun loadParticipantInformation(
        currentUser: Participant?,
        friends: MutableList<Participant>?,
        totalSaved: Double?,
        objective: Objective
    ) {
        val rootView = findViewById<LinearLayout>(R.id.participantsListLayout)
        rootView.removeAllViews()

        for (participant in friends!!) {

            // Initialize participant card
            val cardView = LayoutInflater.from(this)
                .inflate(R.layout.participant_detail_item, rootView, false) as CardView
            val usernameTv = cardView.findViewById<TextView>(R.id.tvUsername)
            val savingsTv = cardView.findViewById<TextView>(R.id.tvSavings)

            if (participant.participant == currentUser!!.participant) {
                usernameTv.text = currentUser.participant
                savingsTv.text = "Has saved ${StringHelper.getShrunkForm(currentUser.saved)}€ over ${StringHelper.getShrunkForm(currentUser.quote)}€"

            } else {
                usernameTv.text = participant.participant
                savingsTv.text = "Has saved ${StringHelper.getShrunkForm(participant.saved)}€ over ${StringHelper.getShrunkForm(participant.quote)}€"
            }

            if (participant.saved == participant.quote) {
                usernameTv.append(" \uD83E\uDD47")
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
}