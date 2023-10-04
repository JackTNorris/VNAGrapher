package com.example.vnagrapher.ui.realtime_graph

import BluetoothService
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.vnagrapher.MainActivity
import com.example.vnagrapher.TAG
import com.example.vnagrapher.databinding.FragmentGraphBinding
import com.example.vnagrapher.databinding.FragmentRealtimeGraphBinding
import com.example.vnagrapher.services.VNAService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.lang.Exception

//TODO move logic to viewmodel
//TODO put hertz input into own component
class RealtimeGraphFragment : Fragment() {

    private var _binding: FragmentRealtimeGraphBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var btService: BluetoothService

    private val vnaService: VNAService = VNAService.getInstance()
    private var trackedFrequency = 14.0
    private var entries = ArrayList<Entry>()
    private var timeSeconds = 0
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var lineChart: LineChart


    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val realtimeGraphViewModel =
            ViewModelProvider(this)[RealtimeGraphViewModel::class.java]

        _binding = FragmentRealtimeGraphBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val activity = activity as FragmentActivity
        var bluetoothManager = activity.getSystemService<BluetoothManager>(BluetoothManager::class.java)
        btService = BluetoothService.getInstance(bluetoothManager, activity)
        lineChart = binding.data0chart
        binding.start.isEnabled = false
        binding.stop.isEnabled = false
        binding.trackedFrequency.setText(trackedFrequency.toString())
        binding.setFrequency.setOnClickListener(View.OnClickListener {
            try {
                binding.start.isEnabled = true
                binding.stop.isEnabled = false
                this.trackedFrequency = binding.trackedFrequency.text.toString().toDouble()
                btService.writeMessage(vnaService.generateSweepMessage(trackedFrequency, trackedFrequency))
            }
            catch(error: Exception) {
                Toast.makeText(context, "Error setting frequency: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        binding.start.setOnClickListener(View.OnClickListener {
            mainHandler.post(updateDataIntermittently)
            binding.stop.isEnabled = true
            binding.start.isEnabled = false
            binding.setFrequency.isEnabled = false
        })


        binding.stop.setOnClickListener(View.OnClickListener {
            mainHandler.removeCallbacks(updateDataIntermittently)
            if(this.binding.realtimeSaveFile.isChecked) {
                vnaService.writeDataToFile(entries, this.binding.realtimeFileName.text.toString());
            }
            timeSeconds = 0
            //vnaService.writeDataToFile(entries);
            this.entries.clear()
            binding.start.isEnabled = true
            binding.stop.isEnabled = false
            binding.setFrequency.isEnabled = true
        })

        return root
    }

    private val updateDataIntermittently = object : Runnable {
        override fun run() {
            btService.writeMessage("data 0\r")
            mainHandler.postDelayed(this, 1500)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: RealtimeGraphFragment")
        vnaService.data.removeObservers(viewLifecycleOwner)
        mainHandler.removeCallbacks(updateDataIntermittently)
        binding.realtimeSaveFile.isChecked = false
        binding.realtimeFileName.setText("")
        timeSeconds = 0
    }

    override fun onResume() {
        super.onResume()
        vnaService.data.observe(viewLifecycleOwner) { impedance ->
            Log.d(TAG, "Observed data change in realtime")
            val maxRealVal = impedance.maxOf { it.first }
            this.entries.add(Entry(timeSeconds.toFloat(), maxRealVal.toFloat()))
            timeSeconds += 1
            val real = LineDataSet(this.entries, "Real")

            //Part4
            real.setDrawValues(false)
            real.setColor(Color.rgb(255, 0, 0))
            real.label = "Impedance"
            //vl.setDrawFilled(true)
            real.lineWidth = 3f

            lineChart.axisRight.isEnabled = false

            //Part8
            lineChart.setTouchEnabled(true)
            lineChart.setPinchZoom(true)
            lineChart.onTouchListener

            lineChart.data = LineData(real)
            lineChart.notifyDataSetChanged()
            lineChart.invalidate()
        }
    }


}