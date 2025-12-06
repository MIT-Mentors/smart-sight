#include <WiFi.h>
#include <WebSocketsServer.h>
#include "esp_camera.h"

// Replace with your WiFi credentials
const char* ssid = "******";
const char* password = "******";

// SOS button pin
#define SOS_PIN 2 // D1 on the XIAO ESP32-S3 Sense

unsigned long lastDebounceTime = 0;
unsigned long debounceDelay = 50;
int buttonState;
int lastButtonState = HIGH;

// WebSocket server
WebSocketsServer webSocket = WebSocketsServer(8888);

// Camera config for Seeed XIAO ESP32-S3 Sense
camera_config_t camera_config = {
  .pin_pwdn       = -1,
  .pin_reset      = -1,
  .pin_xclk       = 10,
  .pin_sccb_sda   = 40,
  .pin_sccb_scl   = 39,

  .pin_d7         = 48,
  .pin_d6         = 11,
  .pin_d5         = 12,
  .pin_d4         = 14,
  .pin_d3         = 16,
  .pin_d2         = 18,
  .pin_d1         = 17,
  .pin_d0         = 15,

  .pin_vsync      = 38,
  .pin_href       = 47,
  .pin_pclk       = 13,

  .xclk_freq_hz   = 20000000,
  .ledc_timer     = LEDC_TIMER_0,
  .ledc_channel   = LEDC_CHANNEL_0,

  .pixel_format   = PIXFORMAT_JPEG,
  .frame_size     = FRAMESIZE_QVGA,
  .jpeg_quality   = 15,
  .fb_count       = 1
};

//   Fresh Capture Function
void captureAndSend(uint8_t clientNum) {

  // Flush 2–3 old buffered frames
  for (int i = 0; i < 3; i++) {
    camera_fb_t *old = esp_camera_fb_get();
    if (old) esp_camera_fb_return(old);
  }

  delay(80); // allow camera to capture a new frame

  camera_fb_t *fb = esp_camera_fb_get();
  if (!fb) {
    Serial.println("Camera capture failed!");
    webSocket.sendTXT(clientNum, "Camera capture failed!");
    return;
  }

  Serial.printf("Fresh capture: %u bytes\n", fb->len);

  // Send fresh JPEG image
  webSocket.sendBIN(clientNum, fb->buf, fb->len);

  Serial.printf("Sent image (%u bytes) to client %u\n", fb->len, clientNum);

  esp_camera_fb_return(fb);
}

//   WebSocket Events
void onWebSocketEvent(uint8_t num, WStype_t type, uint8_t *payload, size_t length) {

  switch (type) {

    case WStype_CONNECTED:
      Serial.printf("[WS] Client %u connected\n", num);
      break;

    case WStype_DISCONNECTED:
      Serial.printf("[WS] Client %u disconnected\n", num);
      break;

    case WStype_TEXT:
      Serial.printf("[WS] Received TEXT: %s\n", payload);

      if (strcmp((char*)payload, "capture") == 0) {
        captureAndSend(num);
      }
      break;
  }
}

//   SOS Button
void checkSOSButton() {
  int reading = digitalRead(SOS_PIN);

  if (reading != lastButtonState) {
    lastDebounceTime = millis();
  }

  if ((millis() - lastDebounceTime) > debounceDelay) {

    if (reading != buttonState) {
      buttonState = reading;

      if (buttonState == HIGH) {
        Serial.println("SOS button pressed! Broadcasting...");
        webSocket.broadcastTXT("SOS button");
      }
    }
  }

  lastButtonState = reading;
}

// Setup
void setup() {
  Serial.begin(115200);

  pinMode(SOS_PIN, INPUT_PULLDOWN);
  Serial.println("SOS Button initialized.");

  if (!psramFound()) {
    Serial.println("WARNING: PSRAM not found!");
  }

  // Initialize camera
  esp_err_t err = esp_camera_init(&camera_config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed! Error 0x%x\n", err);
    return;
  }
  Serial.println("Camera initialized.");

  // WiFi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(400);
  }
  Serial.println("\nWiFi connected!");
  Serial.print("ESP32 IP: ");
  Serial.println(WiFi.localIP());

  // WebSocket
  webSocket.begin();
  webSocket.onEvent(onWebSocketEvent);
  Serial.println("WebSocket server running on port 8888");
}

void loop() {
  webSocket.loop();
  checkSOSButton();
}
