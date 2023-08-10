package com.example.vnagrapher.services

import android.bluetooth.BluetoothManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class VNAService {

    companion object {
        @Volatile
        private var instance: VNAService? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: VNAService().also { instance = it }
            }
    }

    val realData = MutableLiveData<List<Double>>()
    val imaginaryData = MutableLiveData<List<Double>>()

    fun handleMessage(msg: String) {
        val lines = msg.split("\n")
        if(lines[0].contains("data"))
        {
            updateDataWithString(lines.subList(1, lines.size-1))
        }
    }


    fun updateDataWithString(dataLines: List<String>) {
        var realList: List<Double> = listOf()
        var imaginaryList: List<Double> = listOf()
        for (dataLine in dataLines) {
            val (real, imaginary) = dataLine.split(" ").map { it.toDouble() }
            realList = realList.plus(real)
            imaginaryList = imaginaryList.plus(imaginary)
        }
        realData.value = realList
        imaginaryData.value = imaginaryList
    }

}