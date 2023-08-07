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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var btService: BluetoothService
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

        val textView: TextView = binding.receivedText
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        val activity = activity as FragmentActivity
        var bluetoothManager = activity.getSystemService<BluetoothManager>(BluetoothManager::class.java)
        val handler = Handler(activity.mainLooper, Handler.Callback {
            try {
                var numBytes = it.arg1
                Log.d(TAG, String((it.obj as ByteArray), 0, numBytes))
                binding.receivedText.setText( String((it.obj as ByteArray), 0, numBytes))
                return@Callback true
            } catch (e: Exception) {
                Log.d(TAG, "ISSUES IN HANDLER")
                return@Callback false
            }
        })

        btService = BluetoothService.getInstance(bluetoothManager)
        btService.addHandler(handler)
        btService.getPairedDevices()?.forEach { device ->
            val deviceName = device.name
            Log.d(TAG, deviceName)
            val deviceHardwareAddress = device.address // MAC address
            if(deviceHardwareAddress == "98:D3:11:FC:2F:A6")
            {
                btService.connectDevice(device, {}, _binding!!)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}