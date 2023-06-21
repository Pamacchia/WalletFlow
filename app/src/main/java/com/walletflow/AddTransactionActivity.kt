package com.walletflow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView

class AddTransactionActivity : AppCompatActivity() {

    lateinit var titleTv : TextView
    lateinit var subtitleTv : TextView
    lateinit var amountTitleTv : TextView
    lateinit var amountEditText : EditText
    lateinit var noteEditText : EditText
    lateinit var recurrentCheck : CheckBox
    lateinit var chooseCategoryBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

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

        titleTv.text = typeName?.uppercase()
        subtitleTv.append(typeName)
        amountTitleTv.append(typeVerb + "?")

    }
}