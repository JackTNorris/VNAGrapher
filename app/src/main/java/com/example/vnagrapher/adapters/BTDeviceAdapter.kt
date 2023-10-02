package com.example.vnagrapher.adapters

import android.R.attr.button
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vnagrapher.R


class BTDeviceAdapter(val itemClicked:(itemId:BluetoothDevice)->Unit)
    : ListAdapter<BluetoothDevice, BTDeviceAdapter.BTDeviceViewHolder>(BTDeviceComparator()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BTDeviceViewHolder {
        return BTDeviceViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: BTDeviceViewHolder, position: Int) {
        Log.d("BTDeviceAdapter","onBindViewHolder")
        val current = getItem(position)
        current?.let {
            holder.bind(it)
        }
        holder.itemView.tag = current.address
        holder.itemView.setOnClickListener {
            val itemId = it.tag
            Log.d("BTDeviceAdapter","Item Clicked: " + itemId)
            itemClicked(current)
        }
    }

    class BTDeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceName: TextView = itemView.findViewById(R.id.btDeviceName)
        private val deviceMac: TextView = itemView.findViewById(R.id.btDeviceMac)

        @SuppressLint("MissingPermission")
        fun bind(device: BluetoothDevice) {
            deviceName.text = device.name
            deviceMac.text = device.address
        }

        companion object {
            fun create(parent: ViewGroup): BTDeviceViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_btdevice, parent, false)
                return BTDeviceViewHolder(view)
            }
        }
    }

    class BTDeviceComparator : DiffUtil.ItemCallback<BluetoothDevice>() {
        override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address == newItem.address
        }
    }
}