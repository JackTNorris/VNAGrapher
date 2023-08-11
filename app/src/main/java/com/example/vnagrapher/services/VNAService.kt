package com.example.vnagrapher.services

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

    val data = MutableLiveData<List<Pair<Double, Double>>>()
    var frequencies = listOf<Double>()
    var step = 0.0
    var sweepStart = 0.0
    var sweepStop = 0.0

    fun handleMessage(msg: String) {
        val lines = msg.split("\n")
        if(lines[0].contains("data"))
        {
            updateDataWithString(lines.subList(1, lines.size-1))
        }
        /*
        if(lines[0].contains("frequencies"))
        {
            updateFrequenciesWithString(lines.subList(1, lines.size-1))
        }
         */
        if(lines[0].contains("sweep"))
        {
            updateSweepWithString(lines[0])
        }
    }

    fun updateSweepWithString(sweepLine: String) {
        this.sweepStart = sweepLine.split(" ")[1].toDouble()
        this.sweepStop = sweepLine.split(" ")[2].toDouble()
        this.step = (sweepStop - sweepStart) / 100.0
    }

    fun updateFrequenciesWithString(frequencyLines: List<String>) {
        var frequencyList: List<Double> = listOf()
        for (frequencyLine in frequencyLines) {
            frequencyList = frequencyList.plus(frequencyLine.toDouble())
        }
        this.frequencies = frequencyList
    }

    fun updateDataWithString(dataLines: List<String>) {
        var realList: List<Double> = listOf()
        var imaginaryList: List<Double> = listOf()
        for (dataLine in dataLines) {
            val (real, imaginary) = dataLine.split(" ").map { it.toDouble() }
            realList = realList.plus(real)
            imaginaryList = imaginaryList.plus(imaginary)
        }
        data.value = realList.zip(imaginaryList)
    }

}