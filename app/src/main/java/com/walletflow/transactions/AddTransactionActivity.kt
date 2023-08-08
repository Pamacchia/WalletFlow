package com.walletflow.transactions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.walletflow.BaseActivity
import com.walletflow.R

class AddTransactionActivity : BaseActivity() {

    private lateinit var titleTv: TextView
    private lateinit var subtitleTv: TextView
    private lateinit var amountTitleTv: TextView
    lateinit var amountEditText: EditText
    private lateinit var noteEditText: EditText
    private lateinit var frequentCheck: CheckBox
    lateinit var chooseCategoryBtn: Button

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getIntExtra("type", 0)
        val typeName = intent.getStringExtra("type_name")
        val typeVerb = intent.getStringExtra("type_verb")

        titleTv = findViewById(R.id.tvTitle)
        subtitleTv = findViewById(R.id.tvSubTitle)
        amountTitleTv = findViewById(R.id.tvAmountTitle)
        amountEditText = findViewById(R.id.etAmount)
        noteEditText = findViewById(R.id.etNote)
        frequentCheck = findViewById(R.id.cbFrequent)
        chooseCategoryBtn = findViewById(R.id.btnChooseCategory)

        titleTv.text = typeName?.uppercase()
        subtitleTv.text = "Insert a new $typeName"

        amountTitleTv.text = "How much did you $typeVerb?"
        amountEditText.addTextChangedListener(textWatcher)
        chooseCategoryBtn.isEnabled = amountEditText.text.isNotEmpty()

        chooseCategoryBtn.setOnClickListener {

            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userID = sharedPreferences.getString("userID", "")
            val amount = amountEditText.text.toString()
            val note = noteEditText.text.toString()
            val frequent = frequentCheck.isChecked

            if (amount.isEmpty()) {
                Toast.makeText(this, "Please specify the amount", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, ChooseCategoryActivity::class.java)
                intent.putExtra("amount", amount.toFloat() * type)
                intent.putExtra("note", note)
                intent.putExtra("frequent", frequent)
                intent.putExtra("userID", userID)
                intent.putExtra("typeName", typeName)
                startActivity(intent)
                finish()
            }

        }

    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_transaction_add
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Implementation for afterTextChanged
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Implementation for beforeTextChanged
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            chooseCategoryBtn.isEnabled = amountEditText.text.isNotEmpty()
        }
    }

}