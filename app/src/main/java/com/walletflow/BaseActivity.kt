package com.walletflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.walletflow.objectives.ObjectivesActivity

abstract class BaseActivity : AppCompatActivity() {

    lateinit var bottomNavBar : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResourceId())
        bottomNavBar = findViewById(R.id.bottom_navigation)
        bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_settings -> {

                    true
                }
                R.id.nav_dashboard -> {

                    true
                }
                R.id.nav_objectives -> {
                    openObjectivesActivity()
                    true
                }

                else -> {
                    openHomeActivity()
                    true
                }
            }
        }

    }

    abstract fun getLayoutResourceId(): Int

    private fun openHomeActivity() {
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