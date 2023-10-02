package com.example.vnagrapher.ui.alert_threshold

import BluetoothService
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.ToneGenerator
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
import com.example.vnagrapher.TAG
import com.example.vnagrapher.databinding.FragmentAlertThresholdBinding
import com.example.vnagrapher.databinding.FragmentGraphBinding
import com.example.vnagrapher.databinding.FragmentRealtimeGraphBinding
import com.example.vnagrapher.services.VNAService
import com.example.vnagrapher.ui.graph.GraphViewModel
import com.example.vnagrapher.ui.realtime_graph.RealtimeGraphViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class AlertThresholdFragment: Fragment() {
    private var _binding: FragmentAlertThresholdBinding? = null

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
    private var tonePlaying = false
    private var lineColor = Color.rgb(0, 255, 0)


    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val realtimeGraphViewModel =
            ViewModelProvider(this)[RealtimeGraphViewModel::class.java]

        _binding = FragmentAlertThresholdBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val activity = activity as FragmentActivity
        var bluetoothManager = activity.getSystemService<BluetoothManager>(BluetoothManager::class.java)
        btService = BluetoothService.getInstance(bluetoothManager)
        lineChart = binding.data0chart
        binding.start.isEnabled = false
        binding.stop.isEnabled = false
        binding.setFrequency.isEnabled = true
        binding.trackedFrequency.setText(trackedFrequency.toString())
        binding.setFrequency.setOnClickListener(View.OnClickListener {
            try {
                binding.start.isEnabled = true
                timeSeconds = 0
                binding.stop.isEnabled = false
                this.trackedFrequency = binding.trackedFrequency.text.toString().toDouble()
                btService.writeMessage(vnaService.generateSweepMessage(trackedFrequency, trackedFrequency))
            }
            catch (error: Error) {
                Toast.makeText(context, "Error setting frequency", Toast.LENGTH_SHORT).show()
            }

        })

        binding.start.setOnClickListener(View.OnClickListener {
            mainHandler.post(updateDataIntermittently)
            binding.start.isEnabled = false
            binding.stop.isEnabled = true
            binding.setFrequency.isEnabled = false
        })


        binding.stop.setOnClickListener(View.OnClickListener {
            mainHandler.removeCallbacks(updateDataIntermittently)
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
        timeSeconds = 0
        Log.d(TAG, "onPause: RealtimeGraphFragment")
        vnaService.data.removeObservers(viewLifecycleOwner)
        mainHandler.removeCallbacks(updateDataIntermittently)
    }

    override fun onResume() {
        super.onResume()
        vnaService.data.observe(viewLifecycleOwner) { impedance ->
            Log.d(TAG, "Observed data change in realtime")
            val maxRealVal = impedance.maxOf { it.first }
            this.entries.add(Entry(timeSeconds.toFloat(), maxRealVal.toFloat()))
            timeSeconds += 1
            val real = LineDataSet(this.entries, "Real")
            synchronized(tonePlaying)
            {
                if(!tonePlaying && this.binding.alertThreshold.text.toString().isNotBlank() && maxRealVal > this.binding.alertThreshold.text.toString().toInt())
                {
                    lineColor = Color.rgb(255, 0, 0)
                    triggerSound()
                }
                else
                {
                    lineColor = Color.rgb(0, 255, 0)
                    tonePlaying = false
                }
            }
            //Part4
            real.setDrawValues(false)
            real.setColor(lineColor)
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

    private fun triggerSound() {
        synchronized(tonePlaying)
        {
            tonePlaying = true
        }
        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 50)

        Handler(Looper.getMainLooper()).postDelayed(
            {
                tonePlaying = false

                // This method will be executed once the timer is over
            },
            1000 // value in milliseconds
        )

    }


}