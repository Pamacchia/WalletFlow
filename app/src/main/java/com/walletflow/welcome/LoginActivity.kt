package com.walletflow.welcome

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.walletflow.HomeActivity
import com.walletflow.R

class LoginActivity : AppCompatActivity() {

    private lateinit var registrationBtn: Button
    private lateinit var loginBtn: Button
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var mAuth: FirebaseAuth

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            loginBtn.isEnabled = passwordField.text.isNotEmpty() && emailField.text.isNotEmpty()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_login)
        mAuth = FirebaseAuth.getInstance()

        if(mAuth.currentUser==null){
            Log.d(this.toString(), mAuth.currentUser?.email!!)
            initViews()
            setListeners()
        }
        else{
            finish()
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    private fun initViews() {
        registrationBtn = findViewById(R.id.btn_register)
        loginBtn = findViewById(R.id.btn_login)
        emailField = findViewById(R.id.login_email)
        passwordField = findViewById(R.id.login_password)
        emailField.addTextChangedListener(textWatcher)
        passwordField.addTextChangedListener(textWatcher)
        loginBtn.isEnabled = passwordField.text.isNotEmpty() && emailField.text.isNotEmpty()
    }

    private fun setListeners() {
        registrationBtn.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        loginBtn.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            validateCredentials(email, password)
        }
    }

    private fun validateCredentials(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    finish()
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    Toast.makeText(this, "Wrong credentials", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
