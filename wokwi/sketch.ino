#include <WiFi.h>
#include <PubSubClient.h>

// Wokwi virtual WiFi credentials
const char* ssid = "Wokwi-GUEST";
const char* password = "";

// HiveMQ public broker
const char* mqtt_server = "broker.hivemq.com";
const int mqtt_port = 1883;

// Topics
const char* topic_temp = "iotsimulator_myapp/temperature";
const char* topic_led = "iotsimulator_myapp/led";

// Hardware pins
const int ledPin = 26;

// State variable: track whether LED is ON or OFF
bool isLedOn = false;

WiFiClient espClient;
PubSubClient client(espClient);
unsigned long lastMsg = 0;

void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Connecting to WiFi: ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWiFi connected successfully!");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

void publish_live_temperature() {
  float fakeTemp = 22.0 + ((float)random(0, 60) / 10.0);
  char tempString[8];
  dtostrf(fakeTemp, 1, 1, tempString);

  Serial.print("Publishing Live temperature: ");
  Serial.print(tempString);
  Serial.println(" C");

  client.publish(topic_temp, tempString);
}

void callback(char* topic, byte* payload, unsigned int length) {
  String message = "";
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];
  }

  Serial.print("Message arrived on topic [");
  Serial.print(topic);
  Serial.print("]: ");
  Serial.println(message);

  if (String(topic) == topic_led) {
    if (message.equalsIgnoreCase("on")) {
      digitalWrite(ledPin, HIGH);
      isLedOn = true;
      Serial.println(" -> [ACTION] LED turned ON. Streaming LIVE temperature...");
      publish_live_temperature(); // Send first live reading immediately
    } else if (message.equalsIgnoreCase("off")) {
      digitalWrite(ledPin, LOW);
      isLedOn = false;
      Serial.println(" -> [ACTION] LED turned OFF. Sensor placed in STANDBY.");
    }
  }
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    String clientId = "ESP32Client-" + String(random(0xffff), HEX);

    if (client.connect(clientId.c_str())) {
      Serial.println(" Connected to broker!");
      client.subscribe(topic_led);
      Serial.print("Subscribed to: ");
      Serial.println(topic_led);
    } else {
      Serial.print("Failed, rc=");
      Serial.print(client.state());
      Serial.println(". Retrying in 5 seconds...");
      delay(5000);
    }
  }
}

void setup() {
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  isLedOn = false;

  Serial.begin(115200);
  delay(1000);
  Serial.println("Starting ESP32 MQTT Node...");

  setup_wifi();
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  unsigned long now = millis();
  if (now - lastMsg > 5000) {
    lastMsg = now;

    if (isLedOn) {
      publish_live_temperature();
    } else {
      Serial.println("LED is OFF -> Sensor in Standby (Paused).");
    }
  }
}
