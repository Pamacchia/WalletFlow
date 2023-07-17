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

class ObjectivesActivity : BaseActivity() {

    lateinit var createNewObjective : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNewObjective = findViewById(R.id.btnAddNewObjectives)

        loadObjectives()

        createNewObjective.setOnClickListener {

            val intent = Intent(this, AddObjectiveActivity::class.java)
            startActivity(intent)
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_objective
    }

    fun loadObjectives() {

        val rootView = findViewById<LinearLayout>(R.id.objectivesLayout)
        rootView.removeAllViews()

        val db = FirebaseFirestore.getInstance()
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")

        db.collection("objectives")
            .whereEqualTo("admin", userID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for(objective in task.result){
//                        val button = Button(this)
//                        button.text = objective.getString("name") + " | ${objective.id}"
//                        button.tag = objective.id
//
//
//                        // TODO: Pass only document id and then find everything else directly in the detail page (query to db)
//                        button.setOnClickListener{
//                            val intent = Intent(this, ObjectiveDetailActivity::class.java)
//                            intent.putExtra("name", objective.getString("name"))
//                            intent.putExtra("amount", objective.get("amount").toString().toFloat())
//                            intent.putExtra("saved", objective.get("saved").toString().toFloat())
//                            intent.putExtra("objectiveId", objective.id)
//                            startActivity(intent)
//                        }
//
//                        rootView.addView(button)

                        val inflater = LayoutInflater.from(this)
                        val cardView = inflater.inflate(R.layout.objective_cardview, rootView, false) as CardView
                        val tvTitle = cardView.findViewById<TextView>(R.id.tvObjectiveCardTitle)
                        val tvParticipants = cardView.findViewById<TextView>(R.id.tvObjectiveCardParticipants)
                        val tvSavings = cardView.findViewById<TextView>(R.id.tvObjectiveCardProgress)

                        // Modify the text views with your data
                        tvTitle.text = objective.getString("name")
                        tvParticipants.text = "Temp"
                        // TODO: dollar to euro
                        tvSavings.text = "You saved ${objective.get("saved").toString().toFloat()}$ out of ${objective.get("amount").toString().toFloat()}$"

                        // Add the card view to the container layout
                        rootView.addView(cardView)

                        cardView.setOnClickListener{
                            val intent = Intent(this, ObjectiveDetailActivity::class.java)
                            intent.putExtra("name", objective.getString("name"))
                            intent.putExtra("amount", objective.get("amount").toString().toFloat())
                            intent.putExtra("saved", objective.get("saved").toString().toFloat())
                            intent.putExtra("objectiveId", objective.id)
                            startActivity(intent)
                        }


                    }
                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }
    }


}