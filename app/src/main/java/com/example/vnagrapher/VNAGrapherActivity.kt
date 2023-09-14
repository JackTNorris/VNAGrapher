package com.example.vnagrapher

import BluetoothService
import android.bluetooth.BluetoothManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.vnagrapher.services.VNAService
import com.google.android.material.navigation.NavigationView

class VNAGrapherActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var myBluetoothService: BluetoothService
    private val vnaService = VNAService.getInstance()
    private lateinit var bluetoothManager: BluetoothManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothManager = getSystemService(BluetoothManager::class.java)

        myBluetoothService = BluetoothService.getInstance(bluetoothManager)
        myBluetoothService.configurePermission(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        val handler = Handler(mainLooper, Handler.Callback {
            try {
                var numBytes = it.arg1
                //Log.d(TAG, String((it.obj as ByteArray), 0, numBytes))
                var recievedString = String((it.obj as ByteArray), 0, numBytes)
                val rcvArray = recievedString.split("\n")
                Log.d(TAG, rcvArray.size.toString())
                vnaService.handleMessage(String((it.obj as ByteArray), 0, numBytes))
                return@Callback true
            } catch (e: Exception) {
                Log.d(TAG, "ISSUES IN HANDLER")
                return@Callback false
            }
        })
        myBluetoothService.addHandler(handler)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_graph,
                R.id.nav_realtime_graph,
                R.id.nav_alert_threshold,
                R.id.nav_led_alert
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
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