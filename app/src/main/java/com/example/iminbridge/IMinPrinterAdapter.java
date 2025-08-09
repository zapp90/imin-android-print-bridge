package com.example.iminbridge;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

// Uses reflection so project compiles without iMin SDK at build time.
// If iMin SDK present at runtime, calls its APIs. Otherwise throws.
public class IMinPrinterAdapter implements PrinterAdapter {
    private Object printerHelper;

    @Override
    public void init(Context ctx) throws Exception {
        try {
            Class<?> helperCls = Class.forName("com.imin.printer.PrinterHelper"); // adjust to actual class if different
            printerHelper = helperCls.getConstructor(Context.class).newInstance(ctx);
            // Example: helper.initPrinter()
            helperCls.getMethod("initPrinter").invoke(printerHelper);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("iMin SDK not found on device.");
        }
    }

    @Override
    public void printText(String[] lines, String align, boolean bold, int wScale, int hScale) throws Exception {
        Class<?> cls = printerHelper.getClass();
        for (String line : lines) {
            cls.getMethod("setAlign", int.class).invoke(printerHelper, Utils.alignToImin(align));
            cls.getMethod("setTextStyle", boolean.class, int.class, int.class).invoke(printerHelper, bold, wScale, hScale);
            cls.getMethod("printText", String.class).invoke(printerHelper, line + "\n");
        }
        cls.getMethod("flush").invoke(printerHelper);
    }

    @Override
    public void printImage(Bitmap bmp) throws Exception {
        Class<?> cls = printerHelper.getClass();
        cls.getMethod("printBitmap", Bitmap.class).invoke(printerHelper, bmp);
        cls.getMethod("flush").invoke(printerHelper);
    }

    @Override
    public void printRaw(byte[] raw) throws Exception {
        Class<?> cls = printerHelper.getClass();
        cls.getMethod("sendRAWData", byte[].class).invoke(printerHelper, (Object) raw);
        cls.getMethod("flush").invoke(printerHelper);
    }

    @Override
    public void cut() throws Exception {
        try {
            printerHelper.getClass().getMethod("cutPaper").invoke(printerHelper);
        } catch (NoSuchMethodException e) {
            Log.w("IMinPrinterAdapter", "Cut not supported.");
        }
    }

    @Override
    public void beep() throws Exception {
        try {
            printerHelper.getClass().getMethod("beep").invoke(printerHelper);
        } catch (NoSuchMethodException e) {
            Log.w("IMinPrinterAdapter", "Beep not supported.");
        }
    }
}
