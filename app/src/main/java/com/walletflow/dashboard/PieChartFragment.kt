@file:Suppress("DEPRECATION", "NAME_SHADOWING")

package com.walletflow.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.walletflow.R
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.abs


class PieChartFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var filterMonthTv: TextView
    private lateinit var filterYearTv: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_pie_chart, container, false)
    }

    @SuppressLint("SimpleDateFormat", "UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pieChart = view.findViewById(R.id.pieChartCategories) as PieChart
        filterMonthTv = view.findViewById(R.id.tvFilterDashboardMonth)
        filterYearTv = view.findViewById(R.id.tvFilterDashboardYear)
        initPieChart()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val oneMonthAgoString = SimpleDateFormat("yyyy-MM-dd HH:mm").format(calendar.time)

        val sharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")
        val db = FirebaseFirestore.getInstance()
        val queryRef = db.collection("transactions")
            .whereEqualTo("user", userID)
            .whereEqualTo("type", "expense")

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
        val processedRecords: MutableList<Map<String, Any>?> = mutableListOf()

        queryRef
            .whereGreaterThan("date", date)
            .get().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    for (document in task.result.documents) {
                        processedRecords.add(document.data)
                        Log.w(context.toString(), document.data.toString())
                    }

                    showPieChart(processedRecords)
                } else {
                    Log.w(requireContext().toString(), "Error getting transactions")
                }
            }
    }

    private fun showPieChart(processedRecords: MutableList<Map<String, Any>?>) {
        val pieEntries = ArrayList<PieEntry>()

        val colorArray = resources.getIntArray(R.array.dashboard_colors)
            .copyOfRange(0, maxOf(processedRecords.size, 1))

        val groupedData = groupAndSumRecords(processedRecords)
        // You can now use the groupedData as needed

        if (groupedData.isNotEmpty()) {
            for ((type, sumAmount) in groupedData) {
                pieEntries.add(PieEntry(abs(sumAmount.toFloat()), type))
            }
        } else {
            pieEntries.add(PieEntry(1f, "None"))
            colorArray[0] = R.color.teal_200
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
        //using percentage as values instead of amount
        pieChart.setUsePercentValues(true)
        //remove the description label on the lower left corner, default true if not set
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        //enabling the user to rotate the chart, default true
        pieChart.isRotationEnabled = true
        //adding friction when rotating the pie chart
        pieChart.dragDecelerationFrictionCoef = 0.9f
        //setting the first entry start from right hand side, default starting from top
        pieChart.rotationAngle = 0f
        //highlight the entry when it is tapped, default true if not set
        pieChart.isHighlightPerTapEnabled = true
        //adding animation so the entries pop up from 0 degree
        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad)
    }

    private fun groupAndSumRecords(queryRecords: MutableList<Map<String, Any>?>): Map<String, Double> {
        val groupedData = mutableMapOf<String, Double>()

        for (record in queryRecords) {
            val type = record?.get("category").toString()
            val amount = (record?.get("amount") as Number).toDouble()

            if (groupedData.containsKey(type)) {
                groupedData[type] = groupedData[type]!! + amount
            } else {
                groupedData[type] = amount
            }
        }

        return groupedData
    }

}