package com.walletflow.dashboard

import android.os.Bundle
import android.util.Log
import androidx.viewpager.widget.ViewPager
import com.walletflow.BaseActivity
import com.walletflow.R

class DashboardActivity : BaseActivity() {
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