package com.walletflow.utils

import com.google.firebase.firestore.FirebaseFirestore

object TransactionManager {

    fun updateBalance(db : FirebaseFirestore, amount : Float, userID : String?){

        val query = db.collection("users").whereEqualTo("username", userID)

        query
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    val updatedBalance = document.getDouble("balance")?.plus(amount.toDouble())

                    document.reference
                        .update(mapOf(
                            "balance" to updatedBalance
                        ))
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