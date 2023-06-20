package com.walletflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore


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
            else if (true){

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
                                                if ((password == passwordCheck)) {

                                                    // Create a new user with a first and last name
                                                    val user: MutableMap<String, Any> = HashMap()
                                                    user["username"] = username
                                                    user["email"] = email
                                                    user["password"] = password

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
                                                } else {
                                                    Toast.makeText(
                                                        this,
                                                        "The passwords don't match!",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        } else {
                                            Log.w(this.localClassName, "Error getting documents checking email.", task.exception)
                                        }
                                    }
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
}