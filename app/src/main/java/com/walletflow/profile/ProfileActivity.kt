package com.walletflow.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.welcome.LoginActivity

class ProfileActivity : BaseActivity() {
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val btnMyData = findViewById<Button>(R.id.profileDataButton)
        val btnLogout = findViewById<Button>(R.id.logoutButton)
        val tvUsername = findViewById<TextView>(R.id.profileName)
        val tvEmail = findViewById<TextView>(R.id.profileEmail)
        mAuth = FirebaseAuth.getInstance()

        tvUsername.text = userID

        db.collection("users").whereEqualTo("username", userID).get().addOnSuccessListener {
                tvEmail.text = it.documents.first().getString("email")
            }

        btnMyData.setOnClickListener {
            val intent = Intent(this, PersonalDataActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            finish()
            startActivity(intent)
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_profile
    }
}

