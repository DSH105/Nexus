package com.dsh105.nexus.server.services.music;

import org.json.simple.JSONObject;

public class Song {

    private final String url;

    public Song(final String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Song) {
            return ((Song) other).getUrl().equals(this.url);
        }

        return false;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("url", this.url);

        return json.toJSONString();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
