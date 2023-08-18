package com.walletflow.utils

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.data.Transaction

object TransactionManager {

    fun addTransactionRecordToDB(
        db: FirebaseFirestore,
        transaction: Transaction,
        userID: String?
    ) {

        val transactionMap = mapOf(
            "amount" to transaction.amount,
            "category" to transaction.category,
            "type" to transaction.type,
            "user" to transaction.user,
            "note" to transaction.note,
            "date" to transaction.date,
        )

        db.collection("transactions")
            .add(transactionMap)
            .addOnSuccessListener { documentReference ->
                updateBalance(
                    db,
                    transaction.amount!!.toFloat(),
                    userID
                )

                Log.d(
                    "HomeFrequentTransactionSuccess",
                    "DocumentSnapshot added with ID: " + documentReference.id
                )
            }
            .addOnFailureListener { e ->
                Log.w(
                    "HomeFrequentTransactionError",
                    "Error adding document",
                    e
                )
            }
    }

    fun deleteFrequentTransactionRecordFromDB(document: DocumentSnapshot) {
        document.reference.delete()
            .addOnSuccessListener {
                // Document successfully deleted
                // Handle success or UI updates here
                println("Document deleted successfully.")
            }
            .addOnFailureListener { e ->
                // An error occurred while deleting the document
                // Handle the error here
                println("Error deleting document: $e")
            }
    }

    fun updateBalance(db: FirebaseFirestore, amount: Float, userID: String?) {

        val query = db.collection("users").whereEqualTo("username", userID)

        query
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    val updatedBalance = document.getDouble("balance")?.plus(amount.toDouble())

                    document.reference
                        .update(
                            mapOf(
                                "balance" to updatedBalance
                            )
                        )
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