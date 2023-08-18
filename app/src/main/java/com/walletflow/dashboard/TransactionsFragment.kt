package com.walletflow.dashboard

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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.data.Transaction
import com.walletflow.utils.SQLiteDBHelper
import com.walletflow.utils.TransactionManager

class TransactionsFragment : Fragment() {

    private lateinit var activityFragment : BaseActivity
    private lateinit var filterExpenseTv: TextView
    private lateinit var filterEarningTv: TextView
    private lateinit var rootView: LinearLayout
    private lateinit var listener: ListenerRegistration
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
        activityFragment = activity as BaseActivity
        rootView = view.findViewById(R.id.layoutTransactionList)
        filterExpenseTv = view.findViewById(R.id.tvFilterTransactionListExpense)
        filterEarningTv = view.findViewById(R.id.tvFilterTransactionListEarning)
        filterExpenseTv.setTypeface(null, Typeface.BOLD)

        val queryRef = activityFragment.db
            .collection("transactions")
            .whereEqualTo("user", activityFragment.userID)

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
       listener = queryRef.whereEqualTo("type", type)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(requireContext(), "Error loading data", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                querySnapshot?.let {
                    rootView.removeAllViews()
                    it.documents.forEach { transactionDocumentSnapshot ->

                        val transaction =
                            transactionDocumentSnapshot.toObject(Transaction::class.java)
                        val cardView = layoutInflater.inflate(
                            R.layout.transaction_cardview, rootView,
                            false
                        ) as CardView
                        val tvDate = cardView.findViewById<TextView>(R.id.tvTransactionCardDate)
                        val tvCategory =
                            cardView.findViewById<TextView>(R.id.tvTransactionCardCategory)
                        val tvAmount = cardView.findViewById<TextView>(R.id.tvTransactionCardAmount)
                        val ivCategory = cardView.findViewById<ImageView>(R.id.transactionIv)

                        setIconCard(transaction!!.category, ivCategory)

                        tvDate.text = transaction!!.date
                        tvCategory.text = transaction!!.category
                        tvAmount.text = "${transaction!!.amount} $" // TODO: Euro

                        val deleteButton = cardView.findViewById<Button>(R.id.btTransactionDelete)
                        deleteButton.setOnClickListener {
                            deleteTransaction(transactionDocumentSnapshot, transaction.amount)
                        }

                        rootView.addView(cardView)
                    }
                }
            }
    }

    private fun deleteTransaction(document: DocumentSnapshot, amount: Double?) {
        document.reference.delete().addOnSuccessListener {
            amount?.let { TransactionManager.updateBalance(activityFragment.db, -it.toFloat(), activityFragment.userID) }
            println("Document deleted successfully.")
        }.addOnFailureListener { e ->
            println("Error deleting document: $e")
        }
    }

    private fun setIconCard(categoryName : String?, frequentTransactionIv : ImageView) {
        val local_db = SQLiteDBHelper(requireContext(), null)
        val file_path = local_db.getCategoryImage(categoryName!!)
        val inputStream = requireContext().assets?.open("icons/${file_path}")
        val drawable = Drawable.createFromStream(inputStream, null)
        frequentTransactionIv.setImageDrawable(drawable)
        inputStream!!.close()
    }

    override fun onPause() {
        super.onPause()
        listener.remove()
    }
}
