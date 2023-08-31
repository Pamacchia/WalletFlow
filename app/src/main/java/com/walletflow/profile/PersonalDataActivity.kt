package com.walletflow.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.walletflow.BaseActivity
import com.walletflow.R


class PersonalDataActivity : BaseActivity() {

    private lateinit var newPasswordEt: EditText
    private lateinit var oldPasswordEt: EditText
    private lateinit var confirmPasswordEt: EditText
    private lateinit var submitBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        submitBtn.setOnClickListener {
            changePassword()
        }
    }

    private fun initViews() {
        oldPasswordEt = findViewById(R.id.etOldPassword)
        newPasswordEt = findViewById(R.id.etNewPassword)
        confirmPasswordEt = findViewById(R.id.etConfirmNewPassword)
        submitBtn = findViewById(R.id.submitChangeData)
    }

    private fun changePassword() {

        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val currentPassword = oldPasswordEt.text.toString()
        val newPassword = newPasswordEt.text.toString()

        when {

            fieldsAreEmpty(
                oldPasswordEt.text.toString(),
                newPasswordEt.text.toString(),
                confirmPasswordEt.text.toString()
            ) -> showToast("Please specify all the fields")

            !isPasswordValid(newPasswordEt.text.toString()) -> showToast("Please create a new valid password")

            newPasswordEt.text.toString() != confirmPasswordEt.text.toString() -> showToast("The passwords don't match!")

            else -> {
                user?.reauthenticate(
                    EmailAuthProvider.getCredential(
                        user.email!!,
                        currentPassword
                    )
                )
                    ?.addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        val intent = Intent(this, ProfileActivity::class.java)
                                        startActivity(intent)
                                    } else {
                                        showToast("Something went wrong with the password update")
                                    }
                                }
                        } else {
                            showToast("The old password isn't correct!")
                        }
                    }
            }
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