package com.dsh105.nexus.server.decoder;

import com.dsh105.nexus.server.services.music.requests.CurrentSongRequest;
import org.eclipse.jetty.client.api.Request;

public class CurrentSongRequestDecoder implements RequestDecoder<CurrentSongRequest> {

    @Override
    public CurrentSongRequest decode(Request request) {
        return null;
    }
}
