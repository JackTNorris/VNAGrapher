package com.example.vnagrapher.DeviceSelectionActivity

import BluetoothService
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vnagrapher.R
import com.example.vnagrapher.VNAGrapherActivity
import com.example.vnagrapher.adapters.BTDeviceAdapter

class DeviceSelectionActivity : AppCompatActivity() {
    lateinit var adapter: BTDeviceAdapter
    lateinit var btService: BluetoothService
    val startVNAGrapherActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode== Activity.RESULT_OK){
            Log.d("MainActivity","Completed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btService = BluetoothService.getInstance(getSystemService(BluetoothManager::class.java))
        btService.configurePermission(this)
        setContentView(R.layout.activity_device_selection)
        adapter = BTDeviceAdapter(this::recyclerAdapterItemClicked)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        Log.d("Paired Devices", btService.getPairedDevices()!!.toList().toString())
        adapter.submitList(btService.getPairedDevices()!!.toList())
    }

    override fun onResume() {
        super.onResume()
    }

    fun recyclerAdapterItemClicked(device:BluetoothDevice){
        btService.connectDevice(device, {
            startVNAGrapherActivity.launch(
                Intent(
                    this,
                    VNAGrapherActivity::class.java
                )
            )
        }, {
            createErrorToast()
        })
    }

    fun createErrorToast() {
        //create toast message
        val text = "Error connecting to device"
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(this, text, duration) // in Activity
        toast.show()
    }

}