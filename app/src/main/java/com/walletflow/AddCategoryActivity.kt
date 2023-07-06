package com.walletflow

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
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

//    TODO: Ereditarieta'

    lateinit var addCategoryBtn : Button
    lateinit var iconNameEt : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)


        val db = SQLiteDBHelper(this, null)

        val iconList: MutableList<Icon> = getIconList(ADD_CATEGORY_TYPE)

        loadIcons(iconList)

        addCategoryBtn = findViewById(R.id.btnAddCategory)

        addCategoryBtn.setOnClickListener {
            iconNameEt = findViewById(R.id.etCategoryName)
            db.addCategory(selected, iconNameEt.text.toString())
            finish()
        }
    }
}