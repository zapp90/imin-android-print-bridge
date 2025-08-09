package com.example.iminbridge;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class Utils {
    public static int alignToImin(String align) {
        if ("center".equalsIgnoreCase(align)) return 1;
        if ("right".equalsIgnoreCase(align)) return 2;
        return 0;
    }

    public static byte[] escposAlign(String align) {
        if ("center".equalsIgnoreCase(align)) return new byte[]{0x1B, 0x61, 0x01};
        if ("right".equalsIgnoreCase(align))  return new byte[]{0x1B, 0x61, 0x02};
        return new byte[]{0x1B, 0x61, 0x00};
    }

    public static byte[] escposStyle(boolean bold, int w, int h) {
        int size = ((w - 1) << 4) | (h - 1);
        if (size < 0) size = 0;
        if (size > 0x77) size = 0x77;
        return new byte[]{
                0x1B, 0x21, (byte) (bold ? 0x08 : 0x00),
                0x1D, 0x21, (byte) size
        };
    }

    public static Bitmap decodeBitmap(byte[] data, int maxWidth, boolean grayscale, boolean dither) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bmp == null) return null;
        float scale = Math.min(1f, maxWidth / (float) bmp.getWidth());
        if (scale < 1f) bmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() * scale), (int) (bmp.getHeight() * scale), true);
        if (!grayscale) return bmp;
        Bitmap out = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        for (int y = 0; y < bmp.getHeight(); y++) {
            for (int x = 0; x < bmp.getWidth(); x++) {
                int c = bmp.getPixel(x, y);
                int g = (int)(0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c));
                if (dither) {
                    // simple threshold dither
                    g = g > 160 ? 255 : 0;
                }
                out.setPixel(x, y, Color.rgb(g, g, g));
            }
        }
        return out;
    }

    // Convert a grayscale Bitmap to ESC/POS raster data (approximate)
    public static byte[] bmpToEscPos(Bitmap bmp, int maxWidth) {
        Bitmap m = decodeBitmap(bitmapToBytes(bmp), maxWidth, true, true);
        int width = m.getWidth();
        int height = m.getHeight();
        int bytesPerRow = (width + 7) / 8;
        byte[] image = new byte[8 + (bytesPerRow * height) + height * 8];
        int p = 0;
        // GS v 0: raster bit image
        // We'll emit line by line using ESC * m=33 (24-dot) for broad compatibility
        byte[] out = new byte[(bytesPerRow + 8) * ((height + 23) / 24)];
        int op = 0;
        for (int yBlock = 0; yBlock < height; yBlock += 24) {
            int blockHeight = Math.min(24, height - yBlock);
            out[op++] = 0x1B; out[op++] = 0x2A; out[op++] = 33; // ESC * 33
            out[op++] = (byte) (width & 0xFF);
            out[op++] = (byte) ((width >> 8) & 0xFF);
            for (int x = 0; x < width; x++) {
                for (int k = 0; k < 3; k++) {
                    int slice = 0;
                    for (int b = 0; b < 8; b++) {
                        int y = yBlock + k*8 + b;
                        int bit = 0;
                        if (y < height) {
                            int c = m.getPixel(x, y) & 0xFF;
                            bit = (c < 128) ? 1 : 0;
                        }
                        slice |= (bit << (7 - b));
                    }
                    out[op++] = (byte) slice;
                }
            }
            out[op++] = 0x0A; // newline
        }
        byte[] res = new byte[op];
        System.arraycopy(out, 0, res, 0, op);
        return res;
    }

    private static byte[] bitmapToBytes(Bitmap bmp) {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
