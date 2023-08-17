package com.walletflow.objectives

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.data.Objective
import com.walletflow.data.Participant

class ObjectivesActivity : BaseActivity() {

    lateinit var createNewObjective: Button
    lateinit var createNewGroupObjective: Button
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

    override fun onRestart() {
        super.onRestart()
        loadObjectives()
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_objective
    }

    private fun loadObjectives() {

        val rootView = findViewById<LinearLayout>(R.id.objectivesLayout)
        rootView.removeAllViews()

        db.collection("participants")
            .whereEqualTo("participant", userID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (participant in task.result) {

                        val currentUser = Participant(
                            participant.getString("objectiveId")!!,
                            participant.getString("participant")!!,
                            participant.getDouble("quote")!!,
                            participant.getDouble("saved")!!
                        )

                        val objectiveQuery =
                            db.collection("objectives").document(currentUser.objectiveId!!).get()
                        val otherParticipantsQuery = db.collection("participants")
                            .whereEqualTo("objectiveId", currentUser.objectiveId!!).get()

                        val cardView = LayoutInflater.from(this)
                            .inflate(R.layout.objective_cardview, rootView, false) as CardView
                        val tvTitle = cardView.findViewById<TextView>(R.id.tvObjectiveCardTitle)
                        val tvParticipants =
                            cardView.findViewById<TextView>(R.id.tvObjectiveCardParticipants)
                        val tvSavings =
                            cardView.findViewById<TextView>(R.id.tvObjectiveCardProgress)

                        objectiveQuery.addOnCompleteListener { objectiveQuery ->

                            otherParticipantsQuery.addOnSuccessListener { resultQueryList ->

                                val objective = Objective(
                                    objectiveQuery.result.getString("name")!!,
                                    objectiveQuery.result.getDouble("amount"),
                                    objectiveQuery.result.getString("date")!!,
                                    objectiveQuery.result.getString("admin")!!,
                                    objectiveQuery.result.getBoolean("completed"),
                                    objectiveQuery.result.getString("category")
                                )

                                val participantList =
                                    ArrayList<Participant>() // ArrayList to store Participant instances

                                for (otherParticipant in resultQueryList) {
                                    val tempParticipant = Participant(
                                        otherParticipant.getString("objectiveId")!!,
                                        otherParticipant.getString("participant")!!,
                                        otherParticipant.getDouble("quote")!!,
                                        otherParticipant.getDouble("saved")!!
                                    )
                                    participantList.add(tempParticipant)
                                }

                                tvTitle.text = objective.name + " | Exp: " + objective.date

                                if (participantList.size > 1) {
                                    tvParticipants.text = "Group Objective"
                                } else {
                                    tvParticipants.text = "Solo Objective"
                                }

                                tvSavings.text =
                                    "You saved ${currentUser.saved} out of ${currentUser.quote}"
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
                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }
    }


}