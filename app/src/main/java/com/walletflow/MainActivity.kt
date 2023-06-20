package com.walletflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.walletflow.utils.Hashing
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter


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
            val db = FirebaseFirestore.getInstance()

            val username = usernameField.text.toString()
            val password = passwordField.text.toString()

            db.collection("users")
                .whereEqualTo("username", username)
                .whereEqualTo("password", Hashing.hashPassword(password)).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if(!task.result.isEmpty()){
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Wrong credentials", Toast.LENGTH_LONG).show()
                            passwordField.text.clear()
                        }
                    } else {
                        Log.w(this.localClassName, "Error getting documents.", task.exception)
                    }
                }
        }
    }

}