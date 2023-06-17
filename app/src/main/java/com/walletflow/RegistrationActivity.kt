package com.walletflow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

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

            if(username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordCheck.isEmpty()){
                Toast.makeText(this, "Please specify all the fields", Toast.LENGTH_LONG).show()
            } else {

                if((password == passwordCheck)) {
                    val db = DBHelper(this, null)
                    db.addUser(username, email, password)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }

        }

        registrationBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}