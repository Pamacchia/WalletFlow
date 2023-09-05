package com.walletflow.objectives

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.card.MaterialCardView
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.data.Objective
import com.walletflow.data.Participant
import com.walletflow.utils.StringHelper
import java.text.SimpleDateFormat
import java.util.Calendar

class ObjectivesActivity : BaseActivity() {

    private lateinit var createNewObjective: Button
    private lateinit var createNewGroupObjective: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNewObjective = findViewById(R.id.btnAddNewObjective)
        createNewGroupObjective = findViewById(R.id.btnAddNewGroupObjective)

        loadObjectives()

        createNewObjective.setOnClickListener {
            val intent = Intent(this, AddObjectiveActivity::class.java)
            startActivity(intent)
        }

        createNewGroupObjective.setOnClickListener {
            val intent = Intent(this, AddFriendsToObjectiveActivity::class.java)
            startActivity(intent)
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_objective
    }

    private fun loadObjectives() {

        val rootView = findViewById<LinearLayout>(R.id.objectivesLayout)

        db.collection("participants").whereEqualTo("participant", userID)
            .orderBy("date")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                querySnapshot?.let {
                    rootView.removeAllViews()
                    it.documents.forEach { participant ->
                        val currentUser = participant.toObject(Participant::class.java)

                        val userObjectiveQuery =
                            db.collection("objectives").document(currentUser!!.objectiveId).get()
                        val otherParticipantsQuery = db.collection("participants")
                            .whereEqualTo("objectiveId", currentUser.objectiveId).get()

                        // Initialize objective card
                        val cardView = LayoutInflater.from(this).inflate(
                                R.layout.objective_cardview,
                                rootView,
                                false
                            ) as MaterialCardView
                        val tvTitle = cardView.findViewById<TextView>(R.id.tvObjectiveCardTitle)
                        val tvParticipants =
                            cardView.findViewById<TextView>(R.id.tvObjectiveCardParticipants)
                        val tvSavings =
                            cardView.findViewById<TextView>(R.id.tvObjectiveCardProgress)

                        userObjectiveQuery.addOnCompleteListener { objectiveQuery ->

                            otherParticipantsQuery.addOnSuccessListener { resultQueryList ->
                                val objective = Objective(
                                    objectiveQuery.result.getString("name")!!,
                                    objectiveQuery.result.getDouble("amount"),
                                    objectiveQuery.result.getString("date")!!,
                                    objectiveQuery.result.getString("admin")!!,
                                    objectiveQuery.result.getBoolean("completed"),
                                    objectiveQuery.result.getString("category")
                                )

                                Log.d(this.localClassName,"Start:" + objective.date)
                                val participantList = ArrayList<Participant>()

                                for (otherParticipant in resultQueryList) {
                                    val tempParticipant = otherParticipant.toObject(Participant::class.java)
                                    participantList.add(tempParticipant)
                                }

                                associateColorToExpirationDate(objective, cardView)

                                // Initialize card properties
                                tvTitle.text = objective.name + " | Exp: " + objective.date

                                if (participantList.size > 1) {
                                    tvParticipants.text = "Group Objective"
                                } else {
                                    tvParticipants.text = "Solo Objective"
                                }
                                tvSavings.text =
                                    "You saved ${StringHelper.getShrunkForm(currentUser!!.saved)}€ out of ${StringHelper.getShrunkForm(currentUser.quote)}€"

                                Log.d(this.localClassName,"End:" + objective.date)
                                rootView.addView(cardView)

                                cardView.setOnClickListener {
                                    val intent = Intent(this, ObjectiveDetailActivity::class.java)
                                    intent.putExtra("objective", objective)
                                    intent.putExtra("participants", participantList)
                                    intent.putExtra("currentUser", currentUser)
                                    startActivity(intent)
                                }
                            }.addOnFailureListener {
                                Log.w(this.localClassName, "QueryError")
                            }

                        }

                    }
                }
            }
    }

    private fun associateColorToExpirationDate(
        objective: Objective,
        cardView: MaterialCardView
    ) {
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, 3)
        val date2 = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)

        if (objective.date < date) {
            cardView.strokeColor = resources.getColor(R.color.nordRed)
        } else if (objective.date < date2) {
            cardView.strokeColor = resources.getColor(R.color.nordOrange)
        }
    }


}