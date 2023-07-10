package com.walletflow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text

class ObjectiveDetailActivity : BaseActivity() {

    lateinit var titleTv : TextView
    lateinit var recapTv : TextView
    lateinit var addParticipantBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_objective_detail)

        titleTv = findViewById(R.id.tvObjectiveTitle)
        recapTv = findViewById(R.id.tvObjectiveRecap)
        addParticipantBtn = findViewById(R.id.btnAddParticipant)

        titleTv.text = intent.getStringExtra("name") + " detail"
        recapTv.text = intent.getFloatExtra("saved", 0f).toString() + "$ saved of " + intent.getFloatExtra("amount", 0f).toString() + "$"

        addParticipantBtn.setOnClickListener{

        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_objective_detail
    }
}