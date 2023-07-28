package com.walletflow

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.Source
import com.walletflow.data.Transaction
import com.walletflow.transactions.AddTransactionActivity
import com.walletflow.utils.StringHelper
import com.walletflow.utils.TransactionManager
import java.lang.Double.min
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.Calendar


//TODO : Home balance and Objective Balance

class HomeActivity : BaseActivity() {

    lateinit var earningBtn : Button
    lateinit var expenseBtn : Button
    lateinit var balanceTv : TextView
    lateinit var expensesTv : TextView
    lateinit var savingsTv : TextView
    lateinit var totalBudget : TextView

    var balance : Double = 0.0
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



        earningBtn = findViewById(R.id.btnAddEarning)
        expenseBtn = findViewById(R.id.btnAddExpenses)
        totalBudget = findViewById(R.id.tvTotalBudget)
        balanceTv = findViewById(R.id.tvBalance)
        expensesTv = findViewById(R.id.tvExpenses)
        savingsTv = findViewById(R.id.tvExpenses)
        loadHomeData(balanceTv)
        loadFrequentTransactions()

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

    override fun onRestart() {
        super.onRestart()
        loadHomeData(balanceTv)
        loadFrequentTransactions()
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_home
    }

    private fun loadHomeData(balanceTv : TextView){

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")

        Log.w(this.toString(), "FUCKING WAIT")
        Thread.sleep(150L)
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("username", userID)
            .get(Source.SERVER)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    balance = task.result.first().getDouble("balance")!!

                    balanceTv.text = StringHelper.getShrunkForm(balance) + "" + "€"

                    Log.w(this.toString(), task.result.first().getDouble("balance").toString())

                    updateTotalBudget()
                    updateExpenses()
                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }
    }

    private fun updateTotalBudget(){
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")

        val db = FirebaseFirestore.getInstance()

        val calendar = Calendar.getInstance()
        val dateUpper = SimpleDateFormat("yyyy-MM").format(calendar.time)
        calendar.add(Calendar.MONTH, -1)
        val dateLower = SimpleDateFormat("yyyy-MM").format(calendar.time)

        var budget : Double = 0.0

        db.collection("transactions")
            .whereEqualTo("user", userID)
            .whereEqualTo("type", "earning")
            .whereGreaterThanOrEqualTo("date", dateLower)
            .whereLessThan("date", dateUpper)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        budget += document.getDouble("amount")!!
                    }

                    budget = if (budget == 0.0) {
                        balance
                    } else {
                        min(balance, budget)
                    }

                    totalBudget.text = " out of ${StringHelper.getShrunkForm(budget)}$" //TODO: Euro

                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }

    }

    private fun updateExpenses(){
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")

        val db = FirebaseFirestore.getInstance()

        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy").format(calendar.time)

        var expenses = 0.0

        db.collection("transactions")
            .whereEqualTo("user", userID)
            .whereEqualTo("type", "expense")
            .whereGreaterThanOrEqualTo("date", date)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        expenses += document.getDouble("amount")!!
                    }

                    expensesTv.text = "${StringHelper.getShrunkForm(abs(expenses))}$" //TODO: Euro

                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }

    }

    private fun loadFrequentTransactions(){

        val rootView = findViewById<LinearLayout>(R.id.layoutFrequentTransactions)
        rootView.removeAllViews()

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")

        val db = FirebaseFirestore.getInstance()

        db.collection("frequentTransactions")
            .whereEqualTo("user", userID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    for (document in task.result) {

                        val inflater = LayoutInflater.from(this)
                        val cardView = inflater.inflate(R.layout.frequent_transaction_cardview, rootView, false) as CardView
                        val tvNote = cardView.findViewById<TextView>(R.id.tvFrequentTransactionCardNote)
                        val tvType = cardView.findViewById<TextView>(R.id.tvFrequentTransactionCardType)
                        val tvAmount = cardView.findViewById<TextView>(R.id.tvFrequentTransactionCardAmount)

                        tvNote.text = document.getString("note")
                        tvType.text = document.getString("type")
                        tvAmount.text = document.getDouble("amount").toString() + "$" // TODO: Euro
                        // Add the card view to the container layout

                        val addButton = cardView.findViewById<Button>(R.id.btFrequentTransactionAdd)

                        addButton.setOnClickListener {
                            createAlertForFrequentTransaction(document, db, userID, true)
                        }

                        val deleteButton = cardView.findViewById<Button>(R.id.btFrequentTransactionDelete)
                        deleteButton.setOnClickListener {
                            createAlertForFrequentTransaction(document, db, userID, false)
                        }

                        rootView.addView(cardView)

                        Log.w(this.toString(), document.data.toString())
                    }

                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }

    }

    private fun createAlertForFrequentTransaction(
        document: QueryDocumentSnapshot,
        db: FirebaseFirestore,
        userID: String?,
        add : Boolean
    ) {

        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Add")
        alert.setMessage("Are you sure you want to add?")
        alert.setPositiveButton(
            "Yes"
        ) { _, _ ->

            if(add) {
                val transaction = Transaction(
                    document.getDouble("amount"),
                    document.getString("category"),
                    document.getString("note"),
                    document.getString("type"),
                    document.getString("user"),
                    SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().time)
                )
                TransactionManager.addTransactionRecordToDB(db, transaction, document, userID)
            }
            else {
                TransactionManager.deleteFrequentTransactionRecordFromDB(document)
            }

            finish()
            val intent = Intent(this, SuccessActivity::class.java)
            startActivity(intent)

        }
        alert.setNegativeButton("No"
        ) { dialog, _ ->
            dialog.cancel()
        }
        alert.show()
    }



}