package com.walletflow.friends

import android.os.Bundle
import com.walletflow.R
import com.walletflow.fragments.FragmentsActivity

class FriendsActivity : FragmentsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        firstFragment = FriendsListFragment { queryRef, operation ->
            firebaseSnapshotListener(queryRef, operation)
        }
        secondFragment = FriendsRequestFragment { queryRef, operation ->
            firebaseSnapshotListener(queryRef, operation)
        }
        super.onCreate(savedInstanceState)
    }
    override fun getLayoutResourceId(): Int {
        return R.layout.activity_friends
    }
}