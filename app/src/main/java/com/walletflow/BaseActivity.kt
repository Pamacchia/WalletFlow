package com.walletflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.dashboard.DashboardActivity
import com.walletflow.friends.FriendsActivity
import com.walletflow.objectives.ObjectivesActivity
import com.walletflow.profile.ProfileActivity
import java.io.BufferedReader
import java.io.InputStreamReader


abstract class BaseActivity : AppCompatActivity() {

    private lateinit var bottomNavBar: BottomNavigationView

    val db = FirebaseFirestore.getInstance()
    lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResourceId())

        val filename = "userfile"
        val fileInputStream = openFileInput(filename)
        val inputStreamReader = InputStreamReader(fileInputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        val stringBuilder = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        userID = stringBuilder.toString()

        bufferedReader.close()
        inputStreamReader.close()
        fileInputStream.close()

        bottomNavBar = findViewById(R.id.bottom_navigation)

        bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> openActivity(HomeActivity::class.java)
                R.id.nav_dashboard -> openActivity(DashboardActivity::class.java)
                R.id.nav_objectives -> openActivity(ObjectivesActivity::class.java)
                R.id.nav_friends -> openActivity(FriendsActivity::class.java)
                R.id.nav_settings -> openActivity(ProfileActivity::class.java)
            }
            false
        }
    }

    abstract fun getLayoutResourceId(): Int

    private fun openActivity(activityClass: Class<*>) {
        if (!activityClass.name.contains(this.localClassName)) {
            val intent = Intent(this, activityClass)
            startActivity(intent)
            finish()
        }
    }
}