package com.walletflow.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.walletflow.R

class PieChartFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment


        val chart = getView()?.findViewById(R.id.pieChartCategories) as BarChart

        val data = BarData(getXAxisValues(), getDataSet())
        chart.data = data
        chart.animateXY(2000, 2000)
        chart.invalidate()

        return inflater.inflate(R.layout.fragment_pie_chart, container, false)
    }

}