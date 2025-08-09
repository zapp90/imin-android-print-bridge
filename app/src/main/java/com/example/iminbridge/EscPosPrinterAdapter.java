package com.example.iminbridge;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class EscPosPrinterAdapter implements PrinterAdapter {
    private final String host;
    private final int port;

    public EscPosPrinterAdapter(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override public void init(Context ctx) { /* No-op */ }

    private void withSocket(IO io) throws Exception {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), 3000);
            try (OutputStream os = s.getOutputStream()) { io.run(os); }
        }
    }

    @Override
    public void printText(String[] lines, String align, boolean bold, int wScale, int hScale) throws Exception {
        withSocket(os -> {
            os.write(new byte[]{0x1B, 0x40}); // init
            for (String line : lines) {
                os.write(Utils.escposAlign(align));
                os.write(Utils.escposStyle(bold, wScale, hScale));
                os.write((line + "\n").getBytes("UTF-8"));
            }
            os.write(new byte[]{0x0A});
        });
    }

    @Override
    public void printImage(Bitmap bmp) throws Exception {
        byte[] data = Utils.bmpToEscPos(bmp, 576);
        withSocket(os -> {
            os.write(new byte[]{0x1B, 0x40});
            os.write(data);
            os.write(new byte[]{0x0A});
        });
    }

    @Override
    public void printRaw(byte[] raw) throws Exception {
        withSocket(os -> os.write(raw));
    }

    @Override
    public void cut() throws Exception {
        withSocket(os -> os.write(new byte[]{0x1D, 0x56, 0x00}));
    }

    @Override
    public void beep() throws Exception {
        withSocket(os -> os.write(new byte[]{0x1B, 0x42, 0x03, 0x03}));
    }

    interface IO { void run(OutputStream os) throws Exception; }
}
