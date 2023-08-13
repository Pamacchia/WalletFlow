package com.walletflow.welcome

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.HomeActivity
import com.walletflow.R
import com.walletflow.utils.Hashing

class LoginActivity : AppCompatActivity() {

    private lateinit var registrationBtn: Button
    private lateinit var loginBtn: Button
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_login)

        initViews()
        setListeners()
    }

    private fun initViews() {
        registrationBtn = findViewById(R.id.btn_register)
        loginBtn = findViewById(R.id.btn_login)
        usernameField = findViewById(R.id.login_user_name)
        passwordField = findViewById(R.id.login_password)
    }

    private fun setListeners() {
        registrationBtn.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        loginBtn.setOnClickListener {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()

            validateCredentials(username, password)
        }
    }

    private fun validateCredentials(username: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("username", username)
            .whereEqualTo("password", Hashing.hashPassword(password))
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    handleLoginResult(task.result.isEmpty, username)
                } else {
                    handleDatabaseError(task.exception)
                }
            }
    }

    private fun handleLoginResult(isEmpty: Boolean, username: String) {
        if (isEmpty) {
            showToast("Wrong credentials")
            passwordField.text.clear()
        } else {
            saveUserID(username)
            goToHomeActivity()
        }
    }

    private fun saveUserID(username: String) {
        getSharedPreferencesEditor("MyPrefs") {
            putString("userID", username)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun handleDatabaseError(exception: Exception?) {
        Log.w(this.localClassName, "Error getting documents.", exception)
    }

    private fun goToHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    private inline fun Context.getSharedPreferencesEditor(
        name: String,
        mode: Int = Context.MODE_PRIVATE,
        action: SharedPreferences.Editor.() -> Unit
    ) {
        val sharedPreferences = getSharedPreferences(name, mode)
        val editor = sharedPreferences.edit()
        action(editor)
        editor.apply()
    }
}
