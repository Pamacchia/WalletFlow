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

    val sharedPreferences: SharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val userID = sharedPreferences.getString("userID", "")
    val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val adapter = DashboardAdapter(supportFragmentManager)
        viewPager.adapter = adapter


    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_dashboard
    }
}