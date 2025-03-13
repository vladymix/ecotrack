
#include <Wire.h>
#include "sensores.h"
#include <ScioSense_ENS160.h>
#include <Adafruit_AHTX0.h>

#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#include "MClock.h"

// Definimos el pin analógico (GPIO36 es un ADC en el ESP32)
// ADC1 conectado a 8 GPIOs (32-39)
// ADC2 conectado a 10 GPIOs (0, 2, 4, 12-15 y 25-27)
/*
TOUCH0 - GPIO 4
TOUCH1 - GPIO 0
TOUCH2 - GPIO 2
TOUCH3 - GPIO 15
TOUCH4 - GPIO 13
TOUCH5 - GPIO 12
TOUCH6 - GPIO 14
TOUCH7 - GPIO 27
TOUCH8 - GPIO 33
TOUCH9 - GPIO 32

// ADC2 conectado a 10 GPIOs (  y 25-26)

*/
// Noise
const int NOISE = 35;
// Gas
const int GAS = 34;
// GAS
const int EN_GAS = 12;

// PPM
const int PPM = 26;
// LUX
const int LUX = 25;
// Touch
const int TOUCH = 14;




#define SCREEN_WIDTH 128 // Ancho de la pantalla OLED
#define SCREEN_HEIGHT 64 // Altura de la pantalla OLED
#define OLED_RESET    -1 // Pin de reset (opcional)

// Inicializa la pantalla con el ancho y la altura especificados
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);


// Crear objeto para el sensor ENS160
//ScioSense_ENS160    ens160(ENS160_I2CADDR_0);
ScioSense_ENS160 ens160(ENS160_I2CADDR_1); //0x53..ENS160+AHT21


// Crear objeto para el sensor Adafruit_AHT2X0
Adafruit_AHTX0 aht;
bool buttonPressed = false;
// Función de interrupción
void IRAM_ATTR handleButtonPress() {
  Serial.println("¡Botón pulsado!");
  
  buttonPressed = true;  // Cambia el estado de la variable al detectar el evento
}


void setup_sensores()
{
 pinMode(NOISE, INPUT);   
 pinMode(GAS, INPUT);  
 pinMode(PPM, INPUT);  
 pinMode(LUX, INPUT);  

 pinMode(TOUCH, INPUT);  
 pinMode(EN_GAS, OUTPUT);

// Iniciar el bus I2C
  Wire.begin(23, 22);  // Pines I2C predeterminados para ESP32 (21 para SDA, 22 para SCL)



  // Inicializa la pantalla y verifica si se ha detectado correctamente
 if(!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) { 
    Serial.println(F("Error al inicializar la pantalla OLED"));
    //while(true);
  }

  display.clearDisplay(); // Limpia la pantalla
  display.setTextSize(2); // Tamaño del texto (1 es el más pequeño)
  display.setTextColor(SSD1306_WHITE); // Color del texto

  display.setCursor(13,20); // Posición del cursor (arriba a la izquierda)
  display.println("EcoTrack"); // Texto a mostrar
  display.display(); // Envía los datos a la pantalla

  

  // Inicializar el sensor ENS160
   ens160.begin();
  Serial.println(ens160.available() ? "Ens160 done." : "Ens160 failed!");

  if (ens160.available()) {
    // Print ENS160 versions
    Serial.print("\tRev: "); Serial.print(ens160.getMajorRev());
    Serial.print("."); Serial.print(ens160.getMinorRev());
    Serial.print("."); Serial.println(ens160.getBuild());
  
    Serial.print("\tStandard mode ");
    Serial.println(ens160.setMode(ENS160_OPMODE_STD) ? "Mode done." : "Mode failed!");
  }else{
    display.clearDisplay();
    display.setTextSize(0); 
    display.println("Error al inicializar ens160.");
    display.display(); // Envía los datos a la pantalla
    Serial.println("Error al inicializar ens160.");
    sleep(100); 
  }

  // Inicializar el sensor AHT2x
  if (aht.begin()) {
    Serial.println("AHT2x inicializado correctamente.");
  } else {
    display.clearDisplay();
    display.setTextSize(0); // Tamaño del texto (1 es el más pequeño)
    display.setCursor(0,0);
    display.println("Error al inicializar AHT2x."); // Texto a mostrar
    display.display(); // Envía los datos a la pantalla
    Serial.println("Error al inicializar AHT2x.");
    sleep(5000);
    
    while (1); // Detener el programa si no se inicializa el AHT2x
  }

  digitalWrite(EN_GAS, LOW);   // Low to power on
   // Configurar la interrupción
 attachInterrupt(digitalPinToInterrupt(TOUCH), handleButtonPress, FALLING);

//HIGHT to power off

}

void makeMeasure(EcoTrack_Sensor &sensor)
{

  if(buttonPressed){
    sensor.ModeDisplay++;
    sensor.ModeDisplay = sensor.ModeDisplay % 5;
    Serial.println("¡Botón reet!");
     buttonPressed = false;  // Reiniciar la variable para detectar el próximo evento
  }

  sensor.ciclos++;
    // Leer datos del ENS160
  sensor.available_ens160 = ens160.available();

 if (ens160.available()) {
    ens160.measure(true);
    ens160.measureRaw(true);
    sensor.AQI = ens160.getAQI();
    sensor.AQI500 = ens160.getAQI500();
    sensor.TVOC = ens160.getTVOC();
    sensor.eCO2 = ens160.geteCO2();
    sensor.MISR = ens160.getMISR();
  }

 sensor.available_AHT2x = aht.getStatus();

  // Leer datos del sensor AHT2x
  sensors_event_t humidity, temp;
 
  aht.getEvent(&humidity, &temp);
  sensor.temperature = temp.temperature;
  sensor.relative_humidity = humidity.relative_humidity;
  
  // Leemos el valor analógico del pin definido
  sensor.noise = analogRead(NOISE); // int(0 a 1023) (un número entero) 
  sensor.gas = analogRead(GAS);
  sensor.ppm = analogRead(PPM);
  sensor.lux = analogRead(LUX);
 
  Serial.println("-------------------------");

  // Mostramos el valor en el terminal serie
  Serial.print("vNoise:");
  Serial.println(sensor.noise);
  Serial.print("vGas:");
  Serial.println(sensor.gas);
  Serial.print("vPPM:");
  Serial.println(sensor.ppm);
  Serial.print("vLUX:");
  Serial.println(sensor.lux);

    // Imprimir datos de los sensores en el monitor serial
  Serial.println(">> AHT2x ");
  Serial.print("Estado: ");
  Serial.println(sensor.available_AHT2x);
  Serial.print("Temperatura (AHT2x): ");
  Serial.print(sensor.temperature);
  Serial.println(" °C");
  Serial.print("Humedad (AHT2x): ");
  Serial.print(sensor.relative_humidity);
  Serial.println(" %");

  Serial.println(">> ENS160");
  Serial.print("Estado: ");
  Serial.println(sensor.available_ens160);
  Serial.print("AQI: ");
  Serial.print(sensor.AQI);
  Serial.println(" (0-500 ppm)");
  Serial.print("AQI500: ");
  Serial.print(sensor.AQI500);
  Serial.println(" (0-500)");
  Serial.print("TVOC: ");
  Serial.print(sensor.TVOC);
  Serial.println(" ppb");
  Serial.print("eCO2: ");
  Serial.print(sensor.eCO2);
  Serial.println(" ppm");
  Serial.print("MISR: ");
  Serial.print(sensor.MISR);
  Serial.println(" ppm");
}

String getData(EcoTrack_Sensor &sensor)
{
    String data ="";
    data.concat("T:");
    data.concat(sensor.temperature);
    data.concat("|H:");
    data.concat(sensor.relative_humidity);
    data.concat("|ENS:");
    data.concat(sensor.available_ens160);
    data.concat("|eCO2:");
    data.concat(sensor.eCO2);
    data.concat("|AQI500:");
    data.concat(sensor.AQI500);
    data.concat("|Lux:");
    data.concat(sensor.lux);
    data.concat("|ppm:");
    data.concat(sensor.ppm);
    data.concat("|noise:");
    data.concat(sensor.noise);
    data.concat("|gas:");
    data.concat(sensor.gas);
    return data;
}

void printDisplay(EcoTrack_Sensor &sensor)
{

  if(sensor.timeInitialize !=0){
      display.clearDisplay(); // Limpia la pantalla
      display.setTextSize(2); // Tamaño del texto (1 es el más pequeño)
      display.setCursor(13,20); // Posición del cursor (arriba a la izquierda)
      display.println(sensor.timeInitialize); // Texto a mostrar
      display.display(); // Envía los datos a la pantalla
      Serial.print("Time to initialized");
      Serial.println(sensor.timeInitialize);
      return;
   }

  display.clearDisplay(); // Limpia la pantalla


   if(sensor.ModeDisplay==1){
      display.setTextSize(2); // Tamaño del texto (1 es el más pequeño)
      display.setCursor(0,12); // Posición del cursor (arriba a la izquierda)
      String log ="T: ";
      log.concat(sensor.temperature);
      log.concat(" c");
      log.concat("\nH: ");
      log.concat(sensor.relative_humidity);
      log.concat(" %");
      display.println(log); // Texto a mostrar
      display.display(); // Envía los datos a la pantalla
    }
    else if(sensor.ModeDisplay==2){
      display.setTextSize(2); // Tamaño del texto (1 es el más pequeño)
      display.setCursor(0,0); // Posición del cursor (arriba a la izquierda)
      String log ="Lux: ";
      log.concat(sensor.lux);
      log.concat("\nN: ");
      log.concat(sensor.noise);
      log.concat("\nPpm: ");
      log.concat(sensor.ppm);
      log.concat("\nGas: ");
      log.concat(sensor.gas);
      display.println(log); // Texto a mostrar
      display.display(); // Envía los datos a la pantalla
    }

     else if(sensor.ModeDisplay==3){
      display.setTextSize(2); // Tamaño del texto (1 es el más pequeño)
      display.setCursor(0,0); // Posición del cursor (arriba a la izquierda)
      String log ="AQI: ";
      log.concat(sensor.AQI);
      log.concat("\nAQI500: ");
      log.concat(sensor.AQI500);
      log.concat("\neCO2: ");
      log.concat(sensor.eCO2);
      log.concat("\nTVOC: ");
      log.concat(sensor.TVOC);
      display.println(log); // Texto a mostrar
      display.display(); // Envía los datos a la pantalla
    }

     else if(sensor.ModeDisplay == 4){
      display.setTextSize(2); // Tamaño del texto (1 es el más pequeño)
      display.setCursor(13,20); // Posición del cursor (arriba a la izquierda)
      String log = getTimeToDisplay(sensor.mClock);
      display.println(log); // Texto a mostrar
      display.display(); // Envía los datos a la pantalla
    }
    
    else{
      
   
     display.setTextSize(0); // Tamaño del texto (1 es el más pequeño)
     display.setCursor(0,0);

     String log ="Temperature: ";
     log.concat(sensor.temperature);
     log.concat("\nHumidity: ");
     log.concat(sensor.relative_humidity);
     log.concat("\neCO2: ");
     log.concat(sensor.eCO2);
     log.concat("\nAQI500: ");
     log.concat(sensor.AQI500);
     log.concat("\nLux: ");
     log.concat(sensor.lux);
     log.concat("\nPPM: ");
     log.concat(sensor.ppm);
     log.concat("\nCiclos ");
     log.concat(sensor.ciclos);
    
     display.println(log); // Texto a mostrar
     display.display(); // Envía los datos a la pantalla
    }

}
