#include <Arduino.h>
#include "MClock.h"

String getTimeToDisplay(int mSeconds){
  int hours  = mSeconds / (60*60);
  int minutes = (mSeconds - (hours*60*60))/60;
  int seconds = mSeconds - (hours* 60*60) - (minutes * 60);
  Serial.print("GET time >> ");

  char printLineA[17];
  sprintf (printLineA, "%02d:%02d:%02d", hours, minutes, seconds);
   

  String tm ="";
  tm.concat(hours);
  tm.concat(":");
  tm.concat(minutes);
  tm.concat(":");
  tm.concat(seconds);
  Serial.println(tm);
  return printLineA;
}

// Format 00:00
int getTimeInSeconds(String data){
  
  String sHours = data.substring(0,2);
  String sMinutes = data.substring(3,5);
 
  Serial.print("Set time >> ");
  Serial.print("data<<");
  Serial.print(data);
  Serial.print("toTime >> ");
  Serial.print(sHours);
  Serial.print(":");
  Serial.print(sMinutes);

  int hours = sHours.toInt();
  int minutes = sMinutes.toInt();
  int seconds =  (hours * 60 * 60)  + minutes * 60;
  Serial.print("Seconds is:");
  Serial.println(seconds);
  return seconds;
}
