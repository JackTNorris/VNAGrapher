package com.example.vnagrapher.ui.led_alert

import BluetoothService
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.vnagrapher.R
import com.example.vnagrapher.TAG
import com.example.vnagrapher.databinding.FragmentAlertThresholdBinding
import com.example.vnagrapher.databinding.FragmentLedAlertBinding
import com.example.vnagrapher.services.VNAService
import com.example.vnagrapher.ui.realtime_graph.RealtimeGraphViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.lang.Double.parseDouble
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 * Use the [LedAlertFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LedAlertFragment : Fragment() {
    private var _binding: FragmentLedAlertBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var btService: BluetoothService

    private val vnaService: VNAService = VNAService.getInstance()
    private var trackedFrequency = 14.0
    private var entries = ArrayList<Entry>()
    private var timeSeconds = 0
    private val mainHandler = Handler(Looper.getMainLooper())
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

        _binding = FragmentLedAlertBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.ledTrackedFrequency.setText(trackedFrequency.toString())

        val activity = activity as FragmentActivity
        var bluetoothManager = activity.getSystemService<BluetoothManager>(BluetoothManager::class.java)
        btService = BluetoothService.getInstance(bluetoothManager, activity)
        binding.ledAlertStart.isEnabled = false
        binding.ledAlertStop.isEnabled = false
        binding.ledSetFrequency.isEnabled = true

        binding.ledSetFrequency.setOnClickListener(View.OnClickListener {
            try {
                binding.ledAlertStart.isEnabled = true
                binding.ledAlertStop.isEnabled = false
                Log.d(TAG, "Setting frequency")
                this.trackedFrequency = binding.ledTrackedFrequency.text.toString().toDouble()
                btService.writeMessage(vnaService.generateSweepMessage(trackedFrequency, trackedFrequency))
            }
            catch(error: Exception) {
                Log.d(TAG, "Error setting frequency: ${error.message}")
                Toast.makeText(context, "Error setting frequency: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        binding.ledAlertStart.setOnClickListener(View.OnClickListener {
            binding.ledAlertStart.isEnabled = false
            binding.ledAlertStop.isEnabled = true
            binding.ledSetFrequency.isEnabled = false
            mainHandler.post(updateDataIntermittently)
        })


        binding.ledAlertStop.setOnClickListener(View.OnClickListener {
            mainHandler.removeCallbacks(updateDataIntermittently)
            binding.ledAlertStart.isEnabled = true
            binding.ledAlertStop.isEnabled = false
            binding.ledSetFrequency.isEnabled = true
            binding.led.setImageResource(R.drawable.green_negative)
            //vnaService.writeDataToFile(entries);
            this.entries.clear()
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
                try {
                    if(!tonePlaying && this.binding.ledAlertThreshold.text.toString().isNotBlank() && maxRealVal > this.binding.ledAlertThreshold.text.toString().toInt())
                    {
                        binding.led.setImageResource(R.drawable.red_positive)
                        triggerSound()
                    }
                    else
                    {
                        binding.led.setImageResource(R.drawable.green_negative)
                        tonePlaying = false
                    }
                }
                catch(error: Exception) {
                    Toast.makeText(context, "Error with alert setting: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
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