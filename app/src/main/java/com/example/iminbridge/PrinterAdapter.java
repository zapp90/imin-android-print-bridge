package com.example.iminbridge;

import android.content.Context;
import android.graphics.Bitmap;

public interface PrinterAdapter {
    void init(Context ctx) throws Exception;
    void printText(String[] lines, String align, boolean bold, int wScale, int hScale) throws Exception;
    void printImage(Bitmap bmp) throws Exception;
    void printRaw(byte[] raw) throws Exception;
    void cut() throws Exception;
    void beep() throws Exception;
}
