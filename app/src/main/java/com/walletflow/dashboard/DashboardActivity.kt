package com.walletflow.dashboard

import android.os.Bundle
import com.walletflow.R
import com.walletflow.fragments.FragmentsActivity

class DashboardActivity : FragmentsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        firstFragment = TransactionsFragment { queryRef, operation ->
            firebaseSnapshotListener(queryRef, operation)
        }
        secondFragment = PieChartFragment { queryRef, operation ->
            firebaseSnapshotListener(queryRef, operation)
        }

        super.onCreate(savedInstanceState)
    }
    override fun getLayoutResourceId(): Int {
        return R.layout.activity_dashboard
    }
}