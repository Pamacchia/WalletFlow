package com.walletflow.transactions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.walletflow.R
import com.walletflow.data.Icon

private const val DEFAULT = 1

class ChooseCategoryActivity : CategoryActivity() {

    private lateinit var submitBtn: Button
    private lateinit var btnAdd: Button
    private lateinit var transactionType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        submitBtn = findViewById(R.id.btnSubmitCategory)
        btnAdd = findViewById(R.id.btnAddNewCategory)
        transactionType = intent.getStringExtra("typeName")!!

        val iconList: MutableList<Icon> = getIconList(DEFAULT, transactionType)

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

    override fun loadIcons(iconList: MutableList<Icon>) {
        super.loadIcons(iconList)
        btnAdd.setOnClickListener {
            selected = null
            val intent = Intent(this, AddCategoryActivity::class.java)
            intent.putExtra("typeName", transactionType)
            startActivity(intent)
        }
        submitBtn.isEnabled = false
    }

    override fun showSelected(v: View) {
        super.showSelected(v)
        submitBtn.isEnabled = true
    }
}