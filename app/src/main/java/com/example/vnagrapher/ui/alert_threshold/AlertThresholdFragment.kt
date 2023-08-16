package com.example.vnagrapher.ui.alert_threshold

import BluetoothService
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.vnagrapher.databinding.FragmentAlertThresholdBinding
import com.example.vnagrapher.databinding.FragmentGraphBinding
import com.example.vnagrapher.services.VNAService
import com.example.vnagrapher.ui.graph.GraphViewModel
import com.github.mikephil.charting.charts.LineChart

class AlertThresholdFragment: Fragment() {

    private var _binding: FragmentAlertThresholdBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val vnaService: VNAService = VNAService.getInstance()
    private lateinit var lineChart: LineChart

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val graphViewModel =
            ViewModelProvider(this)[GraphViewModel::class.java]

        _binding = FragmentAlertThresholdBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //val textView: TextView = binding.receivedText
        graphViewModel.text.observe(viewLifecycleOwner) {

        }
        val activity = activity as FragmentActivity

        return root
    }
}