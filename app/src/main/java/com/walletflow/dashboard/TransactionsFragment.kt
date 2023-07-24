package com.walletflow.dashboard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.walletflow.objectives.ObjectiveDetailActivity

class TransactionsFragment : Fragment() {

    lateinit var filterExpenseTv : TextView
    lateinit var filterEarningTv : TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filterExpenseTv = view.findViewById(R.id.tvFilterTransactionListExpense)
        filterEarningTv = view.findViewById(R.id.tvFilterTransactionListEarning)

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")
        val db = FirebaseFirestore.getInstance()
        val queryRef = db.collection("transactions")
            .whereEqualTo("user", userID)

        filterRecordsByType(queryRef, "expense", view)

        filterExpenseTv.setOnClickListener {

            filterEarningTv.background = null
            filterExpenseTv.background = resources.getDrawable(R.drawable.edittext_rectangle)

            filterRecordsByType(queryRef, filterExpenseTv.text.toString().lowercase(), view)
        }

        filterEarningTv.setOnClickListener {

            filterExpenseTv.background = null
            filterEarningTv.background = resources.getDrawable(R.drawable.edittext_rectangle)

            filterRecordsByType(queryRef, filterEarningTv.text.toString().lowercase(), view)
        }

    }

    private fun filterRecordsByType(
        queryRef: Query,
        type: String,
        view: View
    ) {

        val rootView = view.findViewById<LinearLayout>(R.id.layoutTransactionList)
        rootView.removeAllViews()

        queryRef
            .whereEqualTo("type", type)
            .get().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    for (document in task.result) {

                        val inflater = LayoutInflater.from(requireContext())
                        val cardView = inflater.inflate(R.layout.transaction_cardview, rootView, false) as CardView
                        val tvDate = cardView.findViewById<TextView>(R.id.tvTransactionCardDate)
                        val tvCategory = cardView.findViewById<TextView>(R.id.tvTransactionCardCategory)
                        val tvAmount = cardView.findViewById<TextView>(R.id.tvTransactionCardAmount)

                        tvDate.text = document.getString("date")
                        tvCategory.text = document.getString("category")
                        tvAmount.text = document.getDouble("amount").toString() + "$" // TODO: Euro
                        // Add the card view to the container layout

                        val deleteButton = cardView.findViewById<Button>(R.id.btTransactionDelete)
                        deleteButton.setOnClickListener {
                            FirebaseFirestore.getInstance().collection("transactions").document(document.id).delete()
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

                        rootView.addView(cardView)

                        Log.w(context.toString(), document.data.toString())
                    }

                } else {
                    Log.w(requireContext().toString(), "Error getting transactions")
                }
            }
    }


}