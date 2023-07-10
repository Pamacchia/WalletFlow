package com.walletflow

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.data.Icon
import java.io.IOException

class ObjectivesActivity : BaseActivity() {

    lateinit var createNewObjective : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_objectives)

        createNewObjective = findViewById(R.id.btnAddNewObjectives)

        loadObjectives()

        createNewObjective.setOnClickListener {

            val intent = Intent(this, AddObjectiveActivity::class.java)
            startActivity(intent)
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_objectives
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
                        val button = Button(this)
                        button.text = objective.getString("name") + " | ${objective.id}"
                        button.tag = objective.id


                        // TODO: Pass only document id and then find everything else directly in the detail page (query to db)
                        button.setOnClickListener{
                            val intent = Intent(this, ObjectiveDetailActivity::class.java)
                            intent.putExtra("name", objective.getString("name"))
                            intent.putExtra("amount", objective.get("amount").toString().toFloat())
                            intent.putExtra("saved", objective.get("saved").toString().toFloat())
                            intent.putExtra("objectiveId", objective.id)
                            startActivity(intent)
                        }

                        rootView.addView(button)
                    }
                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }
    }


}