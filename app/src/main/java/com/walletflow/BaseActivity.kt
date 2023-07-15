package com.walletflow

import android.content.ClipData.Item
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.walletflow.objectives.ObjectivesActivity

abstract class BaseActivity : AppCompatActivity() {

    lateinit var itemOpenHome : View
    lateinit var itemOpenObjectives : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResourceId())

        itemOpenHome = findViewById(R.id.nav_home)
        itemOpenObjectives = findViewById(R.id.nav_objectives)

        itemOpenHome.setOnClickListener {
            openHomeActivity()
        }

        itemOpenObjectives.setOnClickListener {
            openObjectivesActivity()
        }

    }

    abstract fun getLayoutResourceId(): Int

    private fun openHomeActivity() {
        // Launch the HomeActivity
        if(!HomeActivity::class.java.name.contains(this.localClassName)){
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openObjectivesActivity() {
        if(!ObjectivesActivity::class.java.name.contains(this.localClassName)){
            val intent = Intent(this, ObjectivesActivity::class.java)
            startActivity(intent)
        }
    }
}