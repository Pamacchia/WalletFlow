package com.walletflow.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.R
import com.walletflow.data.Transaction
import com.walletflow.utils.TransactionManager

class TransactionsAdapter(private var transactions : MutableMap<String, Transaction>, val collection : CollectionReference)
    : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    /* ViewHolder for displaying header. */
    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tvDate = view.findViewById<TextView>(R.id.tvTransactionCardDate)
        val tvCategory = view.findViewById<TextView>(R.id.tvTransactionCardCategory)
        val tvAmount = view.findViewById<TextView>(R.id.tvTransactionCardAmount)
        val deleteButton = view.findViewById<Button>(R.id.btTransactionDelete)

        fun bind(transaction : Pair<String, Transaction>, onClick : (Pair<String, Transaction>)->Unit) {
            tvDate.text = transaction.second.date
            tvCategory.text = transaction.second.category
            tvAmount.text = transaction.second.amount.toString() + "$"

            deleteButton.setOnClickListener {
                transaction?.let{
                    onClick(it)
                }
            }
        }
    }

    /* Inflates view and returns HeaderViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_cardview, parent, false)
        return TransactionViewHolder(view)
    }

    /* Binds number of flowers to the header. */
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentTransaction = transactions.toList()[position]
        holder.bind(currentTransaction) { pair -> deleteTransactions(pair) }
    }

    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return transactions.size
    }

    private fun deleteTransactions(transaction : Pair<String,Transaction>){
        val document = collection.document(transaction.first)
        document.delete()
            .addOnSuccessListener{
                // Document successfully deleted
                // Handle success or UI updates here
                TransactionManager.updateBalance(
                    FirebaseFirestore.getInstance(),
                    -transaction.second.amount!!.toFloat(),
                    transaction.second.user
                )
                println("Document deleted successfully.")
                transactions.remove(transaction.first)
                notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                // An error occurred while deleting the document
                // Handle the error here
                println("Error deleting document: $e")
            }
    }

    fun getTransactionsList() : MutableMap<String, Transaction>{
        return transactions
    }
}