package com.walletflow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    lateinit var registrationBtn : Button
    lateinit var loginBtn : Button
    lateinit var usernameField : EditText
    lateinit var passwordField : EditText
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registrationBtn = findViewById(R.id.btn_register)
        loginBtn = findViewById(R.id.btn_login)
        usernameField = findViewById(R.id.login_user_name)
        passwordField = findViewById(R.id.login_password)

        registrationBtn.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            val db = DBHelper(this, null)

            val username = usernameField.text.toString()
            val password = passwordField.text.toString()

            if(db.checkLogin(username, password)){
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Wrong credentials", Toast.LENGTH_LONG).show()
                passwordField.text.clear()
            }
        }


    }
}