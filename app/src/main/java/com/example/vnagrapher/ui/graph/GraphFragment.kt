package com.example.vnagrapher.ui.graph

import BluetoothService
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.vnagrapher.R
import com.example.vnagrapher.TAG
import com.example.vnagrapher.databinding.FragmentGraphBinding
import com.example.vnagrapher.services.VNAService
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.random.Random

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var btService: BluetoothService

    private val vnaService: VNAService = VNAService.getInstance()

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val graphViewModel =
            ViewModelProvider(this)[GraphViewModel::class.java]

        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //val textView: TextView = binding.receivedText
        graphViewModel.text.observe(viewLifecycleOwner) {

        }
        val activity = activity as FragmentActivity
        var bluetoothManager = activity.getSystemService<BluetoothManager>(BluetoothManager::class.java)
        btService = BluetoothService.getInstance(bluetoothManager)
        val entries = ArrayList<Entry>()

        for (i in 0..20) {
            entries.add(Entry(i.toFloat(), i.toFloat()))
        }

        val vl = LineDataSet(entries, "My Type")

    //Part4
        vl.setDrawValues(false)
        //vl.setDrawFilled(true)
        vl.lineWidth = 3f
        vl.fillColor =  R.color.gray
        vl.fillAlpha = R.color.red

        val lineChart = binding.data0chart
    //Part5
        lineChart.xAxis.labelRotationAngle = 0f

    //Part6
        lineChart.data = LineData(vl)

    //Part7
        lineChart.axisRight.isEnabled = false
        lineChart.xAxis.axisMaximum = 20f

        //Part8
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.onTouchListener
        //Part9
        lineChart.description.text = "Days"
        lineChart.setNoDataText("No forex yet!")

        binding.data.setOnClickListener { view ->
            var dataNum = binding.dataNum.text.toString()
            var message = "data $dataNum\r"
            btService.writeMessage(message)
        }
        vnaService.realData.observe(viewLifecycleOwner) {
            val entries = ArrayList<Entry>()
            for (i in 0..100) {
                //entries.add(Entry(i.toFloat(),it[i].toFloat()))
                //generate random number
                val x = Random.nextInt(0, 100)
                entries.add(Entry(i.toFloat(), x.toFloat()))
            }
            val vl = LineDataSet(entries, "My Type")

            //Part4
            vl.setDrawValues(false)
            //vl.setDrawFilled(true)
            vl.lineWidth = 3f
            vl.fillColor =  R.color.gray
            vl.fillAlpha = R.color.red
            lineChart.data = LineData(vl)
            lineChart.notifyDataSetChanged()
            lineChart.invalidate()
            Log.d(TAG, "Updated Data")
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}