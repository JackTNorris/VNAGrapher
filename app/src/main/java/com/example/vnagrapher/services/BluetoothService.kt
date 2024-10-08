import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.vnagrapher.DeviceSelectionActivity.DeviceSelectionActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


private const val TAG = "MY_APP_DEBUG_TAG"

// Defines several constants used when transmitting messages between the
// service and the UI.
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2
// ... (Add other message types here as needed.)
private var new_command_crlf: String = "ch>"

class BluetoothService(
    // handler that gets info from Bluetooth service
    private val bluetoothManager: BluetoothManager,
    private var context: Context? = null
) {

    companion object {
        @Volatile
        private var instance: BluetoothService? = null

        //TODO: fix this to be a singleton
        fun getInstance(bluetoothManager: BluetoothManager?, context: Context?) =
            instance ?: synchronized(this) {
                Log.d("VNA_GRAPHER", "CREATING BLUETOOTH SERVICE")
                instance ?: BluetoothService(bluetoothManager as BluetoothManager, context).also { instance = it }
            }
    }

    private var bluetoothAdapter: BluetoothAdapter = bluetoothManager.getAdapter()
    lateinit var connectedThread: BluetoothService.ConnectedThread
    private lateinit var connectedDevice: BluetoothDevice

    var handlers: Array<Handler> = arrayOf<Handler>()

    fun addHandler(handler: Handler) {
        this.handlers += handler
    }

    @SuppressLint("MissingPermission")
    fun isBluetoothConnected(): Boolean {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled
                && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED)
    } //BluetoothHeadset.A2DP can also be used for Stereo media devices.


    fun writeMessage(message: String) {
        connectedThread.write(message.toByteArray())
    }

    fun configurePermission(activity: Activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 2)
                return
            }
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 2)
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
                    activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                return
            }
        }
    }

    fun connectDevice(device: BluetoothDevice, success_callback: () -> Unit, error_callback: () -> Unit) {
        this.ConnectThread(device, success_callback, error_callback).start()
    }

    fun terminateConnection() {
        connectedThread.cancel()
    }

    fun getPairedDevices(): Set<BluetoothDevice>? {
        @SuppressLint("MissingPermission")
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        return pairedDevices
    }


    inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()
            var msgString = ""
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmBuffer.fill(0)
                    mmInStream.read(mmBuffer)
                } catch (e: Exception) {

                    val intent = Intent(context, DeviceSelectionActivity::class.java) // New activity
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    context?.startActivity(intent)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Disconnected from device", Toast.LENGTH_LONG).show()
                    }
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }
                var recv_string = String(mmBuffer, 0, numBytes)
                msgString += recv_string
                if (msgString.indexOf(new_command_crlf) > 0)
                {
                    val byte_msg = msgString.toByteArray()
                    // Send the obtained bytes to the UI activity.
                    handlers.forEach {
                        val readMsg = it.obtainMessage(
                            MESSAGE_READ, byte_msg.size, -1,
                            byte_msg)
                        readMsg.sendToTarget()
                    }

                    msgString = ""
                }

            }
        }


        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                // Send a failure message back to the activity.
                handlers.forEach {
                    val writeErrorMsg = it.obtainMessage(MESSAGE_TOAST)
                    val bundle = Bundle().apply {
                        putString("toast", "Couldn't send data to the other device")
                    }
                    writeErrorMsg.data = bundle
                    it.sendMessage(writeErrorMsg)
                }

                return
            }

            // Share the sent message with the UI activity.
            handlers.forEach {
                val writtenMsg = it.obtainMessage(
                    MESSAGE_WRITE, -1, -1, mmBuffer)
                writtenMsg.sendToTarget()
            }

        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(var device: BluetoothDevice, private val success_callback: () -> Unit, private var error_callback: () -> Unit) : Thread() {
        private val mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createInsecureRfcommSocketToServiceRecord(mUUID)
        }
        override fun run() {
            // Cancel discovery because it   otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            try {
                mmSocket?.let { socket ->
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    socket.connect()
                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
                    Log.d(com.example.vnagrapher.TAG, "CONNECTED Dude")
                    connectedDevice = device
                    connectedThread = ConnectedThread(socket)

                    /*
                    var frag_binding = binding as FragmentHomeBinding
                    frag_binding.pause.setOnClickListener { view ->
                        var message = "pause\r"
                        connectedThread.write(message.toByteArray())
                    }
                    frag_binding.resume.setOnClickListener { view ->
                        var message = "resume\r"
                        connectedThread.write(message.toByteArray())
                    }
                    frag_binding.data.setOnClickListener { view ->
                        var dataNum = binding.dataNum.text.toString()
                        var message = "data $dataNum\r"
                        connectedThread.write(message.toByteArray())
                    }
                    frag_binding.setSweep.setOnClickListener {
                        var sweepStart = binding.sweepStart.text.toString()
                        var sweepEnd = binding.sweepEnd.text.toString()
                        Log.d(com.example.vnagrapher.TAG, sweepStart)
                        Log.d(com.example.vnagrapher.TAG, sweepEnd)
                        connectedThread.write(("sweep $sweepStart $sweepEnd\r").toByteArray())
                    }
                    */
                    connectedThread.start()
                    success_callback()
                }
            }
            catch(e: IOException)
            {
                error_callback()
                //make toast message

                Log.d(com.example.vnagrapher.TAG, "Error Connecting")
            }

        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(com.example.vnagrapher.TAG, "Could not close the client socket", e)
            }
        }
    }
}