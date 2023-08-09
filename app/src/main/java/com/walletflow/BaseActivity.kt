package com.walletflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.dashboard.DashboardActivity
import com.walletflow.objectives.ObjectivesActivity
import com.walletflow.profile.ProfileActivity


abstract class BaseActivity : AppCompatActivity(){

    lateinit var bottomNavBar : BottomNavigationView

    val db = FirebaseFirestore.getInstance()
    lateinit var userID : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResourceId())

        userID = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("userID", "") ?: ""

        bottomNavBar = findViewById(R.id.bottom_navigation)

        bottomNavBar.setOnItemSelectedListener{
            when(it.itemId){
                R.id.nav_home ->{
                    openHomeActivity()
                    true
                }
                R.id.nav_dashboard -> {
                    openDashboardActivity()
                    true
                }
                R.id.nav_objectives ->{
                    openObjectivesActivity()
                    true
                }
                R.id.nav_settings -> {
                    openProfileActivity()
                    true
                }
            }
            false
        }
    }

    abstract fun getLayoutResourceId(): Int

    private fun openHomeActivity() {
        if(!HomeActivity::class.java.name.contains(this.localClassName)){
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openDashboardActivity() {
        if(!DashboardActivity::class.java.name.contains(this.localClassName)){
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openObjectivesActivity() {
        if(!ObjectivesActivity::class.java.name.contains(this.localClassName)){
            val intent = Intent(this, ObjectivesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openProfileActivity() {
        if(!ProfileActivity::class.java.name.contains(this.localClassName)){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}