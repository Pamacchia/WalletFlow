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

        initViews()
        setStartClickListener()
    }

    private fun initViews() {
        btnStart = findViewById(R.id.btnStartOnboarding)
        etBalance = findViewById(R.id.etBalance)
    }

    private fun setStartClickListener() {
        btnStart.setOnClickListener {
            val userID = getUserID()
            val onboardingBalance = etBalance.text.toString().toFloat()

            val updatedFields = mapOf(
                "balance" to onboardingBalance
            )

            modifyRecordByUsername("users", userID, updatedFields)

            goToHomeActivity()
        }
    }

    private fun getUserID(): String? {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userID", "")
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
                    updateDocumentFields(db, collectionName, document.id, updatedFields)
                }
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
            }
    }

    private fun updateDocumentFields(
        db: FirebaseFirestore,
        collectionName: String,
        documentId: String,
        updatedFields: Map<String, Any>
    ) {
        val documentRef = db.collection(collectionName).document(documentId)
        documentRef
            .update(updatedFields)
            .addOnSuccessListener {
                println("Document updated successfully.")
            }
            .addOnFailureListener { e ->
                println("Error updating document: $e")
            }
    }

    private fun goToHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
    }
}
