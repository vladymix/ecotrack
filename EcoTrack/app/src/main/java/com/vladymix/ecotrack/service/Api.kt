package com.vladymix.ecotrack.service

import android.util.Log
import com.google.gson.GsonBuilder
import com.vladymix.ecotrack.service.models.DataSet
import com.vladymix.ecotrack.service.models.EcoTrackRequest
import com.vladymix.ecotrack.service.models.LocationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Api {

    private val retrofit: Retrofit
    private val service: ApiService

    private lateinit var device: String

    companion object {
        private val BASE_URL = "https://flow.iot.etsisi.upm.es"
        private var instance: Api? = null
        fun getInstance(): Api {
            if (instance == null) {
                instance = Api()
            }
            return instance!!
        }
    }

    init {
        val client = OkHttpClient.Builder().build()
        val converter = GsonConverterFactory.create(GsonBuilder().create())

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(converter)
            .client(client)
            .build()
        service = retrofit.create(ApiService::class.java)
    }

    fun setDevice(device: String) {
        this.device = device
    }

    suspend fun sendLocation(latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            try {
                val response = service.postData(device, LocationRequest(latitude, longitude))
                if (response.isSuccessful) {
                    Log.i("API","Send data successful")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    suspend fun sendData(dataSet: DataSet) {
        withContext(Dispatchers.IO) {
            try {
                val response = service.postEcoData(EcoTrackRequest(3,
                    latitude = dataSet.latitude,
                    longitude = dataSet.longitude,
                    EcoTrackRequest.DatasetRequest(dataSet.co2,
                        dataSet.pm25,
                        dataSet.temperature,
                        dataSet.humidity,
                        dataSet.noise,
                        dataSet.air,
                        dataSet.air500,
                        dataSet.tvoc,
                        dataSet.lux,
                        dataSet.gas)))
                if (response.isSuccessful) {
                    Log.i("API","Send data successful")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}