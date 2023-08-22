package com.walletflow.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.utils.Hashing

class PersonalDataActivity : BaseActivity() {

    private lateinit var newPasswordEt : EditText
    private lateinit var oldPasswordEt : EditText
    private lateinit var confirmPasswordEt : EditText
    private lateinit var submitBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        setSubmitClickListener()
    }

    private fun initViews() {
        oldPasswordEt = findViewById(R.id.etOldPassword)
        newPasswordEt = findViewById(R.id.etNewPassword)
        confirmPasswordEt = findViewById(R.id.etConfirmNewPassword)
        submitBtn = findViewById(R.id.submitChangeData)
    }

    private fun setSubmitClickListener() {

        db.collection("users")
            .whereEqualTo("username", userID)
            .get()
            .addOnSuccessListener {
                val oldPw = it.documents.first().getString("password").toString()
                submitBtn.setOnClickListener {
                    when {
                        fieldsAreEmpty(oldPasswordEt.text.toString(), newPasswordEt.text.toString(), confirmPasswordEt.text.toString()) -> showToast("Please specify all the fields")
                        Hashing.hashPassword(oldPasswordEt.text.toString()) != oldPw -> showToast("The old password isn't correct!")
                        !isPasswordValid(newPasswordEt.text.toString()) -> showToast("Please create a new valid password")
                        newPasswordEt.text.toString() != confirmPasswordEt.text.toString() -> showToast("The passwords don't match!")
                        else -> {
                            handleChangePassword(newPasswordEt.text.toString())
                            val intent = Intent(this, ProfileActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            }

    }


    private fun handleChangePassword(newPassword: String) {
        val updatedFields = mapOf(
            "password" to Hashing.hashPassword(newPassword)
        )

        modifyRecordByUsername("users", userID, updatedFields)
    }

    private fun modifyRecordByUsername(
        collectionName: String,
        username: String?,
        updatedFields: Map<String, Any>
    ) {

        val query = db.collection(collectionName).whereEqualTo("username", username)

        query
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    updateDocumentFields(collectionName, document.id, updatedFields)
                }
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
            }
    }

    private fun updateDocumentFields(
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

    private fun isPasswordValid(password: String): Boolean {
        val lengthRange = 5..20

        return password.length in lengthRange &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }
    }

    private fun fieldsAreEmpty(vararg fields: String) = fields.any { it.isEmpty() }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_personal_data
    }
}