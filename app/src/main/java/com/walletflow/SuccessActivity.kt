package com.walletflow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.walletflow.data.Transaction

class SuccessActivity : BaseActivity() {

    private lateinit var dateTv : TextView
    private lateinit var categoryTv : TextView
    private lateinit var amountTv : TextView
    private lateinit var typeTv : TextView
    private lateinit var noteTv : TextView
    private lateinit var successIconTv : TextView
    private lateinit var successOperationTv : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dateTv = findViewById(R.id.tvSuccessDate)
        categoryTv = findViewById(R.id.tvSuccessCategory)
        amountTv = findViewById(R.id.tvSuccessAmount)
        typeTv = findViewById(R.id.tvSuccessType)
        noteTv = findViewById(R.id.tvSuccessNote)

        successIconTv = findViewById(R.id.tvSuccessIcon)
        successOperationTv = findViewById(R.id.tvSuccessOperation)

        val transaction : Transaction = intent.getSerializableExtra("transaction") as Transaction

        if(intent.getStringExtra("operation") == "Add"){
            successIconTv.text = "✅"
            successOperationTv.text = "Successfully added the following transaction: "
        } else {
            successIconTv.text = "\uD83D\uDDD1️"
            successOperationTv.text = "Successfully deleted the following transaction: "
        }

        dateTv.append(transaction.date)
        categoryTv.append(transaction.category)
        amountTv.append(transaction.amount.toString())
        typeTv.append(transaction.type)
        noteTv.append(transaction.note)
    }
    override fun getLayoutResourceId(): Int {
        return R.layout.activity_success
    }
}