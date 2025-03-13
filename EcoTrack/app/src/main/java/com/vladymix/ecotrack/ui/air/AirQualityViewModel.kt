package com.vladymix.ecotrack.ui.air

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladymix.ecotrack.service.Api
import com.vladymix.ecotrack.service.models.DataSet
import kotlinx.coroutines.launch

class AirQualityViewModel:ViewModel() {

    private val api = Api.getInstance()
    private val dataSet = DataSet(0.0,0.0,0f,0f,0f,0f,0f,0f,0f , 0f, 0f, 0f)

    var sendData = 0

    private fun sendData() {
        viewModelScope.launch {
            if(sendData%5==0){
                api.sendData(dataSet)
            }
            sendData++
        }
    }

    private val _log  = MutableLiveData<String>()
    val log : LiveData<String> get() = _log

    private val _deviceBle  = MutableLiveData<BluetoothDevice>()
    val deviceBle : LiveData<BluetoothDevice> get() = _deviceBle

    // Temperature and humidity
    private val _temperature  = MutableLiveData<String>()
    val temperature : LiveData<String> get() = _temperature

    private val _humidity  = MutableLiveData<String>()
    val humidity : LiveData<String> get() = _humidity

    // ANALOG DEVICES
    private val _lux  = MutableLiveData<String>()
    val lux : LiveData<String> get() = _lux

    private val _ppm  = MutableLiveData<String>()
    val ppm : LiveData<String> get() = _ppm

    private val _noise  = MutableLiveData<String>()
    val noise : LiveData<String> get() = _noise

    private val _gas  = MutableLiveData<String>()
    val gas : LiveData<String> get() = _gas

    // EN160
    private val _air  = MutableLiveData<String>()
    val air : LiveData<String> get() = _air

    private val _air500  = MutableLiveData<String>()
    val air500 : LiveData<String> get() = _air500

    private val _co2  = MutableLiveData<String>()
    val co2 : LiveData<String> get() = _co2

    private val _tvoc  = MutableLiveData<String>()
    val tvoc : LiveData<String> get() = _tvoc


    init {
        Log.i("AirQualityViewModel", "🟣 ViewModel is initialized")
    }

    fun setLog(s: String) {
       val nData = "${log.value}\n$s"
        _log.postValue(nData)
    }

    fun setDevice(bluetoothDevice: BluetoothDevice) {
        _deviceBle.postValue(bluetoothDevice)
    }

    fun setTemperatureHumidity(value: String?) {
        try {
           val data = value?.split("|").orEmpty()
            _temperature.postValue(data[0]+" °C")
            _humidity.postValue(data[1]+" %")
            dataSet.temperature = data[0].fixFloat()
            dataSet.humidity = data[1].fixFloat()
        }catch (ex:Exception){
            ex.printStackTrace()
            _log.postValue(ex.message)
        }
    }

    private fun getPercentage(value: String):String{
        try {
           val number =  (value+"f").toFloat()
            //4096 = 100
            // number = x
           val percentage = 100 * number/ 4096f
            return "$percentage %"
        }
        catch (ex:Exception){
            ex.printStackTrace()
        }
        return "-%"
    }

    private fun String.fixFloat(): Float {
        try {
            val number = (this + "f").toFloat()
            return number
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0f
    }

    private fun getTantoPorCien(value: String):String{
        try {
            val number =  (value+"f").toFloat()
            //4096 = 100
            // number = x
            val percentage =  (number / 1000)*100
            return "$percentage %"
        }
        catch (ex:Exception){
            ex.printStackTrace()
        }
        return "-%"
    }

    private fun getAir(value: String):String{
        try {
            val number =  (value+"f").toFloat()
            //4096 = 100
            // number = x
            val percentage = number / 5
            return "$percentage"
        }
        catch (ex:Exception){
            ex.printStackTrace()
        }
        return "-%"
    }

    fun setAnalogDevices(value: String?) {
        try {
            val data = value?.split("|").orEmpty()
            _lux.postValue(getPercentage(data[0]))
            _ppm.postValue(getPercentage(data[1]))
            _noise.postValue(getPercentage(data[2]))
            _gas.postValue(getPercentage(data[3]))

            dataSet.lux = data[0].fixFloat()
            dataSet.pm25 = data[1].fixFloat()
            dataSet.noise = data[2].fixFloat()
            dataSet.gas = data[3].fixFloat()

        }catch (ex:Exception){
            ex.printStackTrace()
            _log.postValue(ex.message)
        }
    }

    fun setAirQuality(value: String?) {
        try {
            val data = value?.split("|").orEmpty()
            _co2.postValue(getTantoPorCien(data[0]))
            _air500.postValue(getPercentage(data[1]))
            _air.postValue(getAir(data[2]))
            _tvoc.postValue(getPercentage(data[3]))

            dataSet.co2 = data[0].fixFloat()
            dataSet.air500 = data[1].fixFloat()
            dataSet.air = data[2].fixFloat()
            dataSet.tvoc = data[3].fixFloat()

            sendData()
        }catch (ex:Exception){
            ex.printStackTrace()
            _log.postValue(ex.message)
        }
    }

    fun setLocation(latitude: Double, longitude: Double) {
       dataSet.latitude = latitude
       dataSet.longitude = longitude
    }


}