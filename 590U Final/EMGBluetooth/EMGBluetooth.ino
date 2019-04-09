#include <Arduino.h>
#include <SPI.h>
#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"
// the setup function runs once when you press reset or power the board
Adafruit_BluefruitLE_SPI ble(8, 7, 4);
void setup() {
  // initialize digital pin 13 as an output.
  pinMode(A1, INPUT);
  ble.setMode(BLUEFRUIT_MODE_DATA);
  ble.begin(true);
  Serial.begin(9600);
}
 
// the loop function runs over and over again forever
void loop() {
  //ble.print("hello");
  bool out = false;
  while(ble.available()){
    int c = ble.read();
    Serial.print((char)c);
    out = true;
  }
  if(out){
    Serial.println();
    out = false;
  }
  ble.println(analogRead(A1));   
  
  delay(1000);
}
