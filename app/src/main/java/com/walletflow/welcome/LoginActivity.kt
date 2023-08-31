package com.walletflow.welcome

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.HomeActivity
import com.walletflow.R
import com.walletflow.data.User

class LoginActivity : AppCompatActivity() {

    private lateinit var registrationBtn: Button
    private lateinit var loginBtn: Button
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_login)
        mAuth = FirebaseAuth.getInstance()

        initViews()
        setListeners()
    }

    private fun initViews() {
        registrationBtn = findViewById(R.id.btn_register)
        loginBtn = findViewById(R.id.btn_login)
        emailField = findViewById(R.id.login_email)
        passwordField = findViewById(R.id.login_password)
    }

    private fun setListeners() {
        registrationBtn.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        loginBtn.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            FirebaseFirestore.getInstance().collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener { task ->
                    if (task.documents.isNotEmpty()) {
                        val user = task.documents.first().toObject(User::class.java)
                        validateCredentials(user!!.username, email, password)
                    } else {
                        Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun validateCredentials(username: String?, email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    getSharedPreferencesEditor("MyPrefs") {
                        putString("userID", username!!)
                    }
                    finish()
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    Toast.makeText(this, "Wrong credentials", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private inline fun Context.getSharedPreferencesEditor(
        name: String, mode: Int = Context.MODE_PRIVATE, action: SharedPreferences.Editor.() -> Unit
    ) {
        val sharedPreferences = getSharedPreferences(name, mode)
        val editor = sharedPreferences.edit()
        action(editor)
        editor.apply()
    }
}
