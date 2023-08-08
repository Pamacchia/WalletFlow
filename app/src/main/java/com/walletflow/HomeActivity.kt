package com.walletflow

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
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
import java.text.SimpleDateFormat
import java.util.Calendar


//TODO : Home balance and Objective Balance

class HomeActivity : BaseActivity() {

    private lateinit var earningBtn: Button
    private lateinit var expenseBtn: Button
    private lateinit var balanceTv: TextView
    private lateinit var expensesTv: TextView
    private lateinit var objectiveMoneyTv: TextView
    private lateinit var totalBudget: TextView
    private lateinit var greetingUser: TextView

    private var balance: Double = 0.0

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
        objectiveMoneyTv = findViewById(R.id.tvObjectiveMoney)
        greetingUser = findViewById(R.id.tvGreetingUser)

        loadHomeData()
        loadFrequentTransactions()

        earningBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("type", EARNING_CONST)
            intent.putExtra("type_name", EARNINGS)
            intent.putExtra("type_verb", EARN)
            startActivity(intent)
        }

        expenseBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("type", EXPENSE_CONST)
            intent.putExtra("type_name", EXPENSES)
            intent.putExtra("type_verb", SPEND)
            startActivity(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        loadHomeData()
        loadFrequentTransactions()
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_home
    }

    @SuppressLint("SetTextI18n")
    private fun loadHomeData() {

        "Hello, $userID".also { greetingUser.text = it }

        db.collection("users").whereEqualTo("username", userID).get(Source.SERVER)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    balance = task.result.first().getDouble("balance")!!
                    balanceTv.text = "${StringHelper.getShrunkForm(balance)}€"
                    updateTotalBudget()
                    updateExpenses()
                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun updateTotalBudget() {

        val calendar = Calendar.getInstance()
        val dateUpper = SimpleDateFormat("yyyy-MM").format(calendar.time)
        calendar.add(Calendar.MONTH, -1)
        val dateLower = SimpleDateFormat("yyyy-MM").format(calendar.time)

        var budget = 0.0
        var thisMonthExpense = 0.0

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
                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }

        db.collection("transactions")
            .whereEqualTo("user", userID)
            .whereEqualTo("type", "expense")
            .whereGreaterThanOrEqualTo("date", dateUpper)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        thisMonthExpense += kotlin.math.abs(document.getDouble("amount")!!)
                    }

                    val thisMonthBudget: Double

                    if (budget == 0.0) {
                        budget = balance
                        thisMonthBudget = balance + thisMonthExpense
                    } else {
                        thisMonthBudget = budget + thisMonthExpense
                    }

                    totalBudget.text = " ${StringHelper.getShrunkForm(budget)}$" //TODO: Euro
                    val progressBarContainer = findViewById<FrameLayout>(R.id.budgetProgressBar)
                    val difference = kotlin.math.abs(budget - thisMonthBudget)
                    val relativeDifference = difference / thisMonthBudget
                    val desiredWidthInDp = 310
                    val minProgressBarWidthInPx = 1
                    val reversedRelativeDifference = 1 - relativeDifference
                    val newWidthInPx = (minProgressBarWidthInPx + (reversedRelativeDifference * (desiredWidthInDp - minProgressBarWidthInPx)) * resources.displayMetrics.density).toInt()

                    val layoutParams = progressBarContainer.layoutParams
                    layoutParams.width = newWidthInPx
                    progressBarContainer.layoutParams = layoutParams

                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }

    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun updateExpenses() {

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

                    expensesTv.text =
                        "${StringHelper.getShrunkForm(kotlin.math.abs(expenses))}$" //TODO: Euro

                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun loadFrequentTransactions() {

        val rootView = findViewById<LinearLayout>(R.id.layoutFrequentTransactions)
        rootView.removeAllViews()

        db.collection("frequentTransactions").whereEqualTo("user", userID).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    for (document in task.result) {

                        val inflater = LayoutInflater.from(this)
                        val cardView = inflater.inflate(
                            R.layout.frequent_transaction_cardview, rootView, false
                        ) as CardView
                        val tvNote =
                            cardView.findViewById<TextView>(R.id.tvFrequentTransactionCardNote)
                        val tvType =
                            cardView.findViewById<TextView>(R.id.tvFrequentTransactionCardType)
                        val tvAmount =
                            cardView.findViewById<TextView>(R.id.tvFrequentTransactionCardAmount)

                        tvNote.text = document.getString("note")
                        tvType.text = document.getString("type")
                        tvAmount.text = document.getDouble("amount").toString() + "$" // TODO: Euro

                        val addButton = cardView.findViewById<Button>(R.id.btFrequentTransactionAdd)

                        addButton.setOnClickListener {
                            createAlertForFrequentTransaction(document, db, userID, true)
                        }

                        val deleteButton =
                            cardView.findViewById<Button>(R.id.btFrequentTransactionDelete)
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

    @SuppressLint("SimpleDateFormat")
    private fun createAlertForFrequentTransaction(
        document: QueryDocumentSnapshot, db: FirebaseFirestore, userID: String?, add: Boolean
    ) {

        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Confirm")
        alert.setMessage("Are you sure?")
        alert.setPositiveButton(
            "Yes"
        ) { _, _ ->

            if (add) {
                val transaction = Transaction(
                    document.getDouble("amount"),
                    document.getString("category"),
                    document.getString("note"),
                    document.getString("type"),
                    document.getString("user"),
                    SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().time)
                )
                TransactionManager.addTransactionRecordToDB(db, transaction, document, userID)
            } else {
                TransactionManager.deleteFrequentTransactionRecordFromDB(document)
            }

            finish()
            val intent = Intent(this, SuccessActivity::class.java)
            startActivity(intent)

        }
        alert.setNegativeButton(
            "No"
        ) { dialog, _ ->
            dialog.cancel()
        }
        alert.show()
    }

}