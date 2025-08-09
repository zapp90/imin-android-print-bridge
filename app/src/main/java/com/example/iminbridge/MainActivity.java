package com.example.iminbridge;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends Activity {
    private HTTPServer http;
    private PrinterAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("iMin Android Print Bridge\nListening on :9100\nPOST /print");
        tv.setPadding(32, 64, 32, 64);
        setContentView(tv);

        requestPermissions(new String[]{ Manifest.permission.INTERNET }, 1);

        // Choose adapter: try iMin first, fallback to ESC/POS TCP if configured
        try {
            adapter = new IMinPrinterAdapter();
            adapter.init(this);
        } catch (Exception e) {
            SharedPreferences sp = getSharedPreferences("bridge", MODE_PRIVATE);
            String host = sp.getString("tcp_host", "192.168.1.100");
            int port = sp.getInt("tcp_port", 9100);
            adapter = new EscPosPrinterAdapter(host, port);
        }

        http = new HTTPServer(9100, req -> {
            if (!"POST".equalsIgnoreCase(req.method) || !"/print".equals(req.path)) {
                return HTTPServer.Response.json(404, "{\"ok\":false,\"error\":\"not found\"}");
            }
            try {
                PrintJobParser.Job job = PrintJobParser.parse(req.body);

                // Optional token check
                SharedPreferences sp = getSharedPreferences("bridge", MODE_PRIVATE);
                String required = sp.getString("token", null);
                if (required != null && (job.token == null || !required.equals(job.token))) {
                    return HTTPServer.Response.json(401, "{\"ok\":false,\"error\":\"unauthorized\"}");
                }

                if ("text".equals(job.mode)) {
                    adapter.printText(job.lines, job.align, job.bold, job.wScale, job.hScale);
                } else if ("image".equals(job.mode)) {
                    adapter.printImage(job.image);
                } else if ("raw".equals(job.mode)) {
                    adapter.printRaw(job.raw);
                }

                if (job.beep) { try { adapter.beep(); } catch (Exception ignore) {} }
                if (job.cut)  { try { adapter.cut();  } catch (Exception ignore) {} }

                return HTTPServer.Response.json(200, "{\"ok\":true}");
            } catch (Exception ex) {
                String s = ex.getMessage() == null ? "error" : ex.getMessage().replace("\"", "'");
                return HTTPServer.Response.json(500, "{\"ok\":false,\"error\":\"" + s + "\"}");
            }
        });
        http.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (http != null) http.stop();
    }
}
