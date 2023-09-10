package com.walletflow.transactions

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.walletflow.R
import com.walletflow.data.Icon
import com.walletflow.utils.SQLiteDBHelper

private const val DEFAULT = 0

class AddCategoryActivity : CategoryActivity() {

    lateinit var addCategoryBtn: Button
    lateinit var iconNameEt: EditText
    private lateinit var transactionType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = SQLiteDBHelper(this, null)

        transactionType = intent.getStringExtra("typeName")!!
        val iconList: MutableList<Icon> = getIconList(DEFAULT, transactionType)
        val nameList: MutableList<String> = db.getAllCategoryNames()
        loadIcons(iconList)

        addCategoryBtn = findViewById(R.id.btnAddCategory)
        iconNameEt = findViewById(R.id.etCategoryName)
        iconNameEt.addTextChangedListener(textWatcher)

        addCategoryBtn.setOnClickListener {
            if (selected == null) {
                Toast.makeText(this, "Select one icon", Toast.LENGTH_SHORT).show()
            } else if(isNotNameUnique(nameList)){
                Toast.makeText(this, "Icon name must be unique", Toast.LENGTH_SHORT).show()
            } else {
                db.addCategory(selected, iconNameEt.text.toString())
                finish()
            }
        }
    }

    private fun isNotNameUnique(nameList: MutableList<String>): Boolean {
        var uniqueName = false
        nameList.forEach { name ->
            uniqueName = name == iconNameEt.text.toString().trim()
            if (uniqueName){
                return uniqueName
            }
        }
        return uniqueName
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_transaction_category_add
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            addCategoryBtn.isEnabled = iconNameEt.text.trim().isNotEmpty()
        }
    }
}