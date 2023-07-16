package com.example.vnagrapher

import BluetoothService
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.Handler
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import com.example.vnagrapher.databinding.ActivityMainBinding
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.*

var mUUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
var TAG = "VNA_GRAPHER"
class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var myBluetoothService: BluetoothService
    private lateinit var mHandler: Handler

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private lateinit var connectedThread: BluetoothService.ConnectedThread

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
        bluetoothAdapter = bluetoothManager.getAdapter()
        //--------------------------Kotlin--------------------------
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 2)

                return
            }
        }
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 2)
                return
            }
        }
        if (bluetoothAdapter == null) {
            Log.d("stuff", "NO BLUETOOTH")
            // Device doesn't support Bluetooth
        }
        if (bluetoothAdapter?.isEnabled == false) {
            Log.d("stuff", "bluetooth not enabled")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            val REQUEST_ENABLE_BT = 1
            Log.d("stuff", "bluetooth not here")
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
        }
        //bluetoothAdapter.getProfileConnectionState()
        if (bluetoothAdapter?.isEnabled == true) {
            Log.d("stuff", "we boolin")
        }


        mHandler = Handler(this.mainLooper, Handler.Callback {
            try {
                val response = it.obj as Pair<String, ByteArray>
                val from = response.first
                val msg = response.second.decodeToString()
                Log.d(TAG, msg)
                return@Callback true
            } catch (e: Exception) {
                Log.d(TAG, "ISSUES IN HANDLER")
                return@Callback false
            }
        })

        myBluetoothService = BluetoothService(mHandler)
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            Log.d(TAG, deviceName)
            val deviceHardwareAddress = device.address // MAC address
            if(deviceHardwareAddress == "98:D3:11:FC:2F:A6")
            {
                this.ConnectThread(device, binding).start()
            }

        }

    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice, binding: ActivityMainBinding) : Thread() {
        private val mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createInsecureRfcommSocketToServiceRecord(mUUID)
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                Log.d(TAG, "CONNECTED BITCH")
                connectedThread = myBluetoothService.ConnectedThread(socket)
                connectedThread.start()
                var utilThread = myBluetoothService.ConnectedThread(socket)

                binding.pause.setOnClickListener { view ->
                    var message = "pause\r"
                    Log.d(TAG, "WHAT UP FOOL")
                    utilThread.write(message.toByteArray())
                }
                binding.resume.setOnClickListener { view ->
                    var message = "resume\r"
                    Log.d(TAG, "WHAT UP FOOL")
                    utilThread.write(message.toByteArray())
                }
                binding.data.setOnClickListener { view ->
                    var message = "data\r"
                    Log.d(TAG, "WHAT UP FOOL")
                    utilThread.write(message.toByteArray())
                }

            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
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
