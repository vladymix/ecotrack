package com.vladymix.ecotrack.service.models


data class EcoTrackRequest(
    val id_device:Int =3,
    val latitude: Double,
    val longitude: Double,
    val dataset: DatasetRequest){

    data class DatasetRequest(
        val co2:Float,
        val pm25:Float,
        val temperature:Float,
        val humidity:Float,
        val noise:Float,
        val air:Float,
        val air500:Float,
        val tvoc:Float,
        val lux:Float,
        val gas:Float
    )
}

