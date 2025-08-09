<?php
$HOST = 'http://192.168.1.50:9100';
$imgPath = __DIR__ . '/logo.png';
$b64 = base64_encode(file_get_contents($imgPath));
$payload = [
  "mode" => "image",
  "base64" => $b64,
  "grayscale" => true,
  "dither" => true,
  "max_width_px" => 576,
  "cut" => true
];
$ch = curl_init("$HOST/print");
curl_setopt_array($ch, [
  CURLOPT_POST => true,
  CURLOPT_HTTPHEADER => ['Content-Type: application/json'],
  CURLOPT_POSTFIELDS => json_encode($payload),
  CURLOPT_RETURNTRANSFER => true,
  CURLOPT_TIMEOUT => 20
]);
$resp = curl_exec($ch);
if ($resp === false) die(curl_error($ch));
echo $resp;
