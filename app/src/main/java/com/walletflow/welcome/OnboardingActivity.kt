package com.walletflow.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.HomeActivity
import com.walletflow.R
import com.walletflow.utils.DecimalDigitsInputFilter

class OnboardingActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var etBalance: EditText
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_onboarding)
        mAuth = FirebaseAuth.getInstance()

        initViews()
        setStartClickListener()
    }

    private fun initViews() {
        btnStart = findViewById(R.id.btnStartOnboarding)
        etBalance = findViewById(R.id.etBalance)
        etBalance.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(2))
    }

    private fun setStartClickListener() {
        btnStart.setOnClickListener {
            val username = intent.getStringExtra("username")
            val email = intent.getStringExtra("email")
            val password = intent.getStringExtra("password")
            val onboardingBalance = etBalance.text.toString().toFloat()

            mAuth.createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val db = FirebaseFirestore.getInstance()
                        addUser(db, username!!, email, onboardingBalance)
                        saveUserID(username)
                        finish()
                        startActivity(Intent(this, HomeActivity::class.java))
                    } else {
                        Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun addUser(
        db: FirebaseFirestore,
        username: String,
        email: String,
        balance: Float
    ) {
        val user = mapOf(
            "username" to username,
            "email" to email,
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
}
