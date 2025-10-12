#include <WiFi.h>
#include <WebSocketsServer.h>
#include "esp_camera.h"

// Replace with your WiFi credentials
const char* ssid = "*******";
const char* password = "*******";

// WebSocket server
WebSocketsServer webSocket = WebSocketsServer(8888);

// Camera config for Seeed Xiao ESP32S3 Sense
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

  .pixel_format   = PIXFORMAT_JPEG,   // JPEG for streaming
  .frame_size     = FRAMESIZE_QVGA,   // 320x240 (safe for memory)
  .jpeg_quality   = 15,               // lower = better quality
  .fb_count       = 1
};

void captureAndSend(uint8_t clientNum) {
  camera_fb_t * fb = esp_camera_fb_get();
  if (!fb) {
    Serial.println("Camera capture failed!");
    webSocket.sendTXT(clientNum, "Camera capture failed!");
    return;
  }

  Serial.printf("Captured %u bytes\n", fb->len);

  // Send JPEG buffer via WebSocket
  webSocket.sendBIN(clientNum, fb->buf, fb->len);
  Serial.printf("Sent image (%u bytes) to client %u\n", fb->len, clientNum);

  esp_camera_fb_return(fb);
}

void onWebSocketEvent(uint8_t num, WStype_t type, uint8_t * payload, size_t length) {
  switch(type) {
    case WStype_CONNECTED:
      Serial.printf("[WS] Client %u connected!\n", num);
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

void setup() {
  Serial.begin(115200);

  // 🔹 Enable PSRAM (required for camera!)
  if(!psramFound()) {
    Serial.println("PSRAM not found – camera may fail!");
  }

  // Initialize camera
  esp_err_t err = esp_camera_init(&camera_config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x\n", err);
    return;
  }
  Serial.println("Camera initialized.");

  // Connect WiFi
  WiFi.begin(ssid, password);
  Serial.println("Connecting to WiFi...");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi connected!");
  Serial.print("ESP32 IP: ");
  Serial.println(WiFi.localIP());

  // Start WebSocket server
  webSocket.begin();
  webSocket.onEvent(onWebSocketEvent);
  Serial.println("WebSocket server started on port 8888");
}

void loop() {
  webSocket.loop();
}
