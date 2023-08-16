package com.example.vnagrapher.ui.alert_threshold

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AlertThresholdViewModel {
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
}