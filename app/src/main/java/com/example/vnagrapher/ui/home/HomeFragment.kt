package com.example.vnagrapher.ui.home

import BluetoothService
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.vnagrapher.TAG
import com.example.vnagrapher.databinding.FragmentHomeBinding
import com.example.vnagrapher.services.VNAService

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var btService: BluetoothService
    private lateinit var vnaService: VNAService
    private lateinit var textView: TextView

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        textView = binding.receivedText
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        val activity = activity as FragmentActivity
        var bluetoothManager = activity.getSystemService<BluetoothManager>(BluetoothManager::class.java)
        btService = BluetoothService.getInstance(bluetoothManager, activity)
        vnaService = VNAService.getInstance()
        binding.pause.setOnClickListener { view ->
            var message = "pause\r"
            btService.writeMessage(message)
        }
        binding.resume.setOnClickListener { view ->
            var message = "resume\r"
            btService.writeMessage(message)
        }
        binding.data.setOnClickListener { view ->
            var dataNum = 0 //binding.dataNum.text.toString()
            var message = "data $dataNum\r"
            btService.writeMessage(message)
        }
        binding.setSweep.setOnClickListener {
            binding.data.isEnabled = true
            binding.pause.isEnabled = true
            binding.resume.isEnabled = true
            var sweepStart = binding.sweepStart.text.toString().toDouble()
            var sweepEnd = binding.sweepEnd.text.toString().toDouble()
            btService.writeMessage(vnaService.generateSweepMessage(sweepStart, sweepEnd))
        }
        binding.data.isEnabled = false
        binding.pause.isEnabled = false
        binding.resume.isEnabled = false


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        vnaService.data.observe(viewLifecycleOwner) {
            var text = ""
            for (element in it) {
                text += "${element.first}, ${element.second}\n"
            }
            textView.text = text
        }
    }

    override fun onPause() {
        super.onPause()
        vnaService.data.removeObservers(viewLifecycleOwner)
    }
}