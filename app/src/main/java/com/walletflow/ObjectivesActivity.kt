package com.walletflow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class ObjectivesActivity : BaseActivity() {

    lateinit var createNewObjective : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_objectives)

        createNewObjective = findViewById(R.id.btnAddNewObjectives)

        createNewObjective.setOnClickListener {

            val intent = Intent(this, AddObjectiveActivity::class.java)
            startActivity(intent)

        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_objectives
    }


}