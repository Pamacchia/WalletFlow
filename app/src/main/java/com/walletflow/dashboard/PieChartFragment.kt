package com.walletflow.dashboard

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.walletflow.R
import java.util.Arrays


class PieChartFragment : Fragment() {

    lateinit var pieChart : PieChart;
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_pie_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pieChart = view.findViewById(R.id.pieChartCategories) as PieChart
        initPieChart()
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")
        val db = FirebaseFirestore.getInstance()
        val queryRecords = db.collection("transactions")
            .whereEqualTo("user", userID)
            .whereEqualTo("type", "expense")
            .get()

        queryRecords.addOnCompleteListener {task ->

            val processedRecords : MutableList<Map<String, Any>?> = mutableListOf()

            for (document in task.result.documents){
                processedRecords.add(document.data)
                Log.w(context.toString(), document.data.toString())
            }

            showPieChart(processedRecords)
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
                pieEntries.add(PieEntry(sumAmount.toFloat(), type))
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
        pieChart.setData(pieData)
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