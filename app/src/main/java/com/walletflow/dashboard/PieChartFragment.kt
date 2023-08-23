package com.walletflow.dashboard

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.utils.SQLiteDBHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.abs


class PieChartFragment(
    private val listener : (Query, (List<DocumentSnapshot>)->(Unit)) -> Unit
) : Fragment() {

    private lateinit var fragmentActivity : BaseActivity
    private lateinit var queryRef : Query
    private lateinit var pieChart: PieChart
    private lateinit var filterMonthTv: TextView
    private lateinit var filterYearTv: TextView
    private lateinit var totalSpentTv: TextView
    private lateinit var savedRecapTv: TextView
    private lateinit var dashboardAdviceSavingsCard: MaterialCardView
    private lateinit var emojiSavingTv : TextView
    private lateinit var adviceCategoryTv : TextView
    private lateinit var categoryIv : ImageView
    private lateinit var date : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pie_chart, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentActivity = (activity as BaseActivity)
        queryRef = fragmentActivity.db.collection("transactions")
            .whereEqualTo("user", fragmentActivity.userID)
        initViews(view)
        initListeners()

        date = getDateStringWithOffset(-1, Calendar.MONTH)

        queryRef.whereGreaterThan("date", date).get().addOnSuccessListener {
            listener(it.query) { documents -> filterRecordsByDate(documents) }
        }
    }

    private fun initViews(view: View) {
        pieChart = view.findViewById(R.id.pieChartCategories)
        filterMonthTv = view.findViewById(R.id.tvFilterDashboardMonth)
        filterYearTv = view.findViewById(R.id.tvFilterDashboardYear)
        totalSpentTv = view.findViewById(R.id.tvTotalSpent)
        savedRecapTv = view.findViewById(R.id.tvAdviceSaving)
        dashboardAdviceSavingsCard = view.findViewById(R.id.cardDashboardAdviceSaved)
        emojiSavingTv = view.findViewById(R.id.tvEmojiSaving)
        adviceCategoryTv = view.findViewById(R.id.tvAdviceCategory)
        categoryIv = view.findViewById(R.id.categoryImageView)
        filterMonthTv.setTypeface(null, Typeface.BOLD)
    }

    private fun initListeners() {
        filterMonthTv.setOnClickListener {
            updateFilterViews(true)
            date = getDateStringWithOffset(-1, Calendar.MONTH)
            listener(queryRef.whereGreaterThan("date", date))
            { documents -> filterRecordsByDate(documents) }
        }

        filterYearTv.setOnClickListener {
            updateFilterViews(false)
            date = getDateStringWithOffset(-1, Calendar.YEAR)
            listener(queryRef.whereGreaterThan("date", date))
                    { documents -> filterRecordsByDate(documents) }
        }
    }

    private fun updateFilterViews(isMonthFilter: Boolean) {
        filterYearTv.setTypeface(null, if (isMonthFilter) Typeface.NORMAL else Typeface.BOLD)
        filterMonthTv.setTypeface(null, if (isMonthFilter) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun getDateStringWithOffset(offset: Int, field: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(field, offset)
        return SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
    }

    private fun filterRecordsByDate(documents : List<DocumentSnapshot>) {
        val processedRecords: MutableList<Map<String, Any>?> = mutableListOf()
        var totalExpense = 0.0
        var totalEarning = 0.0

        documents.forEach { document ->
                val amount = document.getDouble("amount").toString().toDouble()
                val type = document.getString("type")

                if (type == "expense") {
                    processedRecords.add(document.data)
                    totalExpense += amount
                } else {
                    totalEarning += amount
                }
            }

            val percentage = calculateSavingsPercentage(totalEarning, totalExpense)
            val formattedPercentage = String.format("%.2f%%", percentage)
            val summaryText = "You earned $totalEarning and you spent ${abs(totalExpense)}$, " +
                    "meaning you saved $formattedPercentage of your earnings since $date."

            if(percentage > 10) {
                dashboardAdviceSavingsCard.strokeColor = resources.getColor(R.color.nordGreen)
                emojiSavingTv.text = "\uD83E\uDD11"
            } else if (percentage < 0) {
                dashboardAdviceSavingsCard.strokeColor = resources.getColor(R.color.nordRed)
                emojiSavingTv.text = "\uD83E\uDD2C"
            }

            savedRecapTv.text = summaryText
            totalSpentTv.text = "Total sum of expenses: ${abs(totalExpense)}€"
            showPieChart(processedRecords)
    }

    private fun calculateSavingsPercentage(totalEarning: Double, totalExpense: Double): Double {
        return if (totalEarning != 0.0) {
            100 * (totalEarning + totalExpense) / totalEarning
        } else {
            0.0
        }
    }


    private fun showPieChart(processedRecords: MutableList<Map<String, Any>?>) {
        val pieEntries = generatePieEntries(processedRecords)
        val colorArray = getColorArray(pieEntries.size)

        var maxEntry: PieEntry? = null
        var maxAmount = Float.MIN_VALUE
        var totalSum = 0f

        for (entry in pieEntries) {
            totalSum += entry.value
            if (entry.value > maxAmount) {
                maxAmount = entry.value
                maxEntry = entry
            }
        }

        val maxEntryPercentage : Float
        if (maxEntry!!.label != "None") {
            maxEntryPercentage = (maxAmount / totalSum) * 100
            val roundedMaxEntryPercentage = String.format("%.2f", maxEntryPercentage)

            adviceCategoryTv.text = "You spent the most on: ${maxEntry.label}. Amount spent: $maxAmount$. Percentage: $roundedMaxEntryPercentage%"

            setIconCard(maxEntry)
        }
        else{
            adviceCategoryTv.text = "You have no expenses, yet"
        }
        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.valueTextSize = 15f
        pieDataSet.colors = colorArray.asList()

        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(false)

        configurePieChart()
        pieChart.data = pieData
        pieChart.invalidate()
    }

    private fun setIconCard(maxEntry: PieEntry?) {
        val localDB = SQLiteDBHelper(requireContext(), null)
        val filePath = localDB.getCategoryImage(maxEntry!!.label)

        val inputStream = context?.assets?.open("icons/${filePath}")
        val drawable = Drawable.createFromStream(inputStream, null)
        categoryIv.setImageDrawable(drawable)
        inputStream!!.close()
    }

    private fun generatePieEntries(processedRecords: MutableList<Map<String, Any>?>): List<PieEntry> {
        val groupedData = groupAndSumRecords(processedRecords)
        val pieEntries = mutableListOf<PieEntry>()

        if (groupedData.isNotEmpty()) {
            for ((type, sumAmount) in groupedData) {
                pieEntries.add(PieEntry(abs(sumAmount.toFloat()), type))
            }
        } else {
            pieEntries.add(PieEntry(1f, "None"))
        }

        return pieEntries
    }

    private fun getColorArray(recordsSize: Int): IntArray {
        val defaultColor = R.color.black
        val colors = resources.getIntArray(R.array.dashboard_colors)
        return if (recordsSize > 0) {
            colors.copyOfRange(0, recordsSize)
        } else {
            intArrayOf(defaultColor)
        }
    }

    private fun configurePieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.legend.isWordWrapEnabled = true
        pieChart.isRotationEnabled = true
        pieChart.dragDecelerationFrictionCoef = 0.9f
        pieChart.rotationAngle = 0f
        pieChart.isHighlightPerTapEnabled = false
        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad)
        val holeRadius = 35f
        pieChart.holeRadius = holeRadius
        pieChart.transparentCircleRadius = holeRadius-8f
    }

    private fun groupAndSumRecords(queryRecords: MutableList<Map<String, Any>?>): Map<String, Double> {
        val groupedData = mutableMapOf<String, Double>()

        for (record in queryRecords) {
            val type = record?.get("category").toString()
            val amount = (record?.get("amount") as Number).toDouble()

            groupedData[type] = groupedData.getOrDefault(type, 0.0) + amount
        }

        return groupedData
    }


}