package com.vladymix.ecotrack.service.models


data class DataSet(
    var latitude: Double,
    var longitude: Double,
    var co2:Float,
    var pm25:Float,
    var temperature:Float,
    var humidity:Float,
    var noise:Float,
    var air:Float,
    var air500:Float,
    var tvoc:Float,
    var lux:Float,
    var gas:Float
)