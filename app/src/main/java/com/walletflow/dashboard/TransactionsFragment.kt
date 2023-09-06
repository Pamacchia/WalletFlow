package com.walletflow.dashboard

import android.app.AlertDialog
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.data.Transaction
import com.walletflow.utils.IconHelper
import com.walletflow.utils.SQLiteDBHelper
import com.walletflow.utils.StringHelper
import com.walletflow.utils.TransactionManager
import java.text.SimpleDateFormat
import java.util.Calendar

class TransactionsFragment(
    private val listener: (Query, (List<DocumentSnapshot>) -> (Unit)) -> Unit
) : Fragment() {

    private lateinit var activityFragment: BaseActivity
    private lateinit var filterExpenseTv: TextView
    private lateinit var filterEarningTv: TextView
    private lateinit var rootView: LinearLayout
    private lateinit var queryRef: Query

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view.findViewById(R.id.layoutTransactionList)
        filterExpenseTv = view.findViewById(R.id.tvFilterTransactionListExpense)
        filterEarningTv = view.findViewById(R.id.tvFilterTransactionListEarning)
        filterExpenseTv.setTypeface(null, Typeface.BOLD)
        activityFragment = activity as BaseActivity
        queryRef = activityFragment.db.collection("transactions")
            .whereEqualTo("user", activityFragment.userID)

        queryRef.whereEqualTo("type", "expense").get().addOnSuccessListener {
                listener(it.query) { documents -> filterRecordsByType(documents) }
            }

        filterExpenseTv.setOnClickListener {
            filterExpenseTv.setTypeface(null, Typeface.BOLD)
            filterEarningTv.setTypeface(null, Typeface.NORMAL)
            listener(queryRef.whereEqualTo("type", "expense")) { documents ->
                filterRecordsByType(
                    documents
                )
            }
        }

        filterEarningTv.setOnClickListener {
            filterExpenseTv.setTypeface(null, Typeface.NORMAL)
            filterEarningTv.setTypeface(null, Typeface.BOLD)
            listener(queryRef.whereEqualTo("type", "earning")) { documents ->
                filterRecordsByType(
                    documents
                )
            }
        }
    }

    private fun filterRecordsByType(documents: List<DocumentSnapshot>) {
        rootView.removeAllViews()
        documents.forEach { transactionDocumentSnapshot ->

            val transaction = transactionDocumentSnapshot.toObject(Transaction::class.java)
            val cardView = layoutInflater.inflate(
                R.layout.transaction_cardview, rootView, false
            ) as CardView
            val tvDate = cardView.findViewById<TextView>(R.id.tvTransactionCardDate)
            val tvCategory = cardView.findViewById<TextView>(R.id.tvTransactionCardCategory)
            val tvAmount = cardView.findViewById<TextView>(R.id.tvTransactionCardAmount)
            val ivCategory = cardView.findViewById<ImageView>(R.id.transactionIv)

            IconHelper.setIconCard(requireContext(), transaction!!.category, ivCategory)

            tvDate.text = transaction.date
            tvCategory.text = transaction.category
            tvAmount.text = "${StringHelper.getShrunkForm(transaction.amount!!)} €"

            val deleteButton = cardView.findViewById<Button>(R.id.btTransactionDelete)
            deleteButton.setOnClickListener {
                createAlertForFrequentTransaction(transactionDocumentSnapshot, "Confirm deleting operation")
            }

            rootView.addView(cardView)
        }
    }

    private fun createAlertForFrequentTransaction(
        transactionDocumentSnapshot: DocumentSnapshot,
        message: String
    ) {
        val alert: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alert.setTitle(message)
        alert.setMessage("Are you sure?")
        alert.setPositiveButton("Yes") { _, _ ->
            deleteTransaction(transactionDocumentSnapshot, transactionDocumentSnapshot.getDouble("amount"))
        }
        alert.setNegativeButton("No") { dialog, _ ->
            dialog.cancel()
        }
        alert.show()
    }
    private fun deleteTransaction(document: DocumentSnapshot, amount: Double?) {
        document.reference.delete().addOnSuccessListener {
            amount?.let {
                TransactionManager.updateBalance(
                    activityFragment.db,
                    -it.toFloat(),
                    activityFragment.userID
                )
            }
            println("Document deleted successfully.")
        }.addOnFailureListener { e ->
            println("Error deleting document: $e")
        }
    }
}
