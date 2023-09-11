package com.walletflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.dashboard.DashboardActivity
import com.walletflow.data.User
import com.walletflow.friends.FriendsActivity
import com.walletflow.objectives.ObjectivesActivity
import com.walletflow.profile.ProfileActivity


abstract class BaseActivity : AppCompatActivity() {

    private lateinit var bottomNavBar: BottomNavigationView

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var userID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResourceId())

        db.collection("users").whereEqualTo("email", mAuth.currentUser?.email).get()
            .addOnSuccessListener { task ->
                if (task.documents.isNotEmpty()) {
                    val user = task.documents.first().toObject(User::class.java)
                    userID = user?.username!!
                } else {
                    Toast.makeText(this, "Error loading user", Toast.LENGTH_SHORT).show()
                }
            }

//        userID = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("userID", "") ?: ""
        bottomNavBar = findViewById(R.id.bottom_navigation)

        bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> openActivity(HomeActivity::class.java)
                R.id.nav_dashboard -> openActivity(DashboardActivity::class.java)
                R.id.nav_objectives -> openActivity(ObjectivesActivity::class.java)
                R.id.nav_friends -> openActivity(FriendsActivity::class.java)
                R.id.nav_settings -> openActivity(ProfileActivity::class.java)
            }
            false
        }
    }

    abstract fun getLayoutResourceId(): Int

    private fun openActivity(activityClass: Class<*>) {
        if (!activityClass.name.contains(this.localClassName)) {
            val intent = Intent(this, activityClass)
            startActivity(intent)
            finish()
        }
    }
}