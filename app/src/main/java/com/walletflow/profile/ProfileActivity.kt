package com.walletflow.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.walletflow.BaseActivity
import com.walletflow.R

class ProfileActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Find buttons by their IDs
        val btnMyAccount = findViewById<Button>(R.id.myAccountButton)
        val btnMyFriends = findViewById<Button>(R.id.myFriendsButton)
        val btnLogout = findViewById<Button>(R.id.logoutButton)

        // Set click listeners for each button
        btnMyAccount.setOnClickListener {
        }

        btnMyFriends.setOnClickListener {
        }

        btnLogout.setOnClickListener {
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_profile
    }
}

