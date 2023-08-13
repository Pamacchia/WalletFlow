package com.walletflow.dashboard

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.walletflow.R
import com.walletflow.utils.StringHelper
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Calendar
import kotlin.math.abs


class PieChartFragment : Fragment() {

    lateinit var pieChart : PieChart
    lateinit var filterMonthTv : TextView
    lateinit var filterYearTv : TextView
    lateinit var totalSpentTv : TextView
    lateinit var savedRecapTv : TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_pie_chart, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pieChart = view.findViewById(R.id.pieChartCategories) as PieChart
        filterMonthTv = view.findViewById(R.id.tvFilterDashboardMonth)
        filterYearTv = view.findViewById(R.id.tvFilterDashboardYear)
        totalSpentTv = view.findViewById(R.id.tvTotalSpent)
        savedRecapTv = view.findViewById(R.id.tvAdviceSaving)

        initPieChart()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val oneMonthAgoString = SimpleDateFormat("yyyy-MM-dd HH:mm").format(calendar.time)

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")
        val db = FirebaseFirestore.getInstance()
        val queryRef = db.collection("transactions")
            .whereEqualTo("user", userID)

        filterRecordsByDate(queryRef, oneMonthAgoString)

        filterMonthTv.setOnClickListener {

            filterYearTv.background = null
            filterMonthTv.background = resources.getDrawable(R.drawable.edittext_rectangle)

            initPieChart()

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -1)
            val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm").format(calendar.time)
            filterRecordsByDate(queryRef, dateString)
        }

        filterYearTv.setOnClickListener {

            filterMonthTv.background = null
            filterYearTv.background = resources.getDrawable(R.drawable.edittext_rectangle)

            initPieChart()

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -1)
            val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm").format(calendar.time)
            filterRecordsByDate(queryRef, dateString)
        }

    }

    private fun filterRecordsByDate(
        queryRef: Query,
        date: String
    ) {
        var processedRecords: MutableList<Map<String, Any>?> = mutableListOf()
        var totalExpense = 0.0
        var totalEarning = 0.0

        queryRef
            .whereGreaterThan("date", date)
            .get().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    for (document in task.result.documents) {

                        val amount = document.getDouble("amount").toString().toDouble()

                        if(document.getString("type") == "expense"){
                            processedRecords.add(document.data)
                            totalExpense += amount
                        } else {
                            totalEarning += amount
                        }

                        val percentage = 100 * (totalEarning - totalExpense) / totalEarning
                        val formattedPercentage = String.format("%.2f%%", percentage)
                        savedRecapTv.text = "You earned ${totalEarning}$ and you spent ${abs(totalExpense)}$, " +
                                "meaning you saved ${formattedPercentage}% of your earnings since ${date}." //todo: euro
                    }
                    totalSpentTv.text = "Total sum of expenses: ${abs(totalExpense).toString()}$" //todo: euro
                    showPieChart(processedRecords)
                } else {
                    Log.w(requireContext().toString(), "Error getting transactions")
                }
            }
    }

    private fun showPieChart(processedRecords : MutableList<Map<String, Any>?>) {
        val pieEntries = ArrayList<PieEntry>()

        val colorArray = Arrays.copyOfRange(
            resources.getIntArray(R.array.dashboard_colors),
            0,
            maxOf(processedRecords.size, 1)
        )

        val groupedData = groupAndSumRecords(processedRecords)
        // You can now use the groupedData as needed

        if(groupedData.size != 0) {
            for ((type, sumAmount) in groupedData) {
                pieEntries.add(PieEntry(abs(sumAmount.toFloat()), type))
            }
        } else {
            pieEntries.add(PieEntry(1f, "None"))
            colorArray[0] = R.color.black
        }

        val pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet.valueTextSize = 15f
        pieDataSet.colors = colorArray.asList()
        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(false)
        pieChart.data = pieData
        pieChart.setDrawEntryLabels(false)
        pieChart.invalidate()
    }

    private fun initPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.isRotationEnabled = true
        pieChart.dragDecelerationFrictionCoef = 0.9f
        pieChart.rotationAngle = 0f
        pieChart.isHighlightPerTapEnabled = false
        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad)
    }
    fun groupAndSumRecords(queryRecords: MutableList<Map<String, Any>?>): Map<String, Double> {
        val groupedData = mutableMapOf<String, Double>()

        if (queryRecords != null) {
            for (record in queryRecords) {
                val type = record?.get("category").toString()
                val amount = (record?.get("amount") as Number).toDouble()

                if (groupedData.containsKey(type)) {
                    groupedData[type] = groupedData[type]!! + amount
                } else {
                    groupedData[type] = amount
                }
            }
        }

        return groupedData
    }

}