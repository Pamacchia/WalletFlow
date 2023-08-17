package com.walletflow.transactions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.R
import com.walletflow.data.Icon
import com.walletflow.utils.TransactionManager
import java.text.SimpleDateFormat
import java.util.Calendar

private const val DEFAULT = 1

class ChooseCategoryActivity : CategoryActivity() {

    lateinit var submitBtn: Button
    lateinit var btnAdd : Button
    lateinit var transactionType : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transactionType = intent.getStringExtra("typeName")!!
        val iconList: MutableList<Icon> = getIconList(DEFAULT, transactionType)
        submitBtn = findViewById(R.id.btnSubmitCategory)
        btnAdd = findViewById(R.id.btnAddNewCategory)
        loadIcons(iconList)

        submitBtn.setOnClickListener {
            selectCategory()
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_transaction_category_choose
    }

    override fun onRestart() {
        super.onRestart()
        val iconList: MutableList<Icon> = getIconList(DEFAULT, transactionType)
        loadIcons(iconList)
    }

    private fun selectCategory() {
        val resultIntent = Intent()
        resultIntent.putExtra("category", selected)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun loadIcons(iconList : MutableList<Icon>){
        super.loadIcons(iconList)
        btnAdd.setOnClickListener {
            selected = null
            val intent = Intent(this, AddCategoryActivity::class.java)
            intent.putExtra("typeName", transactionType)
            startActivity(intent)
        }
        submitBtn.isEnabled=false
    }

    override fun showSelected(v : View){
        super.showSelected(v)
        submitBtn.isEnabled=true
    }
}