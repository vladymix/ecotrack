package com.vladymix.ecotrack.ui.devices

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.vladymix.ecotrack.R
import com.vladymix.ecotrack.databinding.ActivitySearchDeviceBinding
import com.vladymix.ecotrack.factory.ViewModelFactory
import com.vladymix.ecotrack.ui.air.AirQualityActivity

@SuppressLint("MissingPermission")
class SearchDeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchDeviceBinding
    private val adapter = DevicesAdapter(::onItemSelected)
    private var bluetoothLeScanner : BluetoothLeScanner?=null
    private var scanCallback:ScanCallback?=null

    private val viewMode = ViewModelFactory.airQualityViewModel

    private fun checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1024
            )
        } else {
            startBluetoothOperations()
        }
    }

    private fun startBluetoothOperations() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            startActivityForResult(enableBtIntent, 1024)
            return
        }
        if (bluetoothAdapter != null) {
            scanLeDevice()
        }
    }

    private fun scanLeDevice() {

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        this.bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        this.scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                adapter.addItem(device)
            }
            override fun onScanFailed(errorCode: Int) {
                Toast.makeText(this@SearchDeviceActivity, "Scan failed with error: $errorCode", Toast.LENGTH_LONG)
                    .show()
            }
        }

        this.bluetoothLeScanner?.stopScan(scanCallback)
        this.bluetoothLeScanner?.startScan(scanCallback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1024) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startBluetoothOperations()
            } else {
                Toast.makeText(this, "🔥User has not permissions", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onItemSelected(bluetoothDevice: BluetoothDevice) {
        this.bluetoothLeScanner?.stopScan(this.scanCallback)
        Handler().postDelayed({
            viewMode.setDevice(bluetoothDevice)
            startActivity(AirQualityActivity.create(this))
        }, 1000)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchDeviceBinding.inflate(layoutInflater)
        binding.listDevices.adapter = adapter
        setContentView(binding.root)
        checkBluetoothPermission()


    }
}