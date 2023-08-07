package com.walletflow.objectives

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.data.Objective
import com.walletflow.data.Participant

class ObjectivesActivity : BaseActivity() {

    lateinit var createNewObjective : Button
    lateinit var createNewGroupObjective : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNewObjective = findViewById(R.id.btnAddNewObjectives)
        createNewGroupObjective = findViewById(R.id.btnAddNewGroupObjectives)
        val db = FirebaseFirestore.getInstance()
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")
        loadObjectives(db, userID!!)

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

    private fun loadObjectives(db : FirebaseFirestore, userID : String) {

        val rootView = findViewById<LinearLayout>(R.id.objectivesLayout)
        rootView.removeAllViews()

        db.collection("participants")
            .whereEqualTo("participant", userID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for(participant in task.result){
                        val currentUser = participant.toObject(Participant::class.java)
                        val objectiveQuery = db.collection("objectives").document(currentUser.objectiveId!!).get()
                        val otherParticipantsQuery = db.collection("participants").whereEqualTo("objectiveId", currentUser.objectiveId!!).get()

                        val cardView = LayoutInflater.from(this).inflate(R.layout.objective_cardview, rootView, false) as CardView
                        val tvTitle = cardView.findViewById<TextView>(R.id.tvObjectiveCardTitle)
                        val tvParticipants = cardView.findViewById<TextView>(R.id.tvObjectiveCardParticipants)
                        val tvSavings = cardView.findViewById<TextView>(R.id.tvObjectiveCardProgress)

                        objectiveQuery.addOnCompleteListener { objectiveQuery ->
                            otherParticipantsQuery.addOnSuccessListener { resultQueryList ->
                                val objective = objectiveQuery.result.toObject(Objective::class.java)
                                val otherParticipants = resultQueryList.toObjects(Participant::class.java)

                                tvTitle.text = objective!!.name + "   |   " + objective.date
                                tvParticipants.text = "|"
                                otherParticipants.forEach{ participant ->
                                    if (participant.participant!=currentUser.participant){
                                        tvParticipants.append(
                                            " ${participant.participant} |"
                                        )
                                    }
                                }
                                tvSavings.text = "You saved ${currentUser.saved}$ out of ${objective.amount}$"
                                rootView.addView(cardView)

                                cardView.setOnClickListener{
                                otherParticipants.remove(currentUser)
                                val intent = Intent(this, ObjectiveDetailActivity::class.java)
                                intent.putExtra("objective", objective)
                                intent.putExtra("participants", otherParticipants as ArrayList<Participant>)
                                intent.putExtra("currentUser", currentUser)
                                startActivity(intent)
                                }
                            }
                                .addOnFailureListener {
                                    Log.w(this.localClassName, "Or request query error")
                                }

                        }
                    }
                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }
    }


}