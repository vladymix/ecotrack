#include "BLESensor.h"

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#include "MClock.h"


// Variables para BLE
BLEServer *pServer = nullptr;
BLECharacteristic *pCharacteristic = nullptr;

// Definir nombres y UUIDs para el servicio y la característica
#define SERVICE_UUID "12345678-1234-1234-1234-1234567890ab"

#define CHAR_TEMP_UUID "ADAF0101-C332-42A8-93BD-25E905756CB8"
#define CHAR_ANALOG_UUID "ADAF0001-C332-42A8-93BD-25E905756CB8"
#define CHAR_EN160_UUID "EF680204-9B35-4933-9B10-52FFA9740042"
#define CHAR_CLOCK_UUID "ADAF0D02-C332-42A8-93BD-25E905756CB8"


bool deviceConnected = false;
float contador = 0.0;
String mClock = "00:00";

// Clase para manejar conexiones BLE
class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) {
    deviceConnected = true;
    Serial.println("Dispositivo conectado");
  }

  void onDisconnect(BLEServer* pServer) {
    deviceConnected = false;
    Serial.println("Dispositivo desconectado");
    // Permitir nuevas conexiones automáticamente
    pServer->startAdvertising();
  }
};


BLECharacteristic tempCharacteristic(
  CHAR_TEMP_UUID,
  BLECharacteristic::PROPERTY_READ |
  BLECharacteristic::PROPERTY_NOTIFY
);

BLECharacteristic analogCharacteristic(
  CHAR_ANALOG_UUID,
  BLECharacteristic::PROPERTY_READ |
  BLECharacteristic::PROPERTY_NOTIFY
);

BLECharacteristic en160Characteristic(
  CHAR_EN160_UUID,
  BLECharacteristic::PROPERTY_READ |
  BLECharacteristic::PROPERTY_NOTIFY
);

BLECharacteristic clockCharacteristic(
  CHAR_CLOCK_UUID,
  BLECharacteristic::PROPERTY_READ |
  BLECharacteristic::PROPERTY_WRITE |
  BLECharacteristic::PROPERTY_NOTIFY
);

void setupBLE(){
   Serial.println("Iniciando BLE...");

  // Configurar BLE
  BLEDevice::init("EcoTrack"); // Nombre del dispositivo
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

   // Crear un servicio
  BLEService *pService = pServer->createService(SERVICE_UUID);

  pService->addCharacteristic(&tempCharacteristic);
  pService->addCharacteristic(&analogCharacteristic);
  pService->addCharacteristic(&en160Characteristic);
  pService->addCharacteristic(&clockCharacteristic);

  clockCharacteristic.setValue(mClock);

  // Iniciar servicio y comenzar publicidad
  pService->start();

  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);

  pServer->getAdvertising()->start();
  Serial.println("BLE iniciado y listo para conectar");

}

void sendBLEData(EcoTrack_Sensor  &sensor){
   // Solo enviar datos si hay un dispositivo conectado
  if (deviceConnected) {
    Serial.println("BLE Conected");

    String controlValue = clockCharacteristic.getValue();
    if (controlValue != mClock) {
       Serial.print("**** Value changed... new value: ");
       Serial.println(controlValue);
       mClock = controlValue;
       sensor.mClock = getTimeInSeconds(mClock);
    }

    String data ="";
    //data.concat("T:");
    data.concat(sensor.temperature);
    data.concat("|");
    data.concat(sensor.relative_humidity);
    tempCharacteristic.setValue(data.c_str());
    tempCharacteristic.notify(); // Enviar notificación
    delay(50);

    data ="";
    data.concat(sensor.lux);
    data.concat("|");// 
    data.concat(sensor.ppm);
    data.concat("|");
    data.concat(sensor.noise);
    data.concat("|");
    data.concat(sensor.gas);
    analogCharacteristic.setValue(data.c_str());
    analogCharacteristic.notify();
    delay(50);

    data ="";
    data.concat(sensor.eCO2);
    data.concat("|");
    data.concat(sensor.AQI500);
    data.concat("|");
    data.concat(sensor.AQI);
    data.concat("|");
    data.concat(sensor.TVOC);
    en160Characteristic.setValue(data.c_str());
    en160Characteristic.notify();
  }
}