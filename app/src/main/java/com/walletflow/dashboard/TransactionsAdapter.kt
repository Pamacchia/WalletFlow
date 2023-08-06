package com.walletflow.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.R
import com.walletflow.data.Transaction

class TransactionsAdapter(private var transactions : ArrayList<Transaction>, private val onClickListener : (Transaction) -> Unit)
    : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    /* ViewHolder for displaying header. */
    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tvDate = view.findViewById<TextView>(R.id.tvTransactionCardDate)
        val tvCategory = view.findViewById<TextView>(R.id.tvTransactionCardCategory)
        val tvAmount = view.findViewById<TextView>(R.id.tvTransactionCardAmount)
        val deleteButton = view.findViewById<Button>(R.id.btTransactionDelete)

        fun bind(transaction : Transaction, onClickListener : (Transaction) -> Unit) {
            tvDate.text = transaction.date
            tvCategory.text = transaction.category
            tvAmount.text = transaction.amount.toString()

            deleteButton.setOnClickListener {
                onClickListener
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
        val currentTransaction = transactions[position]
        holder.bind(currentTransaction, onClickListener)
    }

    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return transactions.size
    }

//    /* Updates header to display number of flowers when a flower is added or subtracted. */
//    fun addFriendToGroup(newGroupMember : User) : Boolean{
//        var result = false
//        if (!transactions.contains(newGroupMember)){
//            result = transactions.add(newGroupMember)
//            notifyDataSetChanged()
//        }
//        return result
//    }
//
//    fun removeFriendFromGroup(friend : User) : Boolean{
//        var result = transactions.remove(friend)
//        notifyDataSetChanged()
//        return result
//    }

    fun getTransactionsList() : ArrayList<Transaction>{
        return transactions
    }
}