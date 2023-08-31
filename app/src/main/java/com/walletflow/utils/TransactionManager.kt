package com.walletflow.utils

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.data.Transaction

object TransactionManager {

    fun addTransactionRecordToDB(
        db: FirebaseFirestore, transaction: Transaction, userID: String?
    ) {

        val transactionMap = mapOf(
            "amount" to transaction.amount,
            "category" to transaction.category,
            "type" to transaction.type,
            "user" to transaction.user,
            "note" to transaction.note,
            "date" to transaction.date,
        )

        db.collection("transactions").add(transactionMap)
            .addOnSuccessListener {
                updateBalance(
                    db, transaction.amount!!.toFloat(), userID
                )
            }
    }

    fun deleteFrequentTransactionRecordFromDB(document: DocumentSnapshot) {
        document.reference.delete()
    }

    fun updateBalance(db: FirebaseFirestore, amount: Float, userID: String?) {

        val query = db.collection("users").whereEqualTo("username", userID)

        query.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val updatedBalance = document.getDouble("balance")?.plus(amount.toDouble())
                    document.reference.update(
                            mapOf(
                                "balance" to updatedBalance
                            )
                        )
                }
            }
    }


}