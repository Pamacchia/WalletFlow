package com.walletflow.fragments

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.walletflow.BaseActivity
import com.walletflow.R

abstract class FragmentsActivity : BaseActivity() {

    protected lateinit var firstFragment: Fragment
    protected lateinit var secondFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val dotLayout : LinearLayout = findViewById(R.id.dotLayout)
        val fragmentLit: MutableList<Fragment> =
            mutableListOf(firstFragment, secondFragment)

        val adapter = FragmentAdapter(supportFragmentManager, fragmentLit)
        viewPager.adapter = adapter

        val dots: MutableList<ImageView> = addDots(adapter, dotLayout)

        dots[0].setImageResource(R.drawable.dot_active)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                for (i in 0 until adapter.count) {
                    dots[i].setImageResource(if (i == position) R.drawable.dot_active else R.drawable.dot_inactive)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun addDots(
        adapter: FragmentPagerAdapter,
        dotLayout: LinearLayout
    ): MutableList<ImageView> {
        val dots: MutableList<ImageView> = mutableListOf()
        val factor: Float = this.resources.displayMetrics.density
        val layoutParams = LinearLayout.LayoutParams(15 * factor.toInt(), 15 * factor.toInt())
        layoutParams.rightMargin = 10 * factor.toInt()

        for (i in 0 until adapter.count) {
            val dot = ImageView(this)
            dot.layoutParams = layoutParams
            dot.setImageResource(R.drawable.dot_inactive)
            dotLayout.addView(dot)
            dots.add(dot)
        }
        return dots
    }

    fun firebaseSnapshotListener(
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

}