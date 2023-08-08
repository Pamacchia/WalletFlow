package com.walletflow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.walletflow.dashboard.DashboardActivity
import com.walletflow.objectives.ObjectivesActivity
import com.walletflow.profile.ProfileActivity


abstract class BaseActivity : AppCompatActivity() {

    private lateinit var bottomNavBar: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResourceId())

        bottomNavBar = findViewById(R.id.bottom_navigation)

        bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    openHomeActivity()
                }

                R.id.nav_dashboard -> {
                    openDashboardActivity()
                }

                R.id.nav_objectives -> {
                    openObjectivesActivity()
                }

                R.id.nav_settings -> {
                    openProfileActivity()
                }
            }
            false
        }
    }

    abstract fun getLayoutResourceId(): Int

    private fun openHomeActivity() {
        if (!HomeActivity::class.java.name.contains(this.localClassName)) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openDashboardActivity() {
        if (!DashboardActivity::class.java.name.contains(this.localClassName)) {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openObjectivesActivity() {
        if (!ObjectivesActivity::class.java.name.contains(this.localClassName)) {
            val intent = Intent(this, ObjectivesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openProfileActivity() {
        if (!ProfileActivity::class.java.name.contains(this.localClassName)) {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}