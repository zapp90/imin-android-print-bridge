# imin-android-print-bridge

Android HTTP print bridge for iMin POS devices — receive print jobs via HTTP (`/print`) and send them to the built-in printer using iMin SDK or ESC/POS fallback, with sample PHP clients included.

**A tiny Android HTTP bridge that lets any POS (PHP, Node, etc.) send print jobs to an iMin Android POS device.**  
Drop this app on the iMin terminal → POST text/image/ESC-POS to `http://device-ip:9100/print` → it prints. Simple.

---

## Why this exists
- Many POS backends (PHP, Node, Python) want to print receipts on iMin devices without learning Android SDKs.
- This bridge exposes a clean HTTP API and handles the device-side printing for you.

---

## Features
- **HTTP endpoint** `/print` on port **9100**
- **Text**, **Base64 image**, or **raw ESC/POS bytes** payloads
- **Pluggable printer adapters**: iMin SDK (if present) or generic ESC/POS-over-TCP fallback
- Optional CORS enable, token authentication
- Sample **PHP client** included

---

## Project Structure
```text
imin-android-print-bridge/
├── android-imin-bridge/
│   ├── app/
│   │   ├── build.gradle
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       └── java/com/example/iminbridge/
│   │           ├── MainActivity.java
│   │           ├── HTTPServer.java
│   │           ├── PrintJobParser.java
│   │           ├── PrinterAdapter.java
│   │           ├── IMinPrinterAdapter.java
│   │           ├── EscPosPrinterAdapter.java
│   │           └── Utils.java
│   └── build.gradle
├── php-client/
│   ├── print_text.php
│   ├── print_image.php
│   └── print_raw_escpos.php
├── LICENSE
└── README.md
```

---

## Quick Start
1. **Build & Install**
   - Open `android-imin-bridge` in Android Studio.
   - Set **minSdk 21** or higher, **targetSdk** recent.
   - Build and install the app on the **iMin device**.
   - Open the app once (starts the HTTP server in the foreground).

2. **Find the device IP**
   - On the iMin terminal, check Wi-Fi details (e.g., `192.168.1.50`).

3. **Test from your PC**
   - In `php-client/`, run `php print_text.php` after setting `$HOST = 'http://192.168.1.50:9100'`.

---

## HTTP API

### POST `/print`
**JSON body (text)**:
```json
{
  "mode": "text",
  "lines": [
    "ZAPP MART",
    "----------------------",
    "Item A       2 x 4.00",
    "Total            8.00",
    "",
    "Thank you!"
  ],
  "align": "left",
  "bold": false,
  "width_scale": 1,
  "height_scale": 1,
  "cut": true,
  "beep": false,
  "token": ""
}
```

**JSON body (image)**:
```json
{
  "mode": "image",
  "base64": "iVBORw0KGgoAAAANSUhEUgAA...",
  "grayscale": true,
  "dither": true,
  "max_width_px": 576,
  "cut": true,
  "token": ""
}
```

**JSON body (raw ESC/POS)**:
```json
{
  "mode": "raw",
  "base64": "G0AZbQ==",
  "token": ""
}
```

**Response**:
```json
{ "ok": true }
```
or
```json
{ "ok": false, "error": "message" }
```

**Headers**:
```
Content-Type: application/json
```

---

## Notes
If the iMin SDK is installed, the app will use it automatically.  
Otherwise, it can fallback to TCP ESC/POS (set in app UI > Settings).

---

## Security
Optional static token check: set in the app (then include `"token"` in body).  
Keep the device on a trusted LAN/VPN. Do not expose the port directly to the internet without a reverse proxy/auth.

---

## License
MIT — See LICENSE
