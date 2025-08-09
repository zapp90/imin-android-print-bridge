package com.example.iminbridge;

import android.graphics.Bitmap;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

public class PrintJobParser {
    public static class Job {
        public String mode;
        public String[] lines;
        public String align = "left";
        public boolean bold = false;
        public int wScale = 1;
        public int hScale = 1;
        public Bitmap image;
        public byte[] raw;
        public boolean cut = false;
        public boolean beep = false;
        public String token = null;
    }

    public static Job parse(String body) throws Exception {
        JSONObject o = new JSONObject(body);
        Job j = new Job();
        j.mode = o.optString("mode", "text");

        if ("text".equals(j.mode)) {
            JSONArray arr = o.optJSONArray("lines");
            if (arr == null) throw new IllegalArgumentException("lines required");
            j.lines = new String[arr.length()];
            for (int i = 0; i < arr.length(); i++) j.lines[i] = arr.getString(i);
            j.align = o.optString("align", "left");
            j.bold = o.optBoolean("bold", false);
            j.wScale = o.optInt("width_scale", 1);
            j.hScale = o.optInt("height_scale", 1);
        } else if ("image".equals(j.mode)) {
            String b64 = o.getString("base64");
            byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
            j.image = Utils.decodeBitmap(bytes, o.optInt("max_width_px", 576), o.optBoolean("grayscale", true), o.optBoolean("dither", true));
        } else if ("raw".equals(j.mode)) {
            String b64 = o.getString("base64");
            j.raw = Base64.decode(b64, Base64.DEFAULT);
        } else {
            throw new IllegalArgumentException("Unknown mode");
        }

        j.cut = o.optBoolean("cut", false);
        j.beep = o.optBoolean("beep", false);
        if (o.has("token")) j.token = o.optString("token", null);

        return j;
    }
}
