package com.walletflow.dashboard

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.github.mikephil.charting.charts.PieChart
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.walletflow.BaseActivity
import com.walletflow.R

class DashboardActivity : BaseActivity() {

    private lateinit var pieChartFragment : PieChartFragment
    private lateinit var transactionFragment : TransactionsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transactionFragment = TransactionsFragment { queryRef, operation ->
            firebaseUserTransactionSnapshotListenere(queryRef, operation)
        }
        pieChartFragment = PieChartFragment{ queryRef, operation ->
            firebaseUserTransactionSnapshotListenere(queryRef, operation)
        }
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val fragmentLit : MutableList<Fragment> = mutableListOf(transactionFragment,pieChartFragment)

        val adapter = DashboardAdapter(supportFragmentManager, fragmentLit)
        viewPager.adapter = adapter

    }

    private fun firebaseUserTransactionSnapshotListenere(queryRef : Query, operation : (List<DocumentSnapshot>)->(Unit)){
        queryRef
            .addSnapshotListener (this) { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                querySnapshot?.let { it ->
                    operation(it.documents)
                }
            }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_dashboard
    }
}