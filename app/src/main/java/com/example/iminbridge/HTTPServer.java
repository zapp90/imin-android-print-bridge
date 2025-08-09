package com.example.iminbridge;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Minimal embedded HTTP server (NanoHTTPD-like, simplified)
public class HTTPServer {
    public interface Handler {
        Response handle(Request req) throws Exception;
    }

    public static class Request {
        public String method;
        public String path;
        public String body;
        public Map<String, String> headers = new HashMap<>();
    }

    public static class Response {
        public int status;
        public String body;
        public String contentType = "application/json";
        public static Response json(int code, String json) {
            Response r = new Response();
            r.status = code;
            r.body = json;
            return r;
        }
    }

    private final SimpleTcpServer server;
    private final Handler handler;

    public HTTPServer(int port, Handler handler) {
        this.handler = handler;
        this.server = new SimpleTcpServer(port, socket -> {
            try {
                SimpleHttpExchange ex = SimpleHttpExchange.read(socket);
                Request req = new Request();
                req.method = ex.method;
                req.path = ex.path;
                req.body = ex.body;
                req.headers = ex.headers;
                Response resp = handler.handle(req);
                ex.reply(resp.status, resp.contentType, resp.body);
            } catch (Exception e) {
                try {
                    SimpleHttpExchange.reply500(socket, e.getMessage());
                } catch (IOException ignored) {}
                Log.e("HTTPServer", "Error", e);
            } finally {
                try { socket.close(); } catch (IOException ignore) {}
            }
        });
    }

    public void start() { server.start(); }
    public void stop() { server.stop(); }
}
