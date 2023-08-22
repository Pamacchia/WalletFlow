package com.walletflow.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.HomeActivity
import com.walletflow.R
import com.walletflow.utils.Hashing

class OnboardingActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var etBalance: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_onboarding)

        initViews()
        setStartClickListener()
    }

    private fun initViews() {
        btnStart = findViewById(R.id.btnStartOnboarding)
        etBalance = findViewById(R.id.etBalance)
    }

    private fun setStartClickListener() {
        btnStart.setOnClickListener {
            val username = intent.getStringExtra("username")
            val email = intent.getStringExtra("email")
            val password = intent.getStringExtra("password")

            val onboardingBalance = etBalance.text.toString().toFloat()

            val db = FirebaseFirestore.getInstance()
            addUser(db, username!! , email!!, password!!, onboardingBalance)
            saveUserID(username)

            goToHomeActivity()
        }
    }

    private fun addUser(db: FirebaseFirestore, username: String, email: String, password: String, balance : Float) {
        val user = mapOf(
            "username" to username,
            "email" to email,
            "password" to Hashing.hashPassword(password),
            "balance" to balance
        )
        db.collection("users").add(user)
    }

    private fun saveUserID(username: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString("userID", username)
        }
    }

    private fun goToHomeActivity() {
        finish()
        startActivity(Intent(this, HomeActivity::class.java))
    }
}
