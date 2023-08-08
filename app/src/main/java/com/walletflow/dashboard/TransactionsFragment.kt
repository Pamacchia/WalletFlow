@file:Suppress("DEPRECATION")

package com.walletflow.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.walletflow.R
import com.walletflow.data.Transaction

class TransactionsFragment : Fragment() {

    private lateinit var filterExpenseTv: TextView
    private lateinit var filterEarningTv: TextView
    private lateinit var transactionsRv: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityDashboard = (activity as DashboardActivity)
        val userID = activityDashboard.userID
        val queryRef = activityDashboard.db.collection("transactions").whereEqualTo("user", userID)

        filterExpenseTv = view.findViewById(R.id.tvFilterTransactionListExpense)
        filterEarningTv = view.findViewById(R.id.tvFilterTransactionListEarning)
        transactionsRv = view.findViewById(R.id.rvTransactions)
        filterRecordsByType(queryRef, "expense")

        filterExpenseTv.setOnClickListener {

            filterEarningTv.background = null
            filterExpenseTv.background = resources.getDrawable(R.drawable.edittext_rectangle)

            filterRecordsByType(queryRef, filterExpenseTv.text.toString().lowercase())
        }

        filterEarningTv.setOnClickListener {

            filterExpenseTv.background = null
            filterEarningTv.background = resources.getDrawable(R.drawable.edittext_rectangle)

            filterRecordsByType(queryRef, filterEarningTv.text.toString().lowercase())
        }

    }

    private fun filterRecordsByType(
        queryRef: Query,
        type: String
    ) {
        queryRef
            .whereEqualTo("type", type)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val mapTransactions = mutableMapOf<String, Transaction>()
                    task.result.forEach { document ->
                        mapTransactions[document.id] = document.toObject(Transaction::class.java)
                    }
                    transactionsRv.adapter = TransactionsAdapter(
                        mapTransactions,
                        (activity as DashboardActivity).db.collection("transactions")
                    )
                    Log.d(context.toString(), "transactionsAdapter created")
                } else {
                    Log.w(requireContext().toString(), "Error getting transactions")
                }
            }
    }
}