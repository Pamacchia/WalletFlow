package com.walletflow.dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.walletflow.PieChartFragment
import com.walletflow.R
import com.walletflow.TransactionsFragment

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val PieChartFragment=PieChartFragment()
        val TransactionsFragment=TransactionsFragment()

        setCurrentFragment(PieChartFragment)
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            commit()
        }
}