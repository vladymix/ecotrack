#include <Arduino.h>

struct EcoTrack_Sensor
   {  
      int timeInitialize=30;
      int mClock = 0;
      int ciclos = 0;
      int ModeDisplay = 0;
      int noise; // 0-4096     
      int gas;   // 0-4096        
      int ppm;  // 0-4096   4, Detección del rango de partículas: hasta 8000pcs / 283 ml (1um partículas arriba); Power consumption 90mA
//Storage temperature -30~60°C
      int lux;  // 0-4096    
      uint8_t available_AHT2x; // 1: Sensor calibrado. 0: Sensor no calibrado.
      float temperature; // Celcius
      float relative_humidity; // percentage
      bool available_ens160;
      uint8_t AQI; //ppm 0 a 500
      uint16_t AQI500; //0 a 500
      uint16_t TVOC; //ppb
      uint16_t eCO2; //ppm
      uint8_t MISR; // 0 funcion normal, 1 error en la incialzacion 2 datos no confiables
      
   } ;

void setup_sensores();

void makeMeasure(EcoTrack_Sensor &sensor);

String getData(EcoTrack_Sensor &sensor);

void printDisplay(EcoTrack_Sensor &sensor);