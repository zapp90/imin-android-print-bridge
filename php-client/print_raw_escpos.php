<?php
$HOST = 'http://192.168.1.50:9100';

// ESC/POS: init, center, "HELLO", LF x2, cut
$raw = "\x1B\x40\x1B\x61\x01HELLO\n\n\x1D\x56\x00";

$payload = [
  "mode" => "raw",
  "base64" => base64_encode($raw)
];

$ch = curl_init("$HOST/print");
curl_setopt_array($ch, [
  CURLOPT_POST => true,
  CURLOPT_HTTPHEADER => ['Content-Type: application/json'],
  CURLOPT_POSTFIELDS => json_encode($payload),
  CURLOPT_RETURNTRANSFER => true,
]);
$resp = curl_exec($ch);
if ($resp === false) die(curl_error($ch));
echo $resp;
