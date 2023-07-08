package com.walletflow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AddObjectiveActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_objective)
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_add_objective
    }
}