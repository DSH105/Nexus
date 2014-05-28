package com.dsh105.nexus.server.decoder.types;

import com.dsh105.nexus.server.decoder.RequestDecoder;
import com.dsh105.nexus.server.services.chanstats.ChanStatsRequest;
import org.eclipse.jetty.server.Request;

import java.io.IOException;

public class ChannelRequestDecoder implements RequestDecoder<ChanStatsRequest> {

    @Override
    public ChanStatsRequest decode(Request request) throws IOException {
        return null;
    }
}
