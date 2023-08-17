package com.walletflow.friends

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.walletflow.BaseActivity
import com.walletflow.R

class FriendsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewPager: ViewPager = findViewById(R.id.viewPagerFriends)
        val adapter = FriendsAdapter(supportFragmentManager)
        viewPager.adapter = adapter
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_friends
    }
}