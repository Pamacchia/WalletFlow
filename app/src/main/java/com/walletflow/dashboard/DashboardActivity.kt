package com.walletflow.dashboard

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.viewpager.widget.ViewPager
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.BaseActivity
import com.walletflow.R

class DashboardActivity : BaseActivity() {

    lateinit var sharedPreferences: SharedPreferences
    var userID : String? = null
    lateinit var db : FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val adapter = DashboardAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userID =  sharedPreferences.getString("userID", "")
        db = FirebaseFirestore.getInstance()
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_dashboard
    }
}