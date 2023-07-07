package com.walletflow

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

//TODO : show in home balance and objective's balance (perché non lo calcoliamo più in maniera dinamica e l'utente può recuoerare soldi)
class HomeActivity : AppCompatActivity() {

    lateinit var earningBtn : Button
    lateinit var expenseBtn : Button
    lateinit var balanceTv : TextView
    companion object {
        const val EARNING_CONST = 1
        const val EXPENSE_CONST = -1
        const val EARNINGS = "earning"
        const val EXPENSES = "expense"
        const val SPEND = "spend"
        const val EARN = "earn"
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        earningBtn = findViewById(R.id.btnAddEarning)
        expenseBtn = findViewById(R.id.btnAddExpenses)
        balanceTv = findViewById(R.id.tvBalance)

        balanceTv.text = loadBalance().toString()

        earningBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("type", HomeActivity.EARNING_CONST)
            intent.putExtra("type_name", EARNINGS)
            intent.putExtra("type_verb", EARN)
            startActivity(intent)
        }

        expenseBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("type", HomeActivity.EXPENSE_CONST)
            intent.putExtra("type_name", EXPENSES)
            intent.putExtra("type_verb", SPEND)
            startActivity(intent)
        }
    }

    private fun loadBalance() : Float{

        var balance : Float = 0f
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")

        val db = FirebaseFirestore.getInstance()

        db.collection("users").whereEqualTo("username", userID).limit(1).get()
            .addOnSuccessListener { document ->
                balance = document.first().getString("balance")!!.toFloat()
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
            }

        return balance
    }
}