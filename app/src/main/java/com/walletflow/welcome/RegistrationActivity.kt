package com.walletflow.welcome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.R

class RegistrationActivity : AppCompatActivity() {

    private lateinit var submitBtn: Button
    private lateinit var backToLoginBtn: Button
    private lateinit var usernameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var passwordConfirmField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_registration)

        initViews()
        setSubmitClickListener()
        setBackToLoginClickListener()
    }

    private fun initViews() {
        usernameField = findViewById(R.id.registration_user_name)
        emailField = findViewById(R.id.registraiton_email)
        passwordField = findViewById(R.id.registration_password)
        passwordConfirmField = findViewById(R.id.registration_confirm_password)

        submitBtn = findViewById(R.id.btn_submit)
        backToLoginBtn = findViewById(R.id.btn_back)
    }

    private fun setSubmitClickListener() {
        submitBtn.setOnClickListener {
            val username = usernameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val passwordCheck = passwordConfirmField.text.toString()

            when {
                fieldsAreEmpty(
                    username,
                    email,
                    password,
                    passwordCheck
                ) -> showToast("Please specify all the fields")

                !isPasswordValid(password) -> showToast("Please create a valid password")
                password != passwordCheck -> showToast("The passwords don't match!")
                !isEmailValid(email) -> showToast("Please insert a valid email")
                else -> handleRegistration(username, email, password)
            }
        }
    }

    private fun fieldsAreEmpty(vararg fields: String) = fields.any { it.isEmpty() }

    private fun handleRegistration(username: String, email: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (!task.result.isEmpty) {
                        showToast("Already existing username!")
                    } else {
                        addIfEmailIsNew(db, username, email, password)
                    }
                } else {
                    handleDatabaseError(task.exception)
                }
            }
    }

    private fun setBackToLoginClickListener() {
        backToLoginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun addIfEmailIsNew(
        db: FirebaseFirestore,
        username: String,
        email: String,
        password: String
    ) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    goToOnboardingActivity()
                } else {
                    showToast("Already existing email!")
                }
            }
            .addOnFailureListener { exception ->
                handleDatabaseError(exception)
            }
    }

    private fun isPasswordValid(password: String): Boolean {
        val lengthRange = 5..20

        return password.length in lengthRange &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }
    }

    private fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
        return email.matches(emailRegex)
    }

    private fun handleDatabaseError(exception: Exception?) {
        Log.w(this.localClassName, "Error getting documents from the database.", exception)
    }

    private fun goToOnboardingActivity() {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.putExtra("username", usernameField.text.toString())
        intent.putExtra("email", emailField.text.toString())
        intent.putExtra("password", passwordField.text.toString())
        startActivity(intent)
    }

}
