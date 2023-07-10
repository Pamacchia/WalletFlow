package com.walletflow.transactions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.walletflow.BaseActivity
import com.walletflow.R

class AddTransactionActivity : BaseActivity() {

    lateinit var titleTv : TextView
    lateinit var subtitleTv : TextView
    lateinit var amountTitleTv : TextView
    lateinit var amountEditText : EditText
    lateinit var noteEditText : EditText
    lateinit var recurrentCheck : CheckBox
    lateinit var chooseCategoryBtn : Button
    lateinit var selectorRecurrent : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_add)

        val type = intent.getIntExtra("type", 0)
        val typeName = intent.getStringExtra("type_name")
        val typeVerb = intent.getStringExtra("type_verb")

        titleTv = findViewById(R.id.tvTitle)
        subtitleTv = findViewById(R.id.tvSubTitle)
        amountTitleTv = findViewById(R.id.tvAmountTitle)
        amountEditText = findViewById(R.id.etAmount)
        noteEditText = findViewById(R.id.etNote)
        recurrentCheck = findViewById(R.id.cbRecurrent)
        chooseCategoryBtn = findViewById(R.id.btnChooseCategory)
        selectorRecurrent = findViewById(R.id.spRecurrent)

        titleTv.text = typeName?.uppercase()
        subtitleTv.append(typeName)

        amountTitleTv.append(typeVerb + "?")
        amountEditText.addTextChangedListener(textWatcher)
        chooseCategoryBtn.isEnabled = amountEditText.text.isNotEmpty()

        recurrentCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectorRecurrent.visibility = View.VISIBLE
            } else {
                selectorRecurrent.visibility = View.GONE
            }
        }

        chooseCategoryBtn.setOnClickListener {

            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userID = sharedPreferences.getString("userID", "")
            val amount = amountEditText.text.toString()
            val note = noteEditText.text.toString()
            val recurrent = recurrentCheck.isChecked
            var recurrency = "None"

            val intent = Intent(this, ChooseCategoryActivity::class.java)
            startActivity(intent)

            if(recurrent){
                recurrency = selectorRecurrent.selectedItem.toString()
            }

            if(amount.isEmpty()){
                Toast.makeText(this, "Please specify the amount", Toast.LENGTH_LONG).show()
            }
            else {
                val intent = Intent(this, ChooseCategoryActivity::class.java)
                intent.putExtra("amount", amount.toFloat()*type)
                intent.putExtra("note", note)
                intent.putExtra("recurrency", recurrency)
                intent.putExtra("userID", userID)
                intent.putExtra("typeName", typeName)
                startActivity(intent)
            }

        }

    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_transaction_add
    }

    val textWatcher = object : TextWatcher {
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