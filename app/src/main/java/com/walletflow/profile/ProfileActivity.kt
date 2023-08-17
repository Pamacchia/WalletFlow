package com.walletflow.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.welcome.LoginActivity

class ProfileActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Find buttons by their IDs
        val btnMyData = findViewById<Button>(R.id.profileDataButton)
        val btnMyFriends = findViewById<Button>(R.id.changeTheme)
        val btnLogout = findViewById<Button>(R.id.logoutButton)
        val tvUsername = findViewById<TextView>(R.id.profileName)
        val tvEmail = findViewById<TextView>(R.id.profileEmail)

        tvUsername.text = userID

        db.collection("users")
            .whereEqualTo("username", userID)
            .get()
            .addOnSuccessListener {
                tvEmail.text = it.documents.first().getString("email")
            }

        // Set click listeners for each button
        btnMyData.setOnClickListener {
            val intent = Intent(this, PersonalDataActivity::class.java)
            startActivity(intent)
        }

        btnMyFriends.setOnClickListener {
//            val intent = Intent(this, FriendsActivity::class.java)
//            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_profile
    }
}

