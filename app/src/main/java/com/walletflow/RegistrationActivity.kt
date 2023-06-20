package com.walletflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.utils.Hashing
import java.security.MessageDigest


class RegistrationActivity : AppCompatActivity() {

    lateinit var submitBtn : Button
    lateinit var registrationBtn : Button
    lateinit var usernameField : EditText
    lateinit var emailField : EditText
    lateinit var passwordField : EditText
    lateinit var passwordConfirmField : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        usernameField = findViewById(R.id.registration_user_name)
        emailField = findViewById(R.id.registraiton_email)
        passwordField = findViewById(R.id.registration_password)
        passwordConfirmField = findViewById(R.id.registration_confirm_password)

        submitBtn = findViewById(R.id.btn_submit)
        registrationBtn = findViewById(R.id.btn_back)

        submitBtn.setOnClickListener {

            val username = usernameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val passwordCheck = passwordConfirmField.text.toString()

            // TODO: aggiungere controllo username
            if(username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordCheck.isEmpty()){
                Toast.makeText(this, "Please specify all the fields", Toast.LENGTH_LONG).show()
            }
            else if (!isPasswordValid(password)){
                Toast.makeText(this, "Please create a password with at least one uppercase, lowercase, digit and special character", Toast.LENGTH_LONG).show()
            } else if(password != passwordCheck){
                Toast.makeText(
                    this,
                    "The passwords don't match!",
                    Toast.LENGTH_LONG
                ).show()
            } else if(!isEmailValid(email)){
                Toast.makeText(this, "Please insert a valid email", Toast.LENGTH_LONG).show()
            } else {
                val db = FirebaseFirestore.getInstance()
                db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if(!task.result.isEmpty()){
                                Toast.makeText(this, "Already existing username!", Toast.LENGTH_LONG).show()
                            } else {
                                addIfEmailIsNew(db, username, email, password)
                            }
                        } else {
                            Log.w(this.localClassName, "Error getting documents checking username.", task.exception)
                        }
                    }
            }

        }

        registrationBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


    private fun addUser(db : FirebaseFirestore, username : String, email : String, password : String){
        val user: MutableMap<String, Any> = HashMap()
        user["username"] = username
        user["email"] = email
        user["password"] = Hashing.hashPassword(password)

        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    this.localClassName,
                    "DocumentSnapshot added with ID: " + documentReference.id
                )
            }
            .addOnFailureListener { e ->
                Log.w(
                    this.localClassName,
                    "Error adding document",
                    e
                )
            }
        val intent =
            Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun addIfEmailIsNew(db : FirebaseFirestore, username : String, email : String, password : String){
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (!task.result.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Already existing email!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Create a new user with a first and last name
                        addUser(db, username, email, password)
                    }
                } else {
                    Log.w(this.localClassName, "Error getting documents checking email.", task.exception)
                }
            }
    }
    private fun isPasswordValid(password: String): Boolean {
        val minLength = 5
        val maxLength = 20

        // Check length
        if (password.length !in minLength..maxLength) {
            return false
        }

        // Check for at least one uppercase letter
        if (!password.any { it.isUpperCase() }) {
            return false
        }

        // Check for at least one lowercase letter
        if (!password.any { it.isLowerCase() }) {
            return false
        }

        // Check for at least one digit
        if (!password.any { it.isDigit() }) {
            return false
        }

        // Check for at least one special character
        if (!password.any { !it.isLetterOrDigit() }) {
            return false
        }

        // All checks passed, password is valid
        return true
    }

    private fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
        return email.matches(emailRegex)
    }
}