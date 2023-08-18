package com.walletflow

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.data.Participant
import com.walletflow.data.Transaction
import com.walletflow.data.User
import com.walletflow.transactions.AddTransactionActivity
import com.walletflow.utils.SQLiteDBHelper
import com.walletflow.utils.StringHelper
import com.walletflow.utils.TransactionManager
import java.text.SimpleDateFormat
import java.util.Calendar

@SuppressLint("SetTextI18n", "SimpleDateFormat")
class HomeActivity : BaseActivity() {

    companion object {
        const val EARNING_CONST = 1
        const val EXPENSE_CONST = -1
        const val EARNINGS = "earning"
        const val EXPENSES = "expense"
        const val SPEND = "spend"
        const val EARN = "earn"
    }

    private lateinit var earningBtn: Button
    private lateinit var expenseBtn: Button
    private lateinit var balanceTv: TextView
    private lateinit var expensesTv: TextView
    private lateinit var objectiveMoneyTv: TextView
    private lateinit var totalBudget: TextView
    private lateinit var greetingUser: TextView
    private lateinit var progressBarContainer : FrameLayout

    private var balance: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        earningBtn = findViewById(R.id.btnAddEarning)
        expenseBtn = findViewById(R.id.btnAddExpenses)
        totalBudget = findViewById(R.id.tvTotalBudget)
        balanceTv = findViewById(R.id.tvBalance)
        expensesTv = findViewById(R.id.tvExpenses)
        objectiveMoneyTv = findViewById(R.id.tvObjectiveSavings)
        greetingUser = findViewById(R.id.tvGreetingUser)
        progressBarContainer = findViewById(R.id.budgetProgressBar)

        "Hello, $userID".also { greetingUser.text = it }

        balanceListener()
        objectiveBudgetListener()
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

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_home
    }

    private fun balanceListener() {
        db.collection("users").whereEqualTo("username", userID)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this, "Error loading data", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            querySnapshot?.let {
                val user = it.toObjects(User::class.java).first()
                balance = user.balance
                balanceTv.text = StringHelper.getShrunkForm(balance) + "" + "€"
                updateTotalBudget()
            }
        }
    }

    private fun objectiveBudgetListener() {
        db.collection("participants").whereEqualTo("participant", userID)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                querySnapshot?.let {
                    var objectiveSavedMoney = 0.0
                    val userParticipantToObjectives = it.toObjects(Participant::class.java)
                    for (userParticipantToObjective in userParticipantToObjectives) {
                        objectiveSavedMoney += userParticipantToObjective.saved
                    }
                    objectiveMoneyTv.text = StringHelper.getShrunkForm(objectiveSavedMoney) + "" + "€"
                }
            }
    }

    private fun updateTotalBudget() {
        val calendar = Calendar.getInstance()
        val dateUpper = SimpleDateFormat("yyyy-MM").format(calendar.time)
        calendar.add(Calendar.MONTH, -1)
        val dateLower = SimpleDateFormat("yyyy-MM").format(calendar.time)
        db.collection("transactions")
            .whereEqualTo("user", userID)
            .whereGreaterThanOrEqualTo("date", dateLower)
//            .whereLessThan("date", dateUpper)
            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                querySnapshot?.let { it ->
                    var budget = 0.0
                    var thisMonthExpense = 0.0
                    val transactions = it.toObjects(Transaction::class.java)
                    transactions
                        .forEach{ transaction ->
                            if (transaction.type=="earning" && transaction.date.toString()<dateUpper)
                                budget += transaction.amount!!
                            else if(transaction.type == "expense")
                                thisMonthExpense += kotlin.math.abs(transaction.amount!!)
                        }

                    if (budget==0.0)
                        budget = balance

                    expensesTv.text ="${StringHelper.getShrunkForm(thisMonthExpense)}€"
                    totalBudget.text=" ${StringHelper.getShrunkForm(budget)}€"
                    showProgressBar(budget, budget + thisMonthExpense)
                }
            }
    }

    private fun showProgressBar(budget : Double, thisMonthBudget: Double) {
        var difference = thisMonthBudget - budget
        val relativeDifference = difference / thisMonthBudget
        val desiredWidthInDp = 310
        val minProgressBarWidthInPx = 1
        val reversedRelativeDifference = 1 - relativeDifference
        val newWidthInPx =
            (minProgressBarWidthInPx + (reversedRelativeDifference * (desiredWidthInDp - minProgressBarWidthInPx)) * resources.displayMetrics.density).toInt()

        val layoutParams = progressBarContainer.layoutParams
        layoutParams.width = newWidthInPx
        progressBarContainer.layoutParams = layoutParams
    }

    private fun loadFrequentTransactions() {
        val rootView = findViewById<LinearLayout>(R.id.layoutFrequentTransactions)

        db.collection("frequentTransactions")
            .whereEqualTo("user", userID)
            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                querySnapshot?.let { it ->
                    rootView.removeAllViews()
                    it.documents.forEach{ frequentTransactionDocumentSnapshot ->
                        val frequentTransaction = frequentTransactionDocumentSnapshot.toObject(Transaction::class.java)
                        val inflater = LayoutInflater.from(this)
                        val cardView = inflater.inflate(R.layout.frequent_transaction_cardview,
                            rootView,
                            false
                        ) as CardView
                        val tvNote = cardView.findViewById<TextView>(R.id.tvFrequentTransactionCardNote)
                        val tvType = cardView.findViewById<TextView>(R.id.tvFrequentTransactionCardType)
                        val tvAmount = cardView.findViewById<TextView>(R.id.tvFrequentTransactionCardAmount)
                        val ivCategory = cardView.findViewById<ImageView>(R.id.frequentTransactionIv)

                        tvNote.text = frequentTransaction!!.note
                        tvType.text = frequentTransaction!!.type
                        tvAmount.text = frequentTransaction!!.amount.toString() + "€"

                        setIconCard(frequentTransaction.category, ivCategory)

                        val addButton = cardView.findViewById<Button>(R.id.btFrequentTransactionAdd)
                        addButton.setOnClickListener {
                            createAlertForFrequentTransaction(frequentTransactionDocumentSnapshot, db, userID, true, "Confirm adding operation")
                        }

                        val deleteButton =
                            cardView.findViewById<Button>(R.id.btFrequentTransactionDelete)
                        deleteButton.setOnClickListener {
                            createAlertForFrequentTransaction(frequentTransactionDocumentSnapshot, db, userID, false, "Confirm deleting operation")
                        }

                        rootView.addView(cardView)
                    }
                }

            }
    }

    private fun createAlertForFrequentTransaction(
        frequentTransactionDocumentSnapshot: DocumentSnapshot,
        db: FirebaseFirestore,
        userID: String?,
        add: Boolean,
        message: String
    ) {

        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle(message)
        alert.setMessage("Are you sure?")
        alert.setPositiveButton(
            "Yes"
        ) { _, _ ->

            val transaction = frequentTransactionDocumentSnapshot.toObject(Transaction::class.java)
            transaction!!.date = SimpleDateFormat("yyyy-MM-dd HH:mm").format(
                Calendar.getInstance().time
            )

            if (add) {
                TransactionManager.addTransactionRecordToDB(db, transaction, userID)
            } else {
                TransactionManager.deleteFrequentTransactionRecordFromDB(frequentTransactionDocumentSnapshot)
            }
        }
        alert.setNegativeButton(
            "No"
        ) { dialog, _ ->
            dialog.cancel()
        }
        alert.show()
    }

    private fun setIconCard(categoryName : String?, frequentTransactionIv : ImageView) {
        val local_db = SQLiteDBHelper(this, null)
        val file_path = local_db.getCategoryImage(categoryName!!)
        val inputStream = this.assets?.open("icons/${file_path}")
        val drawable = Drawable.createFromStream(inputStream, null)
        frequentTransactionIv.setImageDrawable(drawable)
        inputStream!!.close()
    }

}