package com.walletflow.dashboard

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.walletflow.R
import com.walletflow.utils.TransactionManager

class TransactionsFragment : Fragment() {

    private lateinit var filterExpenseTv: TextView
    private lateinit var filterEarningTv: TextView
    private lateinit var rootView: LinearLayout

    private val db = FirebaseFirestore.getInstance()
    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }
    private val userID by lazy {
        sharedPreferences.getString("userID", "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView = view.findViewById(R.id.layoutTransactionList)
        filterExpenseTv = view.findViewById(R.id.tvFilterTransactionListExpense)
        filterEarningTv = view.findViewById(R.id.tvFilterTransactionListEarning)

        val queryRef = db.collection("transactions").whereEqualTo("user", userID)

        filterExpenseTv.setOnClickListener {

            filterExpenseTv.setTypeface(null, Typeface.BOLD)
            filterEarningTv.setTypeface(null, Typeface.NORMAL)
            filterRecordsByType(queryRef, "expense")

        }

        filterEarningTv.setOnClickListener {
            filterExpenseTv.setTypeface(null, Typeface.NORMAL)
            filterEarningTv.setTypeface(null, Typeface.BOLD)
            filterRecordsByType(queryRef, "earning")
        }

        filterRecordsByType(queryRef, "expense")
    }

    private fun filterRecordsByType(queryRef: Query, type: String) {
        rootView.removeAllViews()

        queryRef.whereEqualTo("type", type).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    val cardView = layoutInflater.inflate(
                        R.layout.transaction_cardview,
                        rootView,
                        false
                    ) as CardView
                    val tvDate = cardView.findViewById<TextView>(R.id.tvTransactionCardDate)
                    val tvCategory = cardView.findViewById<TextView>(R.id.tvTransactionCardCategory)
                    val tvAmount = cardView.findViewById<TextView>(R.id.tvTransactionCardAmount)

                    tvDate.text = document.getString("date")
                    tvCategory.text = document.getString("category")
                    tvAmount.text = "${document.getDouble("amount")} $" // TODO: Euro

                    val deleteButton = cardView.findViewById<Button>(R.id.btTransactionDelete)
                    deleteButton.setOnClickListener {
                        deleteTransaction(document.id, document.getDouble("amount"))
                        refreshTransactionList(type)
                    }

                    rootView.addView(cardView)
                }
            } else {
                println("Error getting transactions")
            }
        }
    }

    private fun deleteTransaction(documentId: String, amount: Double?) {
        db.collection("transactions").document(documentId).delete().addOnSuccessListener {
            amount?.let { TransactionManager.updateBalance(db, -it.toFloat(), userID) }
            println("Document deleted successfully.")
        }.addOnFailureListener { e ->
            println("Error deleting document: $e")
        }
    }

    private fun refreshTransactionList(type: String) {
        val queryRef = db.collection("transactions").whereEqualTo("user", userID)
        filterRecordsByType(queryRef, type)
    }
}
