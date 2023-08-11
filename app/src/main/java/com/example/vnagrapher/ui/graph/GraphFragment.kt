package com.example.vnagrapher.ui.graph

import BluetoothService
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.vnagrapher.TAG
import com.example.vnagrapher.databinding.FragmentGraphBinding
import com.example.vnagrapher.services.VNAService
import com.example.vnagrapher.ui.realtime_graph.RealtimeGraphViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

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

        val lineChart = binding.data0chart

        binding.data.setOnClickListener { view ->
            var dataNum = binding.dataNum.text.toString()
            var message = "data $dataNum\r"
            btService.writeMessage(message)
        }

        binding.setSweep.setOnClickListener {
            var sweepStart = binding.sweepStart.text.toString()
            var sweepEnd = binding.sweepEnd.text.toString()
            Log.d(com.example.vnagrapher.TAG, sweepStart)
            Log.d(com.example.vnagrapher.TAG, sweepEnd)
            btService.writeMessage(("sweep $sweepStart $sweepEnd\r"))
        }

        vnaService.data.observe(viewLifecycleOwner) {
            val real_entries = ArrayList<Entry>()
            val imag_entries = ArrayList<Entry>()
            for (i in 0..100) {
                val xVal = (vnaService.sweepStart + i * vnaService.step).toInt().toFloat()
                real_entries.add(Entry(xVal,it[i].first.toFloat()))
                imag_entries.add(Entry(xVal, it[i].second.toFloat()))
            }
            val real = LineDataSet(real_entries, "Real")

            val imag = LineDataSet(imag_entries, "Imaginary")
            //Part4
            real.setDrawValues(false)
            imag.setDrawValues(false)
            real.setColor(Color.rgb(255, 0, 0))
            imag.setColor(Color.rgb(0, 255, 0))
            //vl.setDrawFilled(true)
            real.lineWidth = 3f
            imag.lineWidth = 3f

            lineChart.axisRight.isEnabled = false

            //Part8
            lineChart.setTouchEnabled(true)
            lineChart.setPinchZoom(true)
            lineChart.onTouchListener

            lineChart.data = LineData(real, imag)
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