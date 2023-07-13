package com.example.vnagrapher

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService

class BluetoothReceiver(ctxt: Context) {
    init{
        val bluetoothManager: BluetoothManager = ctxt.getSystemService((BluetoothManager::class.java))
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
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
                    ctxt,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
        }
        if (bluetoothAdapter?.isEnabled == true) {
            Log.d("stuff", "we boolin")
        }
    }
}