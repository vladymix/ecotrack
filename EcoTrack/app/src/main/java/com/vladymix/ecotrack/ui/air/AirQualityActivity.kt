package com.vladymix.ecotrack.ui.air

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.vladymix.ecotrack.R
import com.vladymix.ecotrack.databinding.ActivityAirQualityBinding
import com.vladymix.ecotrack.factory.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

@SuppressLint("MissingPermission")
class AirQualityActivity : AppCompatActivity() {

    companion object {

        private const val CHAR_TEMP_UUID = "ADAF0101-C332-42A8-93BD-25E905756CB8"
        private const val CHAR_ANALOG_UUID: String = "ADAF0001-C332-42A8-93BD-25E905756CB8"
        private const val CHAR_EN160_UUID: String = "EF680204-9B35-4933-9B10-52FFA9740042"
        private const val CHAR_CLOCK_UUID: String = "ADAF0D02-C332-42A8-93BD-25E905756CB8"

        fun create(context: Context): Intent {
            return Intent(context, AirQualityActivity::class.java)
        }
    }

    private var address: String?=""
    private lateinit var binding: ActivityAirQualityBinding
    private val viewMode = ViewModelFactory.airQualityViewModel
    private var gatt: BluetoothGatt?=null

    private var firstLocation = true
    private var alertShowed = false
    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationChangeListener {
            viewMode.setLocation(it.latitude, it.longitude)
            if(firstLocation){
                firstLocation = false
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude)))
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(15f))
            }

        }
    }

    private fun Context?.vibratePhone() {
        val vibrator = this?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAirQualityBinding.inflate(layoutInflater)

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).let {
            it.getMapAsync(callback)
        }

        setContentView(binding.root)

        viewMode.temperature.observe(this) {
            binding.tvTemperature.text = it
        }
        viewMode.humidity.observe(this){
            binding.tvHumidity.text = it
        }

        viewMode.lux.observe(this){
            //binding.tvLux.text = it
        }
        viewMode.ppm.observe(this){
            binding.tvPPM.text = it
        }
        viewMode.noise.observe(this){
            binding.tvNoise.text = it
        }

        viewMode.co2.observe(this){
            binding.tvCo2.text = it
        }

        viewMode.air.observe(this){
            val data =it.toFloatOrNull()?:0.0f
            if(data >= 0.8f ){
                AlertDialog.Builder(this).apply {
                    this.setOnDismissListener {
                        alertShowed = false
                    }
                    this.setTitle("Atención")
                    this.setMessage("Se ha detectado un entorno contaminado, por favor tomas las medidas necesarias")
                    this.setPositiveButton("Aceptar") { _, _ -> }

                    if(!alertShowed){
                        alertShowed = true
                        this.show()
                    }
                }
                this.vibratePhone()
            }

            binding.tvAQI.text = it
        }

        viewMode.deviceBle.observe(this) {
            Log.i("ERR","🔥 deviceBle observed")
            this.address = it.address
            connectToDevice(it)
        }
    }

    private fun scanLeDevice() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                address?.let {
                    if(address==result.device.address ){
                        connectToDevice(result.device)
                    }
                }
            }
            override fun onScanFailed(errorCode: Int) {
                Toast.makeText(this@AirQualityActivity, "Scan failed with error: $errorCode", Toast.LENGTH_LONG)
                    .show()
            }
        }

        bluetoothLeScanner?.startScan(scanCallback)
    }

    override fun onDestroy() {
        Log.i("BLE", "OnDestroy  🔴")
        this.gatt?.disconnect()
        this.gatt?.close()
        this.gatt = null
        Log.i("BLE", "OnDestroy  🔴")
        super.onDestroy()
    }

    private fun connectToDevice(device: BluetoothDevice) {

        device.connectGatt(this, true,
            object : BluetoothGattCallback() {

                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    this@AirQualityActivity.gatt = gatt
                    setLog("BLE", "onConnectionStateChange newState $newState await 2")
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        setLog("BLE", "Connected to GATT server")
                        gatt.discoverServices() // Descubrir servicios después de la conexión
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.e("BLE", "Desconectado de ${gatt.device.address}")
                       // gatt.close()
                        //finish()
                    }
                    else{
                        setLog("BLE", "Start scan again")
                        scanLeDevice()
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    setLog("BLE", "onServicesDiscovered status $status")
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val service = gatt.getService(UUID.fromString("12345678-1234-1234-1234-1234567890ab"))
                        val temtCharacteristic = service.getCharacteristic(UUID.fromString(CHAR_TEMP_UUID))
                        val analogCharacteristic = service.getCharacteristic(UUID.fromString(CHAR_ANALOG_UUID))
                        val en160Characteristic = service.getCharacteristic(UUID.fromString(CHAR_EN160_UUID))
                        val clockCharacteristic = service.getCharacteristic(UUID.fromString(CHAR_CLOCK_UUID))

                        temtCharacteristic?.let {
                            setLog("BLE", "🛰 Enable temtCharacteristic")
                            gatt.setCharacteristicNotification(it, true)
                        }
                        analogCharacteristic?.let {
                            setLog("BLE", "🛰 Enable analogCharacteristic")
                            gatt.setCharacteristicNotification(it, true)
                        }
                        en160Characteristic?.let {
                            setLog("BLE", "🛰 Enable en160Characteristic")
                            gatt.setCharacteristicNotification(it, true)
                        }

                        clockCharacteristic?.let {
                           val ti =  SimpleDateFormat("HH:mm").format(Date())
                            it.setValue(ti)
                            it.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            if(gatt.writeCharacteristic(it)){
                                setLog("BLE","💾 Write $ti")
                            }else{
                                setLog("BLE","🔴 NO Write $ti")
                            }
                        }
                    }
                }

                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray
                ) {
                    if (characteristic.uuid ==  UUID.fromString(CHAR_TEMP_UUID)) {
                        val value = characteristic.getStringValue(0)
                        viewMode.setTemperatureHumidity(value)
                        setLog("BLE", "🔥Temp: $value")
                    }
                    if (characteristic.uuid ==  UUID.fromString(CHAR_ANALOG_UUID)) {
                        val value = characteristic.getStringValue(0)
                        viewMode.setAnalogDevices(value)
                        setLog("BLE", "⏱️Analog: $value")
                    }
                    if (characteristic.uuid ==  UUID.fromString(CHAR_EN160_UUID)) {
                        val value = characteristic.getStringValue(0)
                        viewMode.setAirQuality(value)
                        setLog("BLE", "📟EN160: $value")
                    }

                }
            })
    }

    private fun setLog(tag:String, msn:String){
        Log.i(tag, msn)
        viewMode.setLog("$tag:$msn")

    }

}