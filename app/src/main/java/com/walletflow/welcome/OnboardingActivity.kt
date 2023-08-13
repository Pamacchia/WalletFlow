package com.walletflow.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.HomeActivity
import com.walletflow.R

class OnboardingActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var etBalance: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_onboarding)

        btnStart = findViewById(R.id.btnStartOnboarding)
        etBalance = findViewById(R.id.etBalance)

        btnStart.setOnClickListener {

            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userID = sharedPreferences.getString("userID", "")

            val onboardingBalance = etBalance.text.toString().toFloat()

            val updatedFields = mapOf(
                "balance" to onboardingBalance
            )

            modifyRecordByUsername("users", userID, updatedFields)

            Thread.sleep(150)

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun modifyRecordByUsername(
        collectionName: String,
        username: String?,
        updatedFields: Map<String, Any>
    ) {

        val db = FirebaseFirestore.getInstance()

        val query = db.collection(collectionName).whereEqualTo("username", username)

        query
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val documentRef = db.collection(collectionName).document(document.id)
                    documentRef
                        .update(updatedFields)
                        .addOnSuccessListener {
                            println("Document updated successfully.")
                        }
                        .addOnFailureListener { e ->
                            println("Error updating document: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
            }
    }

}