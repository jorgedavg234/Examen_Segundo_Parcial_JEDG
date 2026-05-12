#include <SoftwareSerial.h>

#define PIR_PIN   9
#define LED_PIN   8

#define SEND_INTERVAL 500

unsigned long lastSendTime = 0;
bool ledState = false;

void setup() {
  Serial.begin(9600);
  pinMode(PIR_PIN, INPUT);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);
  Serial.println("Sentry Arduino iniciado");
}

void loop() {
  // Recibir comando desde la app
  if (Serial.available()) {
    char command = Serial.read();
    if (command == '1') {
      ledState = true;
      digitalWrite(LED_PIN, HIGH);
      Serial.println("LED: ON");
    } else if (command == '0') {
      ledState = false;
      digitalWrite(LED_PIN, LOW);
      Serial.println("LED: OFF");
    }
  }

  // Enviar estado del PIR cada SEND_INTERVAL ms
  unsigned long now = millis();
  if (now - lastSendTime >= SEND_INTERVAL) {
    lastSendTime = now;
    int motion = digitalRead(PIR_PIN);  // 1 = movimiento, 0 = sin movimiento
    String data = "M:" + String(motion);
    Serial.println(data);
  }
}