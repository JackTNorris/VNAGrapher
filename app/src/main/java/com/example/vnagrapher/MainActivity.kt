package com.example.vnagrapher

import BluetoothService
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.vnagrapher.databinding.ActivityMainBinding
import android.util.Log
import java.util.*


var mUUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
var TAG = "VNA_GRAPHER"
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var myBluetoothService: BluetoothService
    private lateinit var mHandler: Handler

    private lateinit var bluetoothManager: BluetoothManager


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityMainBinding.inflate(layoutInflater)
     setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        Log.d("stuff", "Hello my friend")
        bluetoothManager = getSystemService(BluetoothManager::class.java)

        mHandler = Handler(this.mainLooper, Handler.Callback {
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

        myBluetoothService = BluetoothService(mHandler, bluetoothManager)
        myBluetoothService.configurePermission(this)
        myBluetoothService.getPairedDevices()?.forEach { device ->
            val deviceName = device.name
            Log.d(TAG, deviceName)
            val deviceHardwareAddress = device.address // MAC address
            if(deviceHardwareAddress == "98:D3:11:FC:2F:A6")
            {
                myBluetoothService.connectDevice(device, {}, binding)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when(item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment_content_main)
    return navController.navigateUp(appBarConfiguration)
            || super.onSupportNavigateUp()
    }

}
