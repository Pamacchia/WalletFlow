package com.walletflow

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
import com.walletflow.transactions.AddTransactionActivity
import java.lang.Double.min
import java.text.SimpleDateFormat
import java.util.Calendar

//TODO : Home balance and Objective Balance)

//TODO: You can spend min(balance, previousMonthEarning)
class HomeActivity : BaseActivity() {

    lateinit var earningBtn : Button
    lateinit var expenseBtn : Button
    lateinit var balanceTv : TextView
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

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_home
    }

    private fun loadHomeData(balanceTv : TextView){

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")

        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("username", userID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    balance = task.result.first().getDouble("balance")!!

                    //TODO: convert balance to shrinked format (K,M,B..)
                    balanceTv.text = balance.toString() + "" + "€"

                    updateTotalBudget()
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

                    totalBudget.text = " out of $budget$" //TODO: Euro

                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }

    }

    private fun loadFrequentTransactions(){

        val rootView = findViewById<LinearLayout>(R.id.layoutFrequentTransactions)


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
                        val tvDate = cardView.findViewById<TextView>(R.id.tvTransactionCardDate)
                        val tvCategory = cardView.findViewById<TextView>(R.id.tvTransactionCardCategory)
                        val tvAmount = cardView.findViewById<TextView>(R.id.tvTransactionCardAmount)

                        tvDate.text = "Test"
                        tvCategory.text = document.getString("category")
                        tvAmount.text = document.getDouble("amount").toString() + "$" // TODO: Euro
                        // Add the card view to the container layout



                        val addButton = cardView.findViewById<Button>(R.id.btTransactionAdd)
                        addButton.setOnClickListener {

                            val transaction = document.data

                            transaction["date"] = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().time)

                            db.collection("transactions")
                                .add(transaction)
                                .addOnSuccessListener { documentReference ->
                                    Log.d(
                                        this.localClassName,
                                        "DocumentSnapshot added with ID: " + documentReference.id
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        this.localClassName,
                                        "Error adding document",
                                        e
                                    )
                                }
                        }

                        val deleteButton = cardView.findViewById<Button>(R.id.btTransactionDelete)
                        deleteButton.setOnClickListener {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    // Document successfully deleted
                                    // Handle success or UI updates here
                                    println("Document deleted successfully.")
                                }
                                .addOnFailureListener { e ->
                                    // An error occurred while deleting the document
                                    // Handle the error here
                                    println("Error deleting document: $e")
                                }
                        }

                        rootView.addView(cardView)

                        Log.w(this.toString(), document.data.toString())
                    }

                } else {
                    Log.w(this.localClassName, "Error getting documents.", task.exception)
                }
            }

    }
}