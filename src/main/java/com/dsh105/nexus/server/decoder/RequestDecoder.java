package com.dsh105.nexus.server.decoder;

import org.eclipse.jetty.client.api.Request;

public interface RequestDecoder<T> {

    public T decode(final Request request);
}
