package com.walletflow.friends

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.walletflow.BaseActivity
import com.walletflow.R

class FriendsActivity : BaseActivity() {

    private lateinit var friendsRequestFragment: FriendsRequestFragment
    private lateinit var friendsListFragment: FriendsListFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        friendsRequestFragment = FriendsRequestFragment { queryRef, operation ->
            firebaseUserFriendsSnapshotListener(queryRef, operation)
        }
        friendsListFragment = FriendsListFragment { queryRef, operation ->
            firebaseUserFriendsSnapshotListener(queryRef, operation)
        }
        val viewPager: ViewPager = findViewById(R.id.viewPagerFriends)
        val fragmentList: MutableList<Fragment> =
            mutableListOf(friendsListFragment, friendsRequestFragment)

        val adapter = FriendsAdapter(supportFragmentManager, fragmentList)
        viewPager.adapter = adapter
    }

    private fun firebaseUserFriendsSnapshotListener(
        queryRef: Query,
        operation: (List<DocumentSnapshot>) -> (Unit)
    ) {
        queryRef.addSnapshotListener(this) { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                querySnapshot?.let {
                    operation(it.documents)
                }
            }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_friends
    }
}