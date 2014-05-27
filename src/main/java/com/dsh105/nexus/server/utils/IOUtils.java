package com.dsh105.nexus.server.utils;

import org.eclipse.jetty.server.Request;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class IOUtils {

    private IOUtils() {}

    public static JSONObject toJson(final Request request) throws IOException {
        String encoding = request.getHeader("encoding");
        String content = "";
        BufferedReader reader = null;
        if (encoding != null && encoding.equals("gzip")) {
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(request.getInputStream()), "UTF-8"));
        } else {
            try {
                reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String line;
        while ((line = reader.readLine()) != null) {
            content += line;
        }

        return (JSONObject) JSONValue.parse(line);
    }
}
