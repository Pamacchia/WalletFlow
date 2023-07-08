package com.walletflow

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.R
import com.walletflow.data.Icon
import com.walletflow.utils.FileManager
import com.walletflow.utils.SQLiteDBHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


private const val ADD_CATEGORY_TYPE = 0
class AddCategoryActivity : CategoryActivity() {

    lateinit var addCategoryBtn : Button
    lateinit var iconNameEt : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)


        val db = SQLiteDBHelper(this, null)

        val iconList: MutableList<Icon> = getIconList(ADD_CATEGORY_TYPE)

        loadIcons(iconList)

        addCategoryBtn = findViewById(R.id.btnAddCategory)
        iconNameEt = findViewById(R.id.etCategoryName)

        iconNameEt.addTextChangedListener(textWatcher)

        addCategoryBtn.setOnClickListener {
            db.addCategory(selected, iconNameEt.text.toString())
            finish()
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_add_category
    }

    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Implementation for afterTextChanged
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Implementation for beforeTextChanged
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            addCategoryBtn.isEnabled = iconNameEt.text.isNotEmpty()
        }
    }
}