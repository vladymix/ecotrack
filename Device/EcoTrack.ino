
#include "BLESensor.h"

EcoTrack_Sensor sensor;


void setup() {
  // Inicializamos la comunicación serie
  Serial.begin(115200); // Velocidad de 115200 bps
  setupBLE();
  setup_sensores();
}



void loop() {

 unsigned long timeStart = millis();

  if(sensor.timeInitialize !=0 ){
      sensor.timeInitialize--;
  }else{
    makeMeasure(sensor);
  }

  printDisplay(sensor);
  sendBLEData(sensor);
  sensor.mClock++;
  unsigned long timeWait = millis() - timeStart;
  timeWait = 1000 - timeWait;
  if(timeWait > 1000){
    timeWait = 1000;
  }

  Serial.print("Tiempo espera (ms): ");
  Serial.println(timeWait);

  delay(timeWait); // Esperar 1segundo para siguiente medicion 50 + 50 de espera envio datos BLE

}

