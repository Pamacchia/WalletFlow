package com.walletflow

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResourceId())
    }

    abstract fun getLayoutResourceId(): Int

    fun openHomeActivity(item: MenuItem) {
        // Launch the HomeActivity
        if(!HomeActivity::class.java.name.contains(this.localClassName)){
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    fun openObjectivesActivity(item: MenuItem) {
        // Launch the HomeActivity
        if(!ObjectivesActivity::class.java.name.contains(this.localClassName)){
            val intent = Intent(this, ObjectivesActivity::class.java)
            startActivity(intent)
        }
    }
}