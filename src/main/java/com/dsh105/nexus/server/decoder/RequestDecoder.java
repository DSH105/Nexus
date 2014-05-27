package com.dsh105.nexus.server.decoder;

import org.eclipse.jetty.server.Request;

import java.io.IOException;

public interface RequestDecoder<T> {

    public T decode(final Request request) throws IOException;
}
