package com.walletflow

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class OnboardingActivity : AppCompatActivity() {

    lateinit var btnStart : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        btnStart = findViewById(R.id.btnStartOnboarding)

        btnStart.setOnClickListener {

            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userID = sharedPreferences.getString("userID", "")

            val db = FirebaseFirestore.getInstance()

            db.collection("users")
                .whereEqualTo("username", userID)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //TODO: Add modification of record using username and not documentID (chatgpt)
                    } else {
                        Log.w(this.localClassName, "Error getting documents checking username.", task.exception)
                    }
                }

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}