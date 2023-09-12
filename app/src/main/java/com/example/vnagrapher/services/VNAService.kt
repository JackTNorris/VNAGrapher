package com.example.vnagrapher.services

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.vnagrapher.TAG
import com.github.mikephil.charting.data.Entry
import java.io.File
import java.io.FileWriter
import java.io.IOException
import org.kotlinmath.*



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
    var sweep = MutableLiveData<Pair<Double, Double>>()
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
            //converting to impedence
            val (newReal, newImaginary) = convertToImpedence(real, imaginary)
            realList = realList.plus(newReal)
            imaginaryList = imaginaryList.plus(newImaginary)
        }
        this.data.value = realList.zip(imaginaryList)
    }


    fun writeDataToFile(entries: ArrayList<Entry>, fileName: String) {
        try {
            var data = entries.joinToString("\n") { "${it.x} ${it.y}" }

            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val file = File(dir, "${fileName}.txt")
            data = "second, impedance\n" + data
            data = "data:\n" + data
            data = "frequencies: ${this.frequencies.joinToString(" ")}\n" + data
            data = "sweep: ${this.sweepStart} ${this.sweepStop}\n" + data
            data = "time_taken: ${System.currentTimeMillis()}\n" + data
            data = "step: ${this.step}\n" + data
            FileWriter(file).use { fileWriter -> fileWriter.append(data) }
            Log.d(TAG, "WROTE DATA TO FILE")

        } catch (e: IOException) {
            Log.d("Exception", "File write failed: $e")
        }
    }

    fun writeDataToFile(real_entries: ArrayList<Entry>, imag_entries: ArrayList<Entry>, fileName: String) {
        try {
            var fileOutputString = "frequency, real, imaginary\n"
            real_entries.forEachIndexed { index, entry ->
                fileOutputString += "${entry.x.toDouble()}, ${entry.y.toDouble()}, ${imag_entries.get(index).y}\n"
                Log.d(TAG, entry.x.toDouble().toString())
            }

            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val file = File(dir, "${fileName}.txt")
            fileOutputString = "data:\n" + fileOutputString
            fileOutputString = "frequencies: ${this.frequencies.joinToString(" ")}\n" + fileOutputString
            fileOutputString = "sweep: ${this.sweepStart} ${this.sweepStop}\n" + fileOutputString
            fileOutputString = "time_taken: ${System.currentTimeMillis()}\n" + fileOutputString
            fileOutputString = "step: ${this.step}\n" + fileOutputString
            FileWriter(file).use { fileWriter -> fileWriter.append(fileOutputString) }
            Log.d(TAG, "WROTE DATA TO FILE")

        } catch (e: IOException) {
            Log.d("Exception", "File write failed: $e")
        }
    }

    private fun convertToImpedence(real: Double, imaginary: Double): Array<Double> {
        var x = real + imaginary*I
        var Zn = 50.0 * (1 + x) / (1 - x)
        return arrayOf(Zn.re, Zn.im)
    }

}